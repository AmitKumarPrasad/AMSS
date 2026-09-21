# Kubernetes deployment baseline

This directory provides a deployment baseline for a Kubernetes environment. It is intentionally not a substitute for a managed PostgreSQL/Redis service, a cloud secret manager, an ingress/TLS controller, monitoring, or disaster recovery.

## Prerequisites

- Kubernetes 1.29+ (or a compatible current cluster)
- PersistentVolume support
- A container registry or image import containing `amss:production`
- A secret manager or sealed/encrypted Secret workflow

## Required secrets

Populate `edusphere-secrets` from your secret manager. Do not commit real values.

- `DB_PASSWORD`
- `OPENAI_API_KEY`
- `JWT_SECRET` (at least 32 random characters)
- `JWT_ISSUER` (public HTTPS issuer)
- `LANGGRAPH_BASE_URL`

The committed `secrets.example.yaml` contains placeholders only.

## Validate and render

```bash
kubectl kustomize k8s > /tmp/amss-k8s.yaml
kubectl apply --dry-run=client --validate=false -f /tmp/amss-k8s.yaml
```

## Deploy

After replacing the example Secret with your secret-manager-backed resource:

```bash
kubectl apply -k k8s/
kubectl -n edusphere rollout status deployment/app
kubectl -n edusphere get pods,svc
```

Before external traffic, verify:

- application liveness and readiness
- authentication
- tenant isolation
- PostgreSQL/Redis health
- document storage
- AI orchestrator connectivity

For production, use managed PostgreSQL/Redis where appropriate and put the application behind an ingress/load balancer with TLS.
