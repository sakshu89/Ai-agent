# AntiGem Assistant — Server Connection Guide

## Supported AI Servers

AntiGem supports any OpenAI-compatible inference server, plus specialized adapters:

| Provider | Adapter | Notes |
|----------|---------|-------|
| Ollama | Ollama | Local LLM server |
| llama.cpp | LlamaCpp | Local C++ inference |
| NVIDIA Nemotron | Nemotron | Cloud API |
| OpenAI API | OpenAICompatible | Official OpenAI |
| Any OpenAI-compatible | OpenAICompatible | Text-generation-webui, LM Studio, etc. |

## Adding a Server

1. Open Settings > AI Servers
2. Tap "Add Server"
3. Fill in:
   - **Name**: Friendly name (e.g., "My Ollama")
   - **Address**: Hostname or IP (e.g., "localhost" or "192.168.1.100")
   - **Port**: Server port (e.g., 11434 for Ollama)
   - **API Endpoint**: API path (e.g., "/v1" or "/api")
   - **Model ID**: Model name (e.g., "nemotron", "llama3.2")
   - **Auth Type**: none, bearer, api_key, or basic
   - **Auth Value**: Token or key (encrypted locally)
   - **Context Length**: Maximum tokens (default 4096)
   - **Timeout**: Request timeout in ms (default 60000)
   - **Provider Type**: ollama, llamacpp, nemotron, openai, generic

4. Tap "Test Connection" to verify
5. Set as default if desired

## Example: Ollama (Local)
- Address: localhost
- Port: 11434
- API Endpoint: /api
- Model ID: nemotron-mini (or any model you've pulled)
- Auth Type: none
- Provider Type: ollama

## Example: OpenAI API
- Address: api.openai.com
- Port: 443
- API Endpoint: /v1
- Model ID: gpt-4o-mini (or your model)
- Auth Type: bearer
- Auth Value: sk-... (your API key)
- Provider Type: openai

## Model Router
The router automatically selects models based on task type:
- **General**: Any connected server
- **Vision**: Server with vision support
- **Coding**: Server with tool support
- **Reasoning**: Server with large context (8K+)
- **Summarization**: Server with streaming
- **Voice**: Server with streaming

You can override automatic routing per conversation.

## Offline Mode
When no server is connected:
- Historical conversations remain accessible
- Local memory search works
- Task history is available
- AI features are clearly marked as unavailable
- Messages are saved and queued for later sending
