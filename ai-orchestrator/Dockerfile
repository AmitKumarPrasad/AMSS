FROM python:3.12-slim
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
COPY pyproject.toml .
RUN pip install --no-cache-dir .
COPY agent.py api.py ./
EXPOSE 8123
CMD ["uvicorn","api:app","--host","0.0.0.0","--port","8123"]
