"""EduSphere LangGraph orchestration layer."""

import os
from typing import TypedDict

from langchain_mcp_adapters.client import MultiServerMCPClient
from langchain_openai import ChatOpenAI
from langgraph.graph import END, START, StateGraph
from langgraph.prebuilt import create_react_agent

SPRING_MCP_URL = os.getenv("SPRING_MCP_URL", "http://localhost:8080/mcp")
MODEL = os.getenv("OPENAI_CHAT_MODEL", "gpt-5.6")


class AgentState(TypedDict):
    message: str
    answer: str


async def orchestration_node(state: AgentState):
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
