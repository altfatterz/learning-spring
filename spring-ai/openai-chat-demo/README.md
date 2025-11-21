🔑 Step-by-Step Guide

### Start the app

Before starting the application make sure you set the `OPENAI_API_KEY` environment variable

Create one here: https://platform.openai.com/api-keys

⚠️ Best Practices for Security
- Never share your API key publicly (e.g., GitHub repos).
- Rotate keys if you suspect compromise.
- Use environment variables or secret managers instead of hardcoding keys.
- Limit usage by creating separate keys for different projects.

### Example 

```bash
$ http :8080/api/chat\?prompt="tell me a joke"

HTTP/1.1 200
Connection: keep-alive
Content-Length: 77
Content-Type: text/plain;charset=UTF-8
Date: Thu, 03 Jul 2025 14:50:09 GMT
Keep-Alive: timeout=60

Why did the scarecrow win an award?

Because he was outstanding in his field!
```

```bash
$ http :8080/api/chat-verbose\?prompt="tell me a joke"

HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Sat, 15 Nov 2025 14:08:23 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "metadata": {
        "empty": false,
        "id": "chatcmpl-CcBEtXaHyuAJbJtcHcJiEFpNkMF0S",
        "model": "gpt-4o-2024-08-06",
        "promptMetadata": [],
        "rateLimit": {
            "requestsLimit": 500,
            "requestsRemaining": 499,
            "requestsReset": "PT0.12S",
            "tokensLimit": 30000,
            "tokensRemaining": 29994,
            "tokensReset": "PT0.012S"
        },
        "usage": {
            "completionTokens": 14,
            "nativeUsage": {
                "completion_tokens": 14,
                "completion_tokens_details": {
                    "accepted_prediction_tokens": 0,
                    "audio_tokens": 0,
                    "reasoning_tokens": 0,
                    "rejected_prediction_tokens": 0
                },
                "prompt_tokens": 11,
                "prompt_tokens_details": {
                    "audio_tokens": 0,
                    "cached_tokens": 0
                },
                "total_tokens": 25
            },
            "promptTokens": 11,
            "totalTokens": 25
        }
    },
    "result": {
        "metadata": {
            "contentFilters": [],
            "empty": true,
            "finishReason": "STOP"
        },
        "output": {
            "media": [],
            "messageType": "ASSISTANT",
            "metadata": {
                "annotations": [],
                "finishReason": "STOP",
                "id": "chatcmpl-CcBEtXaHyuAJbJtcHcJiEFpNkMF0S",
                "index": 0,
                "messageType": "ASSISTANT",
                "refusal": "",
                "role": "ASSISTANT"
            },
            "text": "Why don't skeletons fight each other?\n\nThey don't have the guts.",
            "toolCalls": []
        }
    },
    "results": [
        {
            "metadata": {
                "contentFilters": [],
                "empty": true,
                "finishReason": "STOP"
            },
            "output": {
                "media": [],
                "messageType": "ASSISTANT",
                "metadata": {
                    "annotations": [],
                    "finishReason": "STOP",
                    "id": "chatcmpl-CcBEtXaHyuAJbJtcHcJiEFpNkMF0S",
                    "index": 0,
                    "messageType": "ASSISTANT",
                    "refusal": "",
                    "role": "ASSISTANT"
                },
                "text": "Why don't skeletons fight each other?\n\nThey don't have the guts.",
                "toolCalls": []
            }
        }
    ]
}
```

Logging is enabled with `SimpleLoggerAdvisor`

Advisor: Intercept & Enhance Every AI Call (AOP for AI!)

### Native build

```bash
mvn -Pnative spring-boot:build-image

# docker image created:
docker image ls | grep openai-chat-demo
openai-chat-demo                           0.0.1-SNAPSHOT   bffb0ccfba2a   45 years ago   287MB

# set OPENAI_API_KEY
export OPENAI_API_KEY=

# start 
docker run -e OPENAI_API_KEY=$OPENAI_API_KEY --rm -p 8080:8080 openai-chat-demo:0.0.1-SNAPSHOT

# test
http :8080/api/chat\?prompt="Hello"
Hello! How can I assist you today?
```

### Prometheus Actuator: Track Token Usage

```bash
curl -v http://localhost:8080/actuator/prometheus | grep token

gen_ai_client_token_usage_total{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o",gen_ai_response_model="gpt-4o-2024-08-06",gen_ai_system="openai",gen_ai_token_type="input"} 23.0
gen_ai_client_token_usage_total{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o",gen_ai_response_model="gpt-4o-2024-08-06",gen_ai_system="openai",gen_ai_token_type="output"} 272.0
gen_ai_client_token_usage_total{gen_ai_operation_name="chat",gen_ai_request_model="gpt-4o",gen_ai_response_model="gpt-4o-2024-08-06",gen_ai_system="openai",gen_ai_token_type="total"} 295.0
```

### Start grafana and prometheus

```bash
$ docker compose up 
```

Prometheus: http://localhost:9090/
- Search for `gen_ai_client_token_usage_total`

Grafana: http://localhost:3000/ (admin/admin)


### Resources:

- https://www.youtube.com/watch?v=pBVKkcBhw6I
- https://www.youtube.com/watch?v=R_BXvIKrN4c