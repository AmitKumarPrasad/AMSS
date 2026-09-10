"""EduSphere LangGraph orchestration layer.

The graph consumes school capabilities exposed by the Spring Boot MCP server.
Deployment platforms that expose LangGraph graphs through MCP can then make this
workflow available to other MCP-compatible clients as well.
"""

import os
from typing import Any

from langchain_mcp_adapters.client import MultiServerMCPClient
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

SPRING_MCP_URL = os.getenv("SPRING_MCP_URL", "http://localhost:8080/mcp")
MODEL = os.getenv("OPENAI_CHAT_MODEL", "gpt-5.6")


def build_graph():
    client = MultiServerMCPClient(
        {
            "school": {
                "transport": "streamable_http",
                "url": SPRING_MCP_URL,
            }
        }
    )

    async def load():
        tools = await client.get_tools()
        model = ChatOpenAI(model=MODEL, temperature=0)
        return create_react_agent(
            model,
            tools,
            prompt=(
                "You are the EduSphere orchestration agent. "
                "Respect tenant and role boundaries. Use MCP tools for live school data. "
                "For policy/document questions, call the Spring RAG API through a dedicated "
                "integration in the application layer rather than inventing policy. "
                "For side-effecting operations, require an explicit approval step."
            ),
        )

    return load


async def invoke(message: str) -> Any:
    graph = await build_graph()()
    return await graph.ainvoke({"messages": [{"role": "user", "content": message}]})
