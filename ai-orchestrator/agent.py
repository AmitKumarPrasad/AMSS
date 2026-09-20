"""EduSphere LangGraph orchestration layer."""

import os
from typing import TypedDict
from urllib.parse import urlparse

from langchain_mcp_adapters.client import MultiServerMCPClient
from langchain_openai import ChatOpenAI
from langgraph.graph import END, START, StateGraph
from langgraph.prebuilt import create_react_agent

SPRING_MCP_URL = os.getenv("SPRING_MCP_URL", "http://localhost:8080/mcp")
MODEL = os.getenv("OPENAI_CHAT_MODEL", "gpt-5.6")


class AgentState(TypedDict):
    message: str
    answer: str


def validate_configuration() -> None:
    api_key = os.getenv("OPENAI_API_KEY", "").strip()
    if not api_key:
        raise RuntimeError("OPENAI_API_KEY is required for the AI orchestrator")

    parsed = urlparse(SPRING_MCP_URL)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise RuntimeError("SPRING_MCP_URL must be a valid HTTP(S) URL")

    insecure_allowed = os.getenv("ALLOW_INSECURE_MCP_HTTP", "false").strip().lower() == "true"
    local_host = parsed.hostname in {"localhost", "127.0.0.1", "::1"}
    if parsed.scheme == "http" and not local_host and not insecure_allowed:
        raise RuntimeError(
            "SPRING_MCP_URL must use HTTPS outside localhost; "
            "set ALLOW_INSECURE_MCP_HTTP=true only for explicitly trusted development environments"
        )

    if not MODEL.strip():
        raise RuntimeError("OPENAI_CHAT_MODEL must not be blank")


async def orchestration_node(state: AgentState):
    validate_configuration()
    client = MultiServerMCPClient({
        "school": {"transport": "streamable_http", "url": SPRING_MCP_URL}
    })
    tools = await client.get_tools()
    model = ChatOpenAI(model=MODEL, temperature=0)
    agent = create_react_agent(
        model,
        tools,
        prompt=(
            "You are the EduSphere orchestration agent. Respect tenant and role boundaries. "
            "Use MCP tools for live school data. Never fabricate records. "
            "For policy/document questions, rely on retrieved evidence. "
            "For side-effecting operations, require an explicit approval step."
        ),
    )
    result = await agent.ainvoke({"messages": [{"role": "user", "content": state["message"]}]})
    return {"answer": result["messages"][-1].content}


builder = StateGraph(AgentState, input_schema=AgentState, output_schema=AgentState)
builder.add_node("orchestrate", orchestration_node)
builder.add_edge(START, "orchestrate")
builder.add_edge("orchestrate", END)
graph = builder.compile()
