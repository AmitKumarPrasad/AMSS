import os
import unittest
from unittest.mock import AsyncMock, patch
from uuid import uuid4

from fastapi.testclient import TestClient

from api import app


class ApiAuthenticationTests(unittest.TestCase):
    def setUp(self):
        self.previous = os.environ.get("AI_ORCHESTRATOR_SERVICE_TOKEN")
        os.environ["AI_ORCHESTRATOR_SERVICE_TOKEN"] = "test-service-token"

    def tearDown(self):
        if self.previous is None:
            os.environ.pop("AI_ORCHESTRATOR_SERVICE_TOKEN", None)
        else:
            os.environ["AI_ORCHESTRATOR_SERVICE_TOKEN"] = self.previous

    def payload(self):
        return {
            "requestId": str(uuid4()),
            "schoolId": str(uuid4()),
            "userId": str(uuid4()),
            "roles": ["ROLE_TEACHER"],
            "message": "hello",
        }

    def headers(self, payload, token="test-service-token"):
        return {
            "Authorization": f"Bearer {token}",
            "X-Request-ID": payload["requestId"],
            "X-School-ID": payload["schoolId"],
            "X-User-ID": payload["userId"],
            "X-User-Roles": "ROLE_TEACHER",
        }

    def test_rejects_invalid_service_token(self):
        payload = self.payload()
        with TestClient(app) as client:
            response = client.post("/internal/v1/ai/ask", headers=self.headers(payload, "wrong"), json=payload)
        self.assertEqual(response.status_code, 401)

    @patch("api.graph.ainvoke", new_callable=AsyncMock)
    def test_accepts_valid_service_token(self, invoke):
        invoke.return_value = {"answer": "ok"}
        payload = self.payload()
        with TestClient(app) as client:
            response = client.post("/internal/v1/ai/ask", headers=self.headers(payload), json=payload)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["answer"], "ok")

    def test_fails_closed_when_token_is_not_configured(self):
        os.environ.pop("AI_ORCHESTRATOR_SERVICE_TOKEN", None)
        payload = self.payload()
        with TestClient(app) as client:
            response = client.post("/internal/v1/ai/ask", headers=self.headers(payload), json=payload)
        self.assertEqual(response.status_code, 503)


if __name__ == "__main__":
    unittest.main()
