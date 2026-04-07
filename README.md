# auditvault
Secure Document Retrieval System with Audit Logging built using Spring Boot, PostgreSQL, and JavaScript frontend.

## Local setup (quick)

### Backend (Spring Boot)

- Copy `AuditVault/.env.example` to your own env vars (do not commit secrets)
- Run:

```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8081`.

### Frontend (React + Vite)

From `AuditVault/frontend`:

```bash
npm install
npm run dev
```

If needed, set `VITE_API_URL=http://localhost:8081`.
