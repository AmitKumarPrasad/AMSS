#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${1:?Usage: $0 <gcp-project-id>}"
REGION="${REGION:-asia-south1}"
REPO_OWNER="AmitKumarPrasad"
REPO_NAME="AMSS"
POOL_ID="github-pool"
PROVIDER_ID="github-provider"
SA_NAME="amss-deployer"

gcloud config set project "$PROJECT_ID"

gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  iamcredentials.googleapis.com \
  sts.googleapis.com

gcloud iam service-accounts describe "$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com" >/dev/null 2>&1 || \
  gcloud iam service-accounts create "$SA_NAME" --display-name="AMSS GitHub deployer"

for ROLE in roles/run.admin roles/cloudbuild.builds.editor roles/artifactregistry.admin roles/iam.serviceAccountUser; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="$ROLE" >/dev/null
done

gcloud iam workload-identity-pools describe "$POOL_ID" --location=global >/dev/null 2>&1 || \
  gcloud iam workload-identity-pools create "$POOL_ID" \
    --location=global \
    --display-name="GitHub Actions"

POOL_NAME="$(gcloud iam workload-identity-pools describe "$POOL_ID" --location=global --format='value(name)')"

gcloud iam workload-identity-pools providers describe "$PROVIDER_ID" \
  --workload-identity-pool="$POOL_ID" --location=global >/dev/null 2>&1 || \
  gcloud iam workload-identity-pools providers create-oidc "$PROVIDER_ID" \
    --workload-identity-pool="$POOL_ID" \
    --location=global \
    --issuer-uri="https://token.actions.githubusercontent.com" \
    --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
    --attribute-condition="assertion.repository=='$REPO_OWNER/$REPO_NAME'"

gcloud iam service-accounts add-iam-policy-binding \
  "$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/$POOL_NAME/attribute.repository/$REPO_OWNER/$REPO_NAME" >/dev/null

PROVIDER_NAME="$(gcloud iam workload-identity-pools providers describe "$PROVIDER_ID" \
  --workload-identity-pool="$POOL_ID" --location=global --format='value(name)')"

cat <<EOF
Set these GitHub repository variables in AmitKumarPrasad/AMSS:

GCP_PROJECT_ID=$PROJECT_ID
GCP_WIF_PROVIDER=$PROVIDER_NAME
GCP_SERVICE_ACCOUNT=$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com
FRONTEND_ORIGIN=https://amitkumarprasad.github.io

Cloud Run region: $REGION
EOF
