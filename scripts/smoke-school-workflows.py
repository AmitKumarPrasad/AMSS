"""Exercise real HTTP workflows in the disposable CI school under the 512 MiB limit."""
import json
from datetime import date
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4

BASE = "http://localhost:8080"
SCHOOL = "11111111-1111-1111-1111-111111111111"
STUDENT = "33333333-3333-3333-3333-333333333333"
ROOT = f"/api/v1/schools/{SCHOOL}"
TOKEN = json.loads(Path("/tmp/demo-login.json").read_text())["accessToken"]


def request(path, method="GET", body=None, expected=200, content_type="application/json", raw=False):
    data = body if isinstance(body, bytes) else json.dumps(body).encode() if body is not None else None
    headers = {"X-Request-ID": str(uuid4()), "Content-Type": content_type}
    if TOKEN: headers["Authorization"] = f"Bearer {TOKEN}"
    try:
        with urlopen(Request(BASE + path, data=data, headers=headers, method=method), timeout=30) as response:
            status, payload = response.status, response.read()
    except HTTPError as error:
        status, payload = error.code, error.read()
    assert status == expected, f"{method} {path}: expected {expected}, got {status}: {payload[:500]!r}"
    return payload if raw else json.loads(payload) if payload else None


today = date.today().isoformat()
for _ in range(3):
    for endpoint in ["students?status=ACTIVE&q=Aarav", "academic-years", "academics/subjects",
                     f"attendance/summary?date={today}", "fees/invoices", "notifications",
                     "documents?publishedOnly=false", "operations/dashboard", "operations/audit", "ai/conversations"]:
        request(f"{ROOT}/{endpoint}")

request(f"/api/v1/schools/{uuid4()}/students", expected=403)
request(f"{ROOT}/attendance/students/bulk", "POST", {
    "attendanceDate": today, "entries": [{"studentId": STUDENT, "status": "PRESENT"}]
})
assert request(f"{ROOT}/attendance/summary?date={today}")["present"] == 1

invoice = request(f"{ROOT}/fees/invoices", "POST", {
    "studentId": STUDENT, "invoiceNumber": "CI-WORKFLOW-1", "dueDate": today, "amount": "10.00"
}, expected=201)
payments_path = f"{ROOT}/fees/invoices/{invoice['id']}/payments"
assert request(payments_path)["content"] == []
request(payments_path, "POST", {"paymentReference": "CI-PAY-1", "amount": "4.00"}, expected=201)
request(payments_path, "POST", {"paymentReference": "CI-PAY-1", "amount": "1.00"}, expected=409)
request(payments_path, "POST", {"paymentReference": "CI-OVERPAY", "amount": "7.00"}, expected=400)
assert len(request(payments_path)["content"]) == 1
request(payments_path, "POST", {"paymentReference": "CI-PAY-2", "amount": "6.00"}, expected=201)
assert request(f"{ROOT}/fees/invoices?status=PAID")["content"][0]["id"] == invoice["id"]

boundary = "amss-" + str(uuid4())
document_bytes = b"AMSS disposable CI document."
multipart = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"title\"\r\n\r\nCI document\r\n"
             f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"ci.txt\"\r\n"
             "Content-Type: text/plain\r\n\r\n").encode() + document_bytes + f"\r\n--{boundary}--\r\n".encode()
document = request(f"{ROOT}/documents/upload", "POST", multipart, expected=201,
                   content_type=f"multipart/form-data; boundary={boundary}")
assert document["status"] == "DRAFT"
request(f"{ROOT}/documents/{document['id']}/publish", "POST")
assert request(f"{ROOT}/documents/{document['id']}/content", raw=True) == document_bytes
request(f"{ROOT}/documents/{document['id']}/archive", "POST")
request(f"{ROOT}/documents/{document['id']}/content", expected=404)
print("School reads, tenant boundary, attendance save, payment safeguards and document lifecycle passed under 512 MiB.")

# Provision a clean real tenant and ensure demo identities cannot access it or reuse old tokens.
profile = request(f"{ROOT}/setup/profile")
assert profile["initialSetupAvailable"]
real = request(f"{ROOT}/setup/production-school", "POST", {
    "schoolName": "CI Production School", "timezone": "Asia/Kolkata", "username": "ci-real-admin",
    "email": "admin@example.com", "displayName": "School Administrator", "password": "ci-real-password-123"
})
request(f"{ROOT}/students", expected=401)
TOKEN = ""
request("/api/v1/auth/login", "POST", {"username": "demo-admin", "password": "ci-demo-password-123"}, expected=401)
TOKEN = request("/api/v1/auth/login", "POST", {"username": "ci-real-admin", "password": "ci-real-password-123"})["accessToken"]
ROOT = f"/api/v1/schools/{real['schoolId']}"
assert request(f"{ROOT}/students")["content"] == []
assert not request(f"{ROOT}/setup/profile")["initialSetupAvailable"]
request(f"/api/v1/schools/{SCHOOL}/students", expected=403)
student = request(f"{ROOT}/students", "POST", {"admissionNumber": "REAL-001", "firstName": "Real", "lastName": "Student", "dateOfBirth": "2018-04-10"})
request(f"{ROOT}/students", "POST", {"admissionNumber": "REAL-001", "firstName": "Duplicate"}, expected=409)
teacher = request(f"{ROOT}/setup/users", "POST", {"username": "ci-real-teacher", "email": "teacher@example.com", "displayName": "Teacher", "password": "ci-teacher-password-123", "role": "TEACHER"})
request(f"{ROOT}/setup/users", "POST", {"username": "ci-super-user", "email": "super@example.com", "displayName": "Escalation", "password": "ci-super-password-123", "role": "SUPER_ADMIN"}, expected=400)
admin_token=TOKEN
TOKEN=""
TOKEN=request("/api/v1/auth/login", "POST", {"username":"ci-real-teacher","password":"ci-teacher-password-123"})["accessToken"]
request(f"{ROOT}/setup/users", expected=403)
assert request(f"{ROOT}/students")["content"][0]["id"]==student["id"]
teacher_token=TOKEN
TOKEN=admin_token
request(f"{ROOT}/setup/users/{teacher['id']}/status?active=false", "PATCH", expected=200)
TOKEN=teacher_token
request(f"{ROOT}/students", expected=401)
print("Real school onboarding, clean data isolation, duplicate rejection, role protection and token revocation passed.")

# Exercise the full academic/staff setup used by the real-school forms.
TOKEN=admin_token
request(f"{ROOT}/setup/users", "POST", {"username":"ci-long-password","email":"long@example.com","displayName":"Invalid password","password":"x"*73,"role":"TEACHER"}, expected=400)
year=request(f"{ROOT}/academic-years", "POST", {"code":"CI-2026","name":"CI Academic Year","startsOn":"2026-01-01","endsOn":"2026-12-31"})
request(f"{ROOT}/academic-years/{year['id']}/status?active=true", "PATCH")
classroom=request(f"{ROOT}/classes", "POST", {"academicYearId":year['id'],"name":"Class 1","gradeLevel":1})
section=request(f"{ROOT}/classes/{classroom['id']}/sections", "POST", {"name":"A","room":"101"},expected=201)
subject=request(f"{ROOT}/academics/subjects", "POST", {"code":"MATH","name":"Mathematics"})
request(f"{ROOT}/academics/classes/{classroom['id']}/subjects/{subject['id']}","POST")
request(f"{ROOT}/enrollments/sections/{section['id']}/students", "POST", {"studentId":student['id'],"enrolledOn":today},expected=201)
period={"subjectId":subject['id'],"dayOfWeek":"MONDAY","startsAt":"09:00","endsAt":"10:00","room":"101"}
request(f"{ROOT}/timetable/sections/{section['id']}","POST",period)
request(f"{ROOT}/timetable/sections/{section['id']}","POST",period,expected=409)
assert len(request(f"{ROOT}/timetable/sections/{section['id']}")["content"])==1
assessment=request(f"{ROOT}/assessments","POST",{"academicYearId":year['id'],"classId":classroom['id'],"name":"Math Test","assessmentDate":today,"maxMarks":"100"},expected=201)
request(f"{ROOT}/assessments/{assessment['id']}/results/bulk","POST",{"entries":[{"studentId":student['id'],"marks":"85","grade":"A"}]})
assert str(request(f"{ROOT}/assessments/{assessment['id']}/results")[0]["marks"]).startswith("85")
request(f"{ROOT}/assessments/report-cards/{student['id']}?academicYearId={year['id']}")
member=request(f"{ROOT}/staff","POST",{"employeeCode":"CI-STF-1","fullName":"CI Teacher","designation":"Teacher","employmentType":"FULL_TIME","joinedOn":today})
assert request(f"{ROOT}/staff")["content"][0]["id"]==member["id"]
assert request(f"{ROOT}/staff?q=Teacher")["content"][0]["id"]==member["id"]
leave=request(f"{ROOT}/staff/{member['id']}/leave","POST",{"leaveType":"CASUAL","startsOn":today,"endsOn":today,"reason":"CI workflow"})
assert request(f"{ROOT}/staff/leave/{leave['id']}/review?status=APPROVED","POST")["status"]=="APPROVED"
request(f"{ROOT}/staff/leave/{leave['id']}/review?status=REJECTED","POST",expected=409)
print("Real school academic setup, enrollment, timetable collision, assessment results/report card and staff leave workflows passed.")

# Security regression: a real-school publisher cannot rebind the old school's file.
TOKEN=admin_token
request(f"{ROOT}/documents", "POST", {"title":"Foreign file","fileName":"ci.txt","contentType":"text/plain","storageKey":document["storageKey"]}, expected=400)
request(f"{ROOT}/documents", "POST", {"title":"Foreign alias","fileName":"ci.txt","contentType":"text/plain","storageKey":document["storageKey"].replace("/","//",1)}, expected=400)
parent=request(f"{ROOT}/setup/users", "POST", {"username":"ci-qa-parent","email":"parent@example.com","displayName":"Parent","password":"ci-parent-password-123","role":"PARENT"})
TOKEN=""
TOKEN=request("/api/v1/auth/login", "POST", {"username":"ci-qa-parent","password":"ci-parent-password-123"})["accessToken"]
request(f"{ROOT}/documents?publishedOnly=false", expected=403)
request(f"{ROOT}/documents?publishedOnly=true")
request("/actuator/prometheus", expected=403)
TOKEN=""
request("/actuator/prometheus", expected=401)
request("/api/v1/auth/login", "POST", {"username":"ci-qa-parent","password":"x"*100}, expected=400)
for i in range(11):
    request("/api/v1/auth/login", "POST", {"username":"ci-throttle-unknown","password":"wrong-password"}, expected=401 if i<10 else 429)
TOKEN=admin_token
print("Document-key isolation, draft metadata protection, private metrics, bounded login inputs and login throttling passed.")

TOKEN=admin_token
private_doc=request(f"{ROOT}/documents", "POST", {"title":"Private draft","fileName":"private.txt","contentType":"text/plain","storageKey":real['schoolId']+"/qa/private.txt","audienceRole":"SCHOOL_ADMIN"}, expected=201)
qa_teacher=request(f"{ROOT}/setup/users", "POST", {"username":"ci-doc-teacher","email":"docteacher@example.com","displayName":"Teacher","password":"ci-doc-password-123","role":"TEACHER"})
TOKEN=""
TOKEN=request("/api/v1/auth/login", "POST", {"username":"ci-doc-teacher","password":"ci-doc-password-123"})["accessToken"]
assert private_doc['id'] not in [d['id'] for d in request(f"{ROOT}/documents?publishedOnly=false")]
request(f"{ROOT}/documents/{private_doc['id']}/publish", "POST", expected=404)
print("Teacher draft metadata and publishing respect document ownership/audience.")
