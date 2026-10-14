# AMSS Commercial Selling Guide

## Product
**AMSS / EduSphere AI — AI-Powered School Management Platform Backend**

AMSS is a production-oriented school management backend built around Spring Boot and a modular/microservice architecture. It provides school-domain APIs, authentication and authorization, tenant isolation, academic workflows, attendance, assessments, fees, communication, documents, notifications, audit/operations capabilities and AI/RAG/MCP integration.

## Recommended pricing
| Tier | Price | Intended buyer |
|---|---:|---|
| Starter / Source | $699 | Developer or small software company |
| Professional | $999 | Company deploying for one school/organization |
| Business | $1,499+ | Company needing implementation/customization |
| Exclusive | $3,000–$5,000+ | Buyer requesting negotiated exclusivity |

These are suggested launch prices. Adjust based on support scope, customization, exclusivity and buyer requirements.

## What the buyer receives
- Complete backend source code for the purchased release
- Spring Boot REST APIs
- JWT authentication and role-based authorization
- Multi-tenant school/domain model
- Student and guardian workflows
- Academic years, classes, sections and subjects
- Attendance workflows
- Assessments, results and report-card APIs
- Fees, payments and balance workflows
- Announcements and audience targeting
- Document registration and storage abstraction
- Notifications and delivery workflows
- Audit and operational endpoints
- AI assistant foundation
- RAG/vector-search integration
- MCP/tool integration
- LangGraph integration points
- PostgreSQL + pgvector
- Redis
- Flyway migrations
- Docker Compose development and production baselines
- Health/readiness endpoints
- CI verification
- Deployment and operations documentation

## Not included by default
- Cloud hosting fees
- OpenAI/model API usage
- PostgreSQL/Redis managed-service fees
- Email/SMS provider fees
- Custom frontend development
- Customer-specific integrations
- Long-term maintenance
- 24/7 support or SLA
- Exclusive ownership unless separately contracted

## Sales positioning
Do not market the product as "just Java source code".

Position it as:

> A production-oriented AI school management backend that gives a software company a ready foundation for launching a school ERP/SIS product without building the core domain, security, AI and operational workflows from zero.

## Delivery checklist
- [ ] Remove secrets and credentials.
- [ ] Verify .env files are not committed.
- [ ] Verify production secrets are placeholders only.
- [ ] Review Git history for accidentally committed credentials.
- [ ] Run CI.
- [ ] Verify build and tests.
- [ ] Verify Docker/Compose configuration.
- [ ] Verify database migration instructions.
- [ ] Verify deployment documentation.
- [ ] Provide license and invoice/order terms.
- [ ] Create a release/tag for the sold version.
- [ ] Keep the master repository private.
- [ ] Deliver only the licensed release or a separate customer repository.

## Recommended commercial workflow
1. Buyer reviews product documentation/demo.
2. Buyer selects license tier.
3. Written order/license terms are accepted.
4. Payment is received.
5. A release/tag is created.
6. Customer repository/package is prepared.
7. Credentials and secrets are excluded.
8. Source package is delivered.
9. Deployment assistance is provided if included.
10. Support period starts from delivery/deployment date.

Keep the development repository private. For each commercial customer, prefer a clean customer-specific repository or release package instead of giving unrestricted access to the main development repository.
