# oop-final-project-phone-evaluation

## Quick setup

This project can use OpenRouter's free model router for online AI valuation, and it falls back to the built-in local valuation engine if the API is unavailable.

1. Create your local config:

```bash
cp .env.example .env
```

2. Open `.env` and set your key:

```bash
OPENROUTER_API_KEY=your_openrouter_key
OPENROUTER_MODELS=meta-llama/llama-3.2-3b-instruct:free,openrouter/free
OPENROUTER_MODEL=openrouter/free
```

3. Compile and run:

```bash
javac -d out src/main/*.java
java -cp out main.Main
```

## Notes

- Keep your real `.env` local. The repo tracks `.env.example`, while `.env` is ignored by Git.
- If no key is configured, the app still works with the local valuation engine.
- The UI will show whether each valuation used external AI or local fallback logic.
- The project now writes a lightweight local vector database to `data/phone_vectors.db` and uses that retrieval context before calling the external AI API.
- If one free model is rate-limited, the app will automatically try the next model from `OPENROUTER_MODELS` before falling back locally.
