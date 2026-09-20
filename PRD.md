# AMSS Product Requirements Document

## Product
AMSS (AI-enabled School Management System) / EduSphere AI.

## Vision
Provide a secure, multi-tenant school platform that manages core academic and administrative workflows while exposing AI capabilities through a dedicated Python AI orchestrator.

## Primary Users
- Platform administrators
- School administrators
- Teachers
- Students
- Parents/guardians

## Core Capabilities
1. Tenant/school isolation.
2. Authentication and authorization.
3. User and role management.
4. Student and teacher management.
5. Academic year, class and section management.
6. Attendance.
7. Assessments and results.
8. Fees/payments.
9. Notices and school communication.
10. AI-assisted workflows through the Python orchestrator.
11. REST APIs suitable for web/mobile clients.
12. Containerized local development and CI validation.

## Non-functional Requirements
- Tenant data must never cross school boundaries.
- APIs must validate input and return consistent HTTP semantics.
- Backend builds must be reproducible through Maven.
- AI integration must remain separately deployable from the Java backend.
- Secrets must be supplied through environment/configuration, never committed.
- Every implementation batch must preserve a green CI pipeline.

## Definition of Done
A feature is complete when its code, validation, tests where applicable, API behavior, documentation, and CI status are consistent with this document.
