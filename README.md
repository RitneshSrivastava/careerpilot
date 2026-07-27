# CareerPilot

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen?logo=spring)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6DB33F?logo=spring)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

An AI-powered career preparation platform. Upload a resume and get real, AI-generated ATS scoring, skill extraction, and improvement suggestions — plus resume-to-job match scoring against live listings, all behind a secure, production-shaped Spring Boot backend with a React frontend.

Built to demonstrate backend engineering depth (JWT auth, layered architecture, async processing, structured error handling) alongside genuine AI integration (Spring AI + OpenAI, structured output parsing, cost-aware caching) — not just a CRUD app with an API call bolted on.

---

## Features

**Authentication**
- Registration with email OTP verification (real email delivery, 10-minute expiry codes)
- JWT-based stateless login, BCrypt password hashing
- Resend-OTP flow for undelivered codes

**Resume Management**
- Upload (PDF, validated for type/size/integrity), download, replace, soft delete
- Paginated resume listing

**AI Resume Analysis**
- PDF text extraction (Apache PDFBox)
- Async AI analysis via OpenAI (Spring AI): ATS score, extracted skills, improvement suggestions, summary
- Structured JSON output parsing — not manually regex'd from free text
- Database-backed caching: re-analysis is skipped if a completed result already exists for the current file version (no repeat API cost)

**Job Recommendation**
- Deterministic resume-to-job skill matching (explainable score, not another black-box AI call)
- Ranked results showing matched vs. missing skills per listing

**Frontend**
- React (Vite) single-page app: register → verify → login → upload → analyze → match
- Custom "flight deck" visual design, including a circular ATS-score instrument gauge

---

## System Architecture

```
                    React (Vite) Frontend
                            │
                     REST API (JWT auth)
                            │
                            ▼
                    Spring Boot Backend
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
   Spring Security     Spring Data JPA      Spring AI
   (JWT filter,          (MySQL)           (OpenAI, async,
   BCrypt, OTP gate)                        structured output)
                            │
                            ▼
                    Gmail SMTP (OTP emails)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1 |
| Security | Spring Security, JWT, BCrypt |
| Persistence | Spring Data JPA, MySQL |
| AI | Spring AI 2.0 (OpenAI), Apache PDFBox |
| Email | Spring Mail (Gmail SMTP) |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Frontend | React 18, Vite, React Router |
| Build | Maven |

---

## Project Structure

```
careerpilot/
├── backend/          Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── frontend/          React UI
│   ├── src/
│   └── package.json
└── README.md          (this file)
```

---

## Getting Started

### Prerequisites
- Java 21
- Node.js 18+
- MySQL running locally
- An OpenAI API key
- A Gmail account with an App Password (for OTP emails)

### Environment variables (backend)

No credentials are hardcoded anywhere in this project. Set these before running:

| Variable | Purpose |
|---|---|
| `DB_USERNAME` | MySQL username (defaults to `root`) |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Secret key used to sign JWTs |
| `OPENAI_API_KEY` | OpenAI API key for resume analysis |
| `MAIL_USERNAME` | Gmail address used to send OTP emails |
| `MAIL_APP_PASSWORD` | Gmail App Password (not your account password) |

### Run the backend
```bash
cd backend
./mvnw spring-boot:run
```
API docs: `http://localhost:8080/swagger-ui/index.html`

### Run the frontend
```bash
cd frontend
npm install
npm run dev
```
App: `http://localhost:5173`

---

## Design Decisions

**Why Spring AI over calling the OpenAI SDK directly?**
Structured output parsing (`.entity(ResumeAnalysisResult.class)`) means the model's response is parsed directly into a typed Java object — no manual JSON string-splitting — plus clean Spring-idiomatic dependency injection and auto-configuration.

**Why is the AI call async?**
So the request thread returns immediately instead of blocking on an external API call. The worker that performs the actual call lives in a separate bean (`ResumeAnalysisWorker`) rather than a private method, deliberately — Spring's `@Async` proxy is bypassed on self-invocation, a common gotcha.

**Why is job matching deterministic, not another AI call?**
It's free, instant, and fully explainable — a match score has a precise, defensible reason (`matched skills / required skills`), not a black-box model output.

**Why does the database double as the AI response cache?**
A completed analysis is reused as-is unless the resume file has been replaced since. No Redis needed yet, no repeat OpenAI cost for re-viewing the same file.

---

## Roadmap / Future Enhancements

- AI chat assistant (RAG over the user's own resume)
- Cover letter generator
- Mock interview simulator
- Refresh token rotation, RBAC, admin panel
- Redis caching, rate limiting, structured logging, CI/CD
- Deployment (Render + PostgreSQL)

---

## License

This project is licensed under the [MIT License](LICENSE) — free to use, modify, and learn from.

---

## Author

**Ritnesh Kumar Srivastava**
Computer Science Engineering Graduate
- GitHub: [RitneshSrivastava](https://github.com/RitneshSrivastava)
- LinkedIn: [ritneshks](https://www.linkedin.com/in/ritneshks)
