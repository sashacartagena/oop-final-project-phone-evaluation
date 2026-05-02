# Phone Evaluation Tool

An object-oriented Java command-line application for searching phones and estimating resale prices.

This project combines:

- A local phone knowledge base
- A lightweight RAG-style retrieval pipeline
- Optional external AI valuation through OpenRouter
- A fallback local valuation engine when the API is unavailable

## Overview

The application allows a user to:

- Evaluate a phone by model, storage, and condition
- Search phones by model
- Search phones by price range
- View all reference phones and evaluated phones

The system starts with a built-in reference catalog of phones and can add new evaluated phones during runtime.

## Main Features

### 1. Phone valuation

Users can enter:

- Phone model
- Storage size
- Damage description

The application then:

1. Retrieves the most relevant phone references from the local vectorized reference database
2. Uses those references as context for an external AI model when available
3. Falls back to the local valuation logic if the external API fails

### 2. Search

Users can search:

- By phone model
- By estimated price range

### 3. Built-in reference catalog

The project includes reference phone data for:

- iPhone 11 through iPhone 17 family
- Selected Samsung models
- Selected Google Pixel models
- Selected OnePlus and Motorola models

## Architecture

The design follows a clearer separation of responsibilities:

- `Main`
  Bootstraps the application and wires dependencies.

- `Handler`
  Handles CLI interaction only: menu display, reading user input, and printing results.

- `PhoneAppService`
  Coordinates application-level business flow such as valuation and search.

- `PhoneList`
  Stores phones and performs search operations.

- `ReferencePhoneLoader`
  Loads built-in reference phones into the list at startup.

- `PhoneKnowledgeBase`
  Holds the reference phone catalog.

- `RAGService`
  Performs local valuation using retrieved phone references.

- `PhoneVectorDatabase`
  Builds and stores a lightweight local vector database in `data/phone_vectors.db`.

- `OpenRouterAIService`
  Calls external AI models through OpenRouter and retries multiple free models when needed.

- `AIService`
  Interface for valuation services.

- `Phone`
  Represents phone data.

- `EvaluationResult` and `PhoneEvaluationResponse`
  Carry valuation output and response data.

## RAG-Style Pipeline

This project uses a lightweight RAG-style flow:

1. The reference catalog is vectorized locally.
2. The vectors are written to `data/phone_vectors.db`.
3. When a user evaluates a phone, the system retrieves the most similar references.
4. Those retrieved references are sent as context to the external AI model.
5. If the API is unavailable or rate-limited, the system falls back to the local valuation engine.

This is not a full industrial vector database setup such as FAISS, pgvector, or Pinecone, but it still demonstrates the retrieval + augmentation idea in a local Java project.

## Project Structure

```text
.
├── README.md
├── Usecase.png
├── data/
│   └── phone_vectors.db
└── src/main/
    ├── AIService.java
    ├── EnvLoader.java
    ├── EvaluationResult.java
    ├── Handler.java
    ├── Main.java
    ├── OpenRouterAIService.java
    ├── Phone.java
    ├── PhoneAppService.java
    ├── PhoneEvaluationResponse.java
    ├── PhoneKnowledgeBase.java
    ├── PhoneList.java
    ├── PhoneVectorDatabase.java
    ├── RAGService.java
    └── ReferencePhoneLoader.java
```

## Setup

### 1. Create a local environment file

```bash
cp .env.example .env
```

### 2. Configure your OpenRouter API key

Edit `.env`:

```bash
OPENROUTER_API_KEY=your_openrouter_key
OPENROUTER_MODELS=meta-llama/llama-3.2-3b-instruct:free,openrouter/free
OPENROUTER_MODEL=openrouter/free
```

Notes:

- `.env.example` is tracked
- `.env` is ignored by Git
- If no API key is configured, the app still works with the local valuation engine

## Compile and Run

Compile:

```bash
javac -d out src/main/*.java
```

Run:

```bash
java -cp out main.Main
```

## Example Usage

### Evaluate a phone

1. Choose `1. Evaluate my phone`
2. Enter a model such as `iPhone 12`
3. Enter storage such as `64`
4. Enter damage such as `none`

The application will show:

- AI source
- Whether external AI was used
- Explanation
- Estimated price

### Search by model

1. Choose `2. Search phones`
2. Choose `1. Search by model`
3. Enter a term such as `iPhone 15`

### Search by price

1. Choose `2. Search phones`
2. Choose `2. Search by price range`
3. Enter a minimum and maximum price

## API Behavior

When OpenRouter is configured:

- The app tries external AI first
- It can retry multiple free models from `OPENROUTER_MODELS`
- If one model is rate-limited, it tries the next one
- If all external attempts fail, it falls back to the local valuation engine

The UI shows this clearly through:

- `AI source: ...`
- `Used external AI: Yes/No`

## Design Notes

This project was refactored to improve responsibility separation:

- `Handler` no longer controls both UI and business workflow
- `PhoneList` no longer loads reference data by itself
- Reference loading, application flow, retrieval, and external AI access are separated into dedicated classes

This improves maintainability and better aligns with object-oriented design and SOLID expectations, especially Single Responsibility Principle.

## Limitations

- Current vector storage is file-based and designed for 100+ entries. Not tested for production-scale data.
- Phone prices are reference values, not guaranteed real-time market prices.
- Data added during runtime is in-memory only unless you extend persistence further.

## Future Improvements

- Add persistent storage for evaluated phones
- Add unit tests
- Replace the lightweight vector store with a real embedding/vector database stack
- Add multilingual AI explanations

## Notes

- Executable Java OOP implementation
- Multiple classes with separated responsibilities
- A lightweight RAG-style retrieval design
- External AI integration with fallback behavior
