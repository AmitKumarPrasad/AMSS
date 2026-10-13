# Free-tier deployment

Target stack:

- Frontend: GitHub Pages
- Backend: Google Cloud Run
- AI orchestrator: Google Cloud Run
- PostgreSQL: Neon or another free PostgreSQL provider
- Redis: disabled for the first free-tier deployment

## Required GitHub repository variables

- `GCP_PROJECT_ID`
- `GCP_WIF_PROVIDER`
- `GCP_SERVICE_ACCOUNT`
- `FRONTEND_ORIGIN` (for example `https://amitkumarprasad.github.io`)

## Required GitHub repository secrets

- `DB_URL` as a JDBC PostgreSQL URL
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (32+ random bytes)
- `APP_DEMO_PASSWORD` (12+ characters)
- `AI_ORCHESTRATOR_SERVICE_TOKEN`
- `OPENAI_API_KEY`

If you want a strictly zero-cost launch, set a non-empty placeholder OpenAI key and do not use AI features until you add real API credit.

## Google Cloud prerequisite

Configure Workload Identity Federation for GitHub and grant the deployment service account access to deploy Cloud Run services and build from source.

The workflow is manual (`workflow_dispatch`) so it will not attempt a deployment until the account-level variables and secrets are configured.
