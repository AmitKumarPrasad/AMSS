# Marketplace Quality & Distribution Guide

AMSS is distributed as source code under a commercial agreement. This document defines the clean packaging boundary for marketplace releases and links the evidence buyers and automated quality checks need.

## Product differentiation

AMSS is not a generic CRUD starter. Its implemented school-management workflows include tenant- and role-scoped RAG retrieval, MCP-controlled school tools, LangGraph orchestration, financial idempotency controls, audit trails, and atomic multi-instance notification claiming. See [ARCHITECTURE.md](../ARCHITECTURE.md), [DESIGN.md](../DESIGN.md), and the API developer guide for implementation details.

## Documentation map

- [README](../README.md): product overview, modules, configuration and deployment baseline.
- [Architecture](../ARCHITECTURE.md): system boundaries and component topology.
- [Design](../DESIGN.md): implementation design decisions.
- [API developer guide](API_DEVELOPER_GUIDE.md): REST API usage and development guidance.
- [Release readiness](RELEASE_READINESS.md): repository and launch readiness.
- [PostgreSQL backup/restore](POSTGRES_BACKUP_RESTORE.md): database recovery procedures.
- [Runbook](../RUNBOOK.md): deployment, rollback, operations and incident response.

## Marketplace package hygiene

Build marketplace archives from tracked source files only. Do not include Git metadata, IDE state, build output, caches, local databases, runtime uploads, populated environment files, secrets, or credentials.

The repository ignores `*.sqlite`, `*.sqlite3`, and `*.db`. A marketplace archive containing one of those files was produced outside the tracked source tree and should be regenerated from a clean checkout.

Recommended archive command:

```bash
git archive --format=zip --output=amss-source.zip HEAD
```

Before uploading, inspect the archive and confirm it contains no local database or secret-bearing files.

## Test and coverage evidence

Java tests run in CI through Maven. JaCoCo XML and HTML reports are generated during the test lifecycle. The repository carries a machine-readable marketplace snapshot at `coverage/jacoco.xml` plus a human-readable summary at `coverage/README.md`; CI also uploads the freshly generated report as an artifact. This lets repository-snapshot scanners detect measured coverage directly.

Coverage is evidence, not a substitute for passing tests. CI must remain green before a release is packaged.

## Third-party software

AMSS uses third-party open-source dependencies declared by Maven and Python packaging metadata. Those components remain subject to their respective licenses. Commercial distribution of AMSS does not relicense third-party components.

Review dependency licenses as part of each release and preserve required notices/attribution when redistributing dependencies.
