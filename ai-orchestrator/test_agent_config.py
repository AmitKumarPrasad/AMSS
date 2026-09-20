import os
import unittest
from unittest.mock import patch

from agent import validate_configuration


class ConfigurationTests(unittest.TestCase):
    def test_requires_openai_api_key(self):
        with patch.dict(os.environ, {"OPENAI_API_KEY": "", "SPRING_MCP_URL": "http://localhost:8080/mcp"}, clear=False):
            with self.assertRaisesRegex(RuntimeError, "OPENAI_API_KEY"):
                validate_configuration()

    def test_allows_local_http_for_development(self):
        with patch.dict(
            os.environ,
            {
                "OPENAI_API_KEY": "test-key",
                "SPRING_MCP_URL": "http://localhost:8080/mcp",
                "ALLOW_INSECURE_MCP_HTTP": "false",
                "OPENAI_CHAT_MODEL": "gpt-5.6",
            },
            clear=False,
        ):
            validate_configuration()

    def test_rejects_non_local_http_without_explicit_opt_in(self):
        with patch.dict(
            os.environ,
            {
                "OPENAI_API_KEY": "test-key",
                "SPRING_MCP_URL": "http://school-api:8080/mcp",
                "ALLOW_INSECURE_MCP_HTTP": "false",
            },
            clear=False,
        ):
            with self.assertRaisesRegex(RuntimeError, "HTTPS"):
                validate_configuration()

    def test_accepts_https_mcp_url(self):
        with patch.dict(
            os.environ,
            {
                "OPENAI_API_KEY": "test-key",
                "SPRING_MCP_URL": "https://school-api.example.com/mcp",
                "ALLOW_INSECURE_MCP_HTTP": "false",
            },
            clear=False,
        ):
            validate_configuration()


if __name__ == "__main__":
    unittest.main()
