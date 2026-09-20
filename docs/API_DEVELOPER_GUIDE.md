# AMSS API Developer Documentation

## 1. Interactive API documentation

When the backend is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

Swagger/OpenAPI is generated from the actual Spring controllers and request DTO/record validation constraints, so it remains the runtime source of truth.

## 2. Authentication

1. Call `POST {{baseUrl}}/api/v1/auth/login`.
2. Copy `accessToken` from the response.
3. In Swagger UI use **Authorize** and enter `Bearer <accessToken>`.
4. In Postman the collection uses the `accessToken` environment variable.

Login body:

```json
{
  "username": "schooladmin",
  "password": "your-password"
}
```

Response:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

All school APIs require a JWT whose `school_id` matches the `schoolId` path variable. Never put a real production JWT, password, OpenAI key, database password, or other secret into the repository.

## 3. Common variables

Postman environment:

- `baseUrl`: `http://localhost:8080`
- `schoolId`: UUID of the test school
- `studentId`: UUID of a student
- `staffId`: UUID of a staff member
- `academicYearId`: UUID
- `classId`: UUID
- `sectionId`: UUID
- `subjectId`: UUID
- `invoiceId`: UUID
- `conversationId`: UUID
- `accessToken`: JWT returned by login

## 4. Request body cookbook

### Student
`POST /api/v1/schools/{schoolId}/students`

```json
{
  "admissionNumber": "ADM-2026-001",
  "firstName": "Aarav",
  "lastName": "Kumar",
  "dateOfBirth": "2015-06-20"
}
```

### Staff
`POST /api/v1/schools/{schoolId}/staff`

```json
{
  "employeeCode": "EMP-001",
  "fullName": "Priya Sharma",
  "email": "priya@example.com",
  "phone": "9876543210",
  "designation": "Teacher",
  "employmentType": "FULL_TIME",
  "joinedOn": "2026-04-15"
}
```

### Staff leave
`POST /api/v1/schools/{schoolId}/staff/{staffId}/leave`

```json
{
  "leaveType": "CASUAL",
  "startsOn": "2026-09-21",
  "endsOn": "2026-09-22",
  "reason": "Personal work"
}
```

### Academic year
`POST /api/v1/schools/{schoolId}/academic-years`

```json
{
  "code": "2026-27",
  "name": "Academic Year 2026-27",
  "startsOn": "2026-04-01",
  "endsOn": "2027-03-31"
}
```

### Subject
`POST /api/v1/schools/{schoolId}/academics/subjects`

```json
{
  "code": "MATH",
  "name": "Mathematics"
}
```

### Fee invoice
`POST /api/v1/schools/{schoolId}/fees/invoices`

```json
{
  "studentId": "{{studentId}}",
  "invoiceNumber": "INV-2026-001",
  "dueDate": "2026-10-10",
  "amount": 12500.00
}
```

### Fee payment
`POST /api/v1/schools/{schoolId}/fees/invoices/{invoiceId}/payments`

```json
{
  "paymentReference": "PAY-2026-0001",
  "amount": 5000.00
}
```

### AI conversation
`POST /api/v1/schools/{schoolId}/ai/conversations`

```json
{
  "title": "Fee discussion"
}
```

### Rename AI conversation
`PATCH /api/v1/schools/{schoolId}/ai/conversations/{conversationId}`

```json
{
  "title": "Fee discussion - September"
}
```

### AI question
`POST /api/v1/schools/{schoolId}/ai/ask`

```json
{
  "question": "What is the fee status for my student?",
  "conversationId": "{{conversationId}}"
}
```

### RAG document indexing
`POST /api/v1/schools/{schoolId}/ai/documents/{documentId}/index`

```json
{
  "content": "School policy: students must maintain 75 percent attendance."
}
```

## 5. Pagination

List endpoints use zero-based pagination unless documented otherwise:

`?page=0&size=50`

Do not request unbounded pages. The backend enforces bounded page sizes on protected list APIs.

Typical filters:

- `status`
- `q`
- `page`
- `size`

## 6. Important headers

```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
X-Request-ID: <UUID>
```

`X-Request-ID` is especially important for AI requests. It is used for request correlation/idempotency.

## 7. Expected error handling

The API uses a consistent error response through `ApiExceptionHandler`. When debugging, capture:

- HTTP status
- response body
- `X-Request-ID` if present
- endpoint and HTTP method
- schoolId
- authenticated role

Never log passwords or bearer tokens.

## 8. Recommended onboarding sequence for a new developer

1. Start PostgreSQL/Redis and the backend.
2. Open Swagger UI.
3. Create/login with a development user.
4. Authorize Swagger with the returned JWT.
5. Create an academic year.
6. Create a class/section/subject.
7. Create a student and guardian.
8. Create staff and attendance data.
9. Create an invoice and payment.
10. Test assessments/report cards.
11. Test notifications/documents/communications.
12. Create an AI conversation and call `/ai/ask`.
13. Use the Postman collection for repeatable API regression testing.

## 9. Source of truth

Do not manually guess request fields when implementing a new client. Use:

`/v3/api-docs`

The generated OpenAPI document reflects the current controller signatures, validation annotations, parameters and request schemas. Update the Postman examples when an API contract intentionally changes.
