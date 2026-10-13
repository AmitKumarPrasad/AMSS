FROM python:3.12-slim
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
COPY pyproject.toml agent.py api.py ./
RUN pip install --no-cache-dir .
EXPOSE 8123
CMD ["sh","-c","uvicorn api:app --host 0.0.0.0 --port ${PORT:-8123}"]
