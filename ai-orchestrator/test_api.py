import unittest
from unittest.mock import AsyncMock, patch
from uuid import uuid4

from fastapi.testclient import TestClient

from api import app


class ApiContractTests(unittest.TestCase):
    def test_rejects_mismatched_tenant_context(self):
        request_id = uuid4()
        school_id = uuid4()
        user_id = uuid4()
        with TestClient(app) as client:
            response = client.post(
                "/internal/v1/ai/ask",
                headers={
                    "X-Request-ID": str(request_id),
                    "X-School-ID": str(uuid4()),
                    "X-User-ID": str(user_id),
                    "X-User-Roles": "ROLE_TEACHER",
                },
                json={
                    "requestId": str(request_id),
                    "schoolId": str(school_id),
                    "userId": str(user_id),
                    "roles": ["ROLE_TEACHER"],
                    "message": "hello",
                },
            )
        self.assertEqual(response.status_code, 400)

    @patch("api.graph.ainvoke", new_callable=AsyncMock)
    def test_returns_stable_response_contract(self, invoke):
        request_id = uuid4()
        school_id = uuid4()
        user_id = uuid4()
        invoke.return_value = {"answer": "Attendance policy found."}
        with TestClient(app) as client:
            response = client.post(
                "/internal/v1/ai/ask",
                headers={
                    "X-Request-ID": str(request_id),
                    "X-School-ID": str(school_id),
                    "X-User-ID": str(user_id),
                    "X-User-Roles": "ROLE_TEACHER",
                },
                json={
                    "requestId": str(request_id),
                    "schoolId": str(school_id),
                    "userId": str(user_id),
                    "roles": ["ROLE_TEACHER"],
                    "message": "hello",
                },
            )
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["requestId"], str(request_id))
        self.assertEqual(response.json()["answer"], "Attendance policy found.")
        self.assertEqual(response.json()["status"], "COMPLETED")
        invoke.assert_awaited_once()


if __name__ == "__main__":
    unittest.main()
