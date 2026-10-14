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
    headers = {"Authorization": f"Bearer {TOKEN}", "X-Request-ID": str(uuid4()), "Content-Type": content_type}
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
