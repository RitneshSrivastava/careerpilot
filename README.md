# CareerPilot

An AI-powered career preparation platform built with Java, Spring Boot, and Spring AI. Users register, upload their resume, and receive AI-generated ATS scoring, skill extraction, and improvement suggestions grounded in the actual content of their document.

## Why this project

Most portfolio backend projects stop at CRUD. CareerPilot is built to demonstrate two things at once:
- A properly secured, production-shaped Spring Boot backend (JWT auth, ownership checks, pagination, soft deletes, structured error handling)
- Real integration with an LLM provider (OpenAI, via Spring AI) — not a toy wrapper, but async-processed, structured-output, cost-aware AI usage

## Tech stack

- **Java 21**, **Spring Boot 4.1**
- **Spring Security** — JWT-based stateless authentication, BCrypt password hashing
- **Spring Data JPA** + **MySQL**
- **Spring AI 2.0** (OpenAI) — structured AI output parsing
- **Apache PDFBox** — resume text extraction
- **Springdoc OpenAPI** — interactive Swagger UI documentation

## Features implemented so far

### Authentication
- User registration and login
- JWT issuance and validation
- Stateless session management, BCrypt password encoding

### Resume Management
- Upload (PDF, validated for type/size/safety)
- List (paginated)
- Download
- Replace
- Soft delete

### AI Resume Analysis
- PDF text extraction
- Async AI analysis via OpenAI (ATS score, extracted skills, improvement suggestions, summary)
- Database-backed caching: re-analysis is skipped if a completed result already exists for the current file version
- Basic rate limiting on concurrent analysis requests

## Getting started

### Prerequisites
- Java 21
- MySQL running locally
- An OpenAI API key (for the AI analysis feature)

### Environment variables required
This project does **not** hardcode any credentials. Set these before running:

| Variable | Purpose |
|---|---|
| `DB_USERNAME` | MySQL username (defaults to `root` if unset) |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Secret key used to sign JWTs |
| `OPENAI_API_KEY` | OpenAI API key for resume analysis |

### Run locally
```bash
git clone https://github.com/RitneshSrivastava/careerpilot.git
cd careerpilot
./mvnw spring-boot:run
```

### API documentation
Once running, interactive API docs are available at:
```
http://localhost:8080/swagger-ui/index.html
```

## Roadmap

This project is under active development. Planned next: job recommendation matching, AI chat assistant, cover letter generation, and mock interview simulation — see project board / commit history for progress.

## Author

Ritnesh Kumar Srivastava
