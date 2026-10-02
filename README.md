# BankWise

> **BankWise is a production-oriented banking AI assistant that uses Retrieval-Augmented Generation (RAG) to answer banking questions from a user-owned knowledge base, with citations, secure authentication, persistent chat history, and resilient LLM integration.**

## Overview

BankWise is a backend-first banking AI assistant built with Java and Spring Boot. The system combines document ingestion, vector search, RAG, LLM generation, JWT authentication, persistent conversations, and administrative user management.

The application is designed around clear module boundaries so that authentication, documents, knowledge bases, conversations, chat, RAG, and administration can evolve independently.

### Core idea

```text
User
  |
  v
Authentication / JWT
  |
  v
Chat API
  |
  v
Conversation ownership
  |
  v
RAG Pipeline
  |
  +--> Query processing
  +--> Vector retrieval
  +--> Context building
  +--> Prompt construction
  +--> LLM generation
  +--> Answer validation
  +--> Citation generation
  |
  v
Chat persistence
  |
  +--> User message
  +--> Assistant message
  +--> Conversation metadata
  |
  v
MongoDB Atlas
```

## Features

### Authentication and security

* Email/password registration and login
* JWT-based authentication
* Custom Spring Security principal
* Database-backed token blacklist
* Account status management
* Login failure tracking and account locking support
* Password hash excluded from API representations
* Role-based authorization
* Admin-only APIs
* Correlation ID support through `X-Correlation-Id`

### Document and knowledge base management

* Upload banking documents
* Document metadata extraction
* Document parsing through a parser resolver
* Chunking with configurable chunk size and overlap
* Vector storage using MongoDB Atlas Vector Search
* User-owned knowledge bases
* Knowledge-base access validation
* Support direction for text, PDF, and web-based banking content

### RAG pipeline

* Query normalization
* Knowledge-base scoped retrieval
* Context construction
* Prompt construction
* LLM answer generation
* Answer validation against retrieved documents
* Citation generation
* Controlled `no answer` behavior when relevant knowledge is unavailable

### Chat and conversations

* Persistent conversation history
* Conversations survive logout/login
* User-owned conversations
* User and assistant messages stored separately
* Active/archived conversation status
* Conversation message count
* Last-message timestamp
* Conversation ownership checks
* Automatic conversation creation for new chats
* Archived conversations cannot receive new messages

### LLM resilience

* Resilience4j integration around the LLM boundary
* Retry support for transient failures
* Circuit breaker support
* Fallback LLM/client support
* Permanent provider failures should not be blindly retried
* LLM failures happen before chat persistence

### Administration

* Admin-only user listing
* User search
* User details
* User account-status updates
* Basic user statistics
* Sensitive authentication fields are not exposed

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                         API Layer                            │
│ Controllers / DTOs / Request Validation / Responses         │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                       Application Layer                      │
│ Services / Orchestration / Use Cases                        │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                         Domain Layer                         │
│ Domain Models / Enums / Business Rules                      │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                     Infrastructure Layer                    │
│ MongoDB / Vector Store / LLM / Storage / External APIs      │
└─────────────────────────────────────────────────────────────┘
```

### Module boundaries

```text
com.bankwise
├── admin
├── auth
├── chat
├── common
├── config
├── conversation
├── document
├── knowledgebase
├── rag
└── security
```

## RAG Flow

```text
Chat Request
     |
     v
Knowledge Base Access Validation
     |
     v
Query Processor
     |
     v
Document Retriever
     |
     v
Retrieved Documents
     |
     v
Context Builder
     |
     v
Prompt Builder
     |
     v
LLM Service
     |
     +------ Primary LLM
     |
     +------ Resilience4j
     |          |
     |          +--> Retry transient failures
     |          +--> Circuit breaker
     |          +--> Fallback LLM
     |
     v
Answer Validator
     |
     v
Citation Builder
     |
     v
RagResult
```

The current RAG request is scoped to the user's knowledge base. Conversation history is persisted by the chat module; making previous messages part of the LLM context is a separate evolution of the RAG pipeline.

## Chat Persistence Flow

```text
ChatController
      |
      v
ChatService
      |
      +--> RAG / LLM generation
      |
      v
ChatPersistenceService
      |
      +--> Create/get conversation
      +--> Save USER message
      +--> Save ASSISTANT message
      +--> Atomically update conversation metadata
      |
      v
MongoDB transaction
```

### Conversation relationship

```text
User 1 ───────── * Conversation 1 ───────── * ChatMessage
```

* `User` owns conversations.
* `Conversation` owns chat-thread metadata.
* `ChatMessage` belongs to a conversation and user.
* Logout does not delete conversation history.
* Conversation deletion currently uses archive semantics.

## Technology Stack

| Area             | Technology                                          |
| ---------------- | --------------------------------------------------- |
| Language         | Java 21                                             |
| Backend          | Spring Boot 4.1.1                                   |
| Web              | Spring MVC / REST                                   |
| Security         | Spring Security                                     |
| Authentication   | JWT                                                 |
| Database         | MongoDB Atlas                                       |
| Vector store     | MongoDB Atlas Vector Search                         |
| RAG framework    | Spring AI 2.0.1                                     |
| Primary LLM      | Groq-compatible cloud chat model                    |
| Embeddings       | Gemini `gemini-embedding-001`                       |
| Document parsing | Apache Tika / Spring AI document reader             |
| Resilience       | Resilience4j                                        |
| Build            | Maven                                               |
| Testing          | JUnit / Mockito / Spring Boot Test / Testcontainers |
| Code quality     | SonarQube                                           |
| Frontend         | Planned separately                                  |

## Requirements

* Java 21+
* Maven 3.9+
* MongoDB Atlas
* Groq API key
* Gemini API key
* Git

Optional:

* Docker
* Docker Compose
* SonarQube
* IntelliJ IDEA / STS

## Project Setup

```bash
git clone <YOUR_REPOSITORY_URL>
cd bankwise
```

Configure:

```text
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster>/<database>
GROQ_API_KEY=<your-groq-api-key>
GEMINI_API_KEY=<your-gemini-api-key>
JWT_SECRET=<strong-random-secret>
```

Never commit real secrets.

### Application configuration

```yaml
server:
  port: 9090

spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: bankwise-db

bankwise:
  storage:
    documents: ./storage/documents
  document:
    max-file-size: 10485760
    chunk-size: 1000
    chunk-overlap: 200
```

## Running the Application

Linux/macOS:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Application:

```text
http://localhost:9090
```

Health:

```text
GET /actuator/health
```

## API Overview

### Authentication

Base path:

```text
/api/v1/auth
```

| Method | Endpoint                | Purpose  |
| ------ | ----------------------- | -------- |
| POST   | `/api/v1/auth/register` | Register |
| POST   | `/api/v1/auth/login`    | Login    |
| POST   | `/api/v1/auth/logout`   | Logout   |

### Conversations

```text
/api/v1/conversations
```

| Method | Endpoint                                 | Purpose                   |
| ------ | ---------------------------------------- | ------------------------- |
| POST   | `/api/v1/conversations`                  | Create conversation       |
| GET    | `/api/v1/conversations`                  | List active conversations |
| GET    | `/api/v1/conversations/{conversationId}` | Get conversation          |
| DELETE | `/api/v1/conversations/{conversationId}` | Archive conversation      |

### Chat

```text
/api/v1/chat
```

```json
{
  "conversationId": null,
  "message": "What is the interest rate mentioned in the uploaded banking policy?"
}
```

Existing conversation:

```json
{
  "conversationId": "<conversation-uuid>",
  "message": "Can you explain the eligibility criteria?"
}
```

Response:

```json
{
  "success": true,
  "message": "Chat response generated successfully",
  "data": {
    "conversationId": "<conversation-uuid>",
    "messageId": "<assistant-message-uuid>",
    "answer": "<generated-answer>",
    "citations": []
  },
  "timestamp": "<timestamp>"
}
```

### Documents

```text
/api/v1/documents
```

Responsible for:

* upload
* storage
* validation
* parsing
* metadata extraction
* chunking
* ingestion

### Administration

```text
/api/v1/admin
```

| Method | Endpoint                              | Purpose           |
| ------ | ------------------------------------- | ----------------- |
| GET    | `/api/v1/admin/users`                 | List/search users |
| GET    | `/api/v1/admin/users/{userId}`        | User details      |
| PATCH  | `/api/v1/admin/users/{userId}/status` | Update status     |
| GET    | `/api/v1/admin/stats`                 | User statistics   |

Admin APIs require:

```text
ROLE_ADMIN
```

## Standard API Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2026-01-01T00:00:00"
}
```

## Security Model

```text
HTTP Request
    |
    v
JWT Filter
    |
    v
Token Validation
    |
    v
CustomUserPrincipal
    |
    v
Controller
```

User-owned resources are always resolved using authenticated `userId`.

Example:

```text
findByIdAndUserId(conversationId, userId)
```

This prevents cross-user resource access.

Admin APIs use:

```java
@PreAuthorize("hasRole(\"ADMIN\")")
```

## Document Ingestion

```text
Multipart File
     |
     v
DocumentController
     |
     v
DocumentService
     |
     v
DocumentStorage
     |
     v
DocumentIngestionService
     |
     +--> Parser Resolver
     +--> Metadata Extractor
     +--> Chunker
     |
     v
Embeddings
     |
     v
MongoDB Atlas Vector Store
```

Configuration:

```text
Storage: ./storage/documents
Maximum file size: 10 MB
Chunk size: 1000
Chunk overlap: 200
```

## Vector Search

```text
Document
  -> parse
  -> chunk
  -> embedding
  -> MongoDB Atlas
```

Query:

```text
Question
  -> embedding
  -> vector similarity search
  -> relevant chunks
  -> context
  -> LLM
```

## LLM Resilience

```text
LlmService
   |
   v
ResilientLlmService
   |
   +--> PrimaryLlmClient
   |
   +--> Resilience4j
          |
          +--> Retry transient failures
          +--> Circuit breaker
          +--> FallbackLlmClient
```

Transient failures can include:

* timeout
* network failure
* HTTP 429
* temporary HTTP 5xx

Permanent failures should not be blindly retried:

* deprecated model
* model not found
* invalid API key
* invalid request

If both primary and fallback generation fail, the chat should fail without persisting a partial turn.

## Transaction Strategy

```text
LLM generation
      |
      | success
      v
Transaction begins
      |
      +--> Save user message
      +--> Save assistant message
      +--> Update conversation metadata
      |
      v
Transaction commit
```

The LLM call remains outside the database transaction so external model latency does not unnecessarily hold database transaction resources.

## Project Structure

```text
src/
├── main/
│   ├── java/com/bankwise/
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── chat/
│   │   ├── common/
│   │   ├── config/
│   │   ├── conversation/
│   │   ├── document/
│   │   ├── knowledgebase/
│   │   ├── rag/
│   │   └── security/
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    └── java/com/bankwise/
        ├── unit/
        ├── integration/
        └── e2e/
```

## Testing Strategy

### Unit tests

Focus on:

* Auth service
* JWT handling
* Conversation service
* Chat persistence
* Query processing
* Prompt construction
* Answer validation
* Citation generation
* Admin service
* Document validation
* Document ingestion

### Integration tests

Test:

* MongoDB repositories
* MongoDB transactions
* Security configuration
* Vector store
* Document ingestion
* Controller/service integration

Testcontainers can provide isolated infrastructure.

### E2E

```text
Register
  -> Login
  -> Upload document
  -> Ingest
  -> Ask question
  -> Receive answer/citations
  -> Logout
  -> Login
  -> Read conversation history
```

## SonarQube / Code Quality

Focus areas:

* No leaked secrets
* Low duplication
* Small focused methods
* Meaningful naming
* Proper exception handling
* Input validation
* Secure authorization
* Resource handling
* Meaningful tests
* Maintainable module boundaries

Build:

```bash
./mvnw clean verify
```

Then execute the Sonar analysis command configured for the project.

## Production Considerations

### Security

* HTTPS
* Strong JWT signing secret/key
* Secure secret storage
* Rate limiting
* Restricted CORS
* Input validation
* File validation
* Upload limits
* Admin authorization
* Ownership checks

### Database indexes

```text
users:
  email unique

conversations:
  userId + status + lastMessageAt

chat_messages:
  conversationId + userId + createdAt
```

For large-scale user search, prefer MongoDB Atlas Search over unbounded regex queries.

### LLM

* Explicit timeouts
* Failure classification
* Retry limits
* Circuit breaker
* Fallback provider/model
* Provider monitoring
* No secrets in source code

## Current Scope

* JWT authentication
* User management
* Document upload/ingestion
* User-owned knowledge bases
* RAG answering
* Citations
* Persistent conversations
* Chat persistence
* Admin module
* LLM resilience/fallback architecture

## Future Scope

* Forgot/reset password
* Email verification
* OAuth/OIDC
* Conversation message-history API
* Automatic conversation titles
* Richer citations
* Web-search fallback
* RBI-aligned fallback policy
* RAG evaluation
* Atlas Search
* Admin audit logs
* Granular permissions
* Rate limiting
* Metrics/tracing
* Frontend
* Complete integration/E2E suite

## Development Workflow

```bash
git checkout master
git pull origin master
git checkout -b dev
```

Example:

```bash
git status
git add .
git commit -m "feat: add conversation persistence"
git push origin dev
```

## Commit Convention

```text
feat: add conversation persistence
fix: handle missing LLM response
refactor: isolate chat persistence service
test: add conversation service tests
docs: update project README
chore: update dependencies
```

## API Design Principles

* Controllers remain thin.
* Business logic belongs in services.
* Domain models own domain behavior where appropriate.
* Repositories handle persistence.
* DTOs define API boundaries.
* Mappers translate domain objects to responses.
* Ownership is enforced at service/repository boundaries.
* External systems are hidden behind interfaces.
* Infrastructure details should not leak into controllers.

## Deep Module / Codebase Design Principles

1. Keep interfaces narrow.
2. Hide implementation details.
3. Create explicit seams around external systems.
4. Separate orchestration from persistence.
5. Prefer domain ownership over shared mutable state.
6. Validate authorization close to the resource boundary.

## End-to-End Chat Request

```text
1. Client sends JWT + chat request
             |
2. Security filter validates JWT
             |
3. Controller obtains userId
             |
4. ChatService resolves knowledge base
             |
5. RagService validates ownership
             |
6. QueryProcessor normalizes question
             |
7. DocumentRetriever finds chunks
             |
8. ContextBuilder builds context
             |
9. PromptBuilder creates prompt
             |
10. LlmService generates answer
             |
11. AnswerValidator validates
             |
12. CitationBuilder builds citations
             |
13. ChatPersistenceService starts transaction
             |
14. USER message saved
             |
15. ASSISTANT message saved
             |
16. Conversation metadata updated
             |
17. Transaction commits
             |
18. ChatResponse returned
```

## Troubleshooting

### MongoDB connects to localhost

Check the active Spring configuration and environment variables. Ensure no local MongoDB URI overrides the Atlas URI.

### MongoDB transaction fails

Verify that the MongoDB deployment supports transactions. MongoDB Atlas supports the required transaction model.

### LLM unavailable

Check:

* API key
* Provider status
* Model name
* Model availability
* Resilience4j fallback configuration

### Vector search returns nothing

Check:

* ingestion completed
* embeddings generated
* vector collection
* vector index
* embedding dimensions
* knowledge-base ID
* retrieval configuration

### Conversation access fails

Verify that authenticated `userId` matches the conversation's `userId`.

## License

Add the chosen project license before public distribution.

Example:

```text
MIT License
```

## Project Purpose

BankWise demonstrates practical backend engineering across:

* Spring Boot
* Spring Security
* JWT
* MongoDB
* RAG
* Vector Search
* LLM integration
* Resilience patterns
* REST API design
* Modular architecture
* Testing
* SonarQube
* Production-oriented code quality

## Disclaimer

BankWise is a software project. Generated answers should not be treated as financial advice. Banking information should be validated against applicable official bank/product documentation and current regulatory guidance before being used for a real financial decision.
