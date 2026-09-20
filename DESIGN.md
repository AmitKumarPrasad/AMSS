# AMSS Technical Design

## Module Design
Each backend feature follows:

Controller -> Service -> Repository -> Entity

DTOs/views are used at API boundaries where appropriate.

## Assessment Lifecycle
Assessments are school-scoped and support controlled status changes. The service layer owns validation and persistence of lifecycle transitions.

## Security Design
Authentication establishes the caller identity. Authorization and tenant checks must be applied before accessing school-owned resources.

## Error Handling
Invalid input should produce a client error. Missing school-scoped resources should produce not-found semantics. Unexpected failures must not expose internal implementation details.

## AI Design
The Python orchestrator owns:
- prompt orchestration
- model-provider integration
- agent/tool execution
- retrieval workflows
- AI-specific observability

The Java backend owns:
- authoritative school data
- transactional business rules
- authorization
- persistence
- stable domain APIs

## Extensibility
New modules should follow the same controller/service/repository boundaries and reuse established security, validation, and error-handling patterns.
