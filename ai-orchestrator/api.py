"""Private FastAPI contract for the EduSphere AI orchestrator."""

from __future__ import annotations

import hmac
import os
from typing import Annotated
from uuid import UUID

from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

from agent import graph

app = FastAPI(title="EduSphere AI Orchestrator", version="1.1")


class AiAskRequest(BaseModel):
    requestId: UUID
    schoolId: UUID
    userId: UUID
    roles: list[str] = Field(min_length=1, max_length=20)
    message: str = Field(min_length=1, max_length=2000)
    conversationId: str | None = Field(default=None, max_length=128)


class AiAskResponse(BaseModel):
    requestId: UUID
    answer: str
    citations: list[str] = Field(default_factory=list)
    status: str = "COMPLETED"


def _validate_service_token(authorization: str | None) -> None:
    expected = os.getenv("AI_ORCHESTRATOR_SERVICE_TOKEN", "").strip()
    supplied = (authorization or "").strip()
    if not expected:
        raise HTTPException(status_code=503, detail="orchestrator authentication is not configured")
    if not supplied.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="invalid service authentication")
    token = supplied[7:].strip()
    if not token or not hmac.compare_digest(token, expected):
        raise HTTPException(status_code=401, detail="invalid service authentication")


@app.post("/internal/v1/ai/ask", response_model=AiAskResponse)
async def ask(
    request: AiAskRequest,
    authorization: Annotated[str | None, Header()] = None,
    x_request_id: Annotated[str, Header(alias="X-Request-ID")] = "",
    x_school_id: Annotated[UUID, Header(alias="X-School-ID")] = None,
    x_user_id: Annotated[UUID, Header(alias="X-User-ID")] = None,
    x_user_roles: Annotated[str, Header(alias="X-User-Roles")] = "",
) -> AiAskResponse:
    _validate_service_token(authorization)
    if request.schoolId != x_school_id or request.userId != x_user_id:
        raise HTTPException(status_code=400, detail="request context does not match trusted headers")

    header_roles = [role.strip() for role in x_user_roles.split(",") if role.strip()]
    if sorted(header_roles) != sorted(request.roles):
        raise HTTPException(status_code=400, detail="request roles do not match trusted headers")

    try:
        UUID(x_request_id)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail="X-Request-ID must be a UUID") from exc

    result = await graph.ainvoke({
        "message": request.message,
        "request_id": x_request_id,
        "school_id": str(request.schoolId),
        "user_id": str(request.userId),
        "roles": request.roles,
        "answer": "",
    })
    return AiAskResponse(requestId=request.requestId, answer=result["answer"], citations=[])


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}
