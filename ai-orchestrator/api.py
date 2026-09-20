"""Stable HTTP contract for the EduSphere AI orchestration service."""

from __future__ import annotations

from typing import Annotated
from uuid import UUID

from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

from agent import graph

app = FastAPI(title="EduSphere AI Orchestrator", version="1.0")


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


@app.post("/internal/v1/ai/ask", response_model=AiAskResponse)
async def ask(
    request: AiAskRequest,
    x_request_id: Annotated[str, Header(alias="X-Request-ID")],
    x_school_id: Annotated[UUID, Header(alias="X-School-ID")],
    x_user_id: Annotated[UUID, Header(alias="X-User-ID")],
    x_user_roles: Annotated[str, Header(alias="X-User-Roles")],
) -> AiAskResponse:
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
    return AiAskResponse(
        requestId=request.requestId,
        answer=result["answer"],
        citations=[],
    )
