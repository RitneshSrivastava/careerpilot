# CareerPilot UI

React frontend for CareerPilot — upload a resume, get AI-powered ATS scoring and feedback.

## Setup

```bash
npm install
npm run dev
```

Runs at `http://localhost:5173`. Requires the CareerPilot backend running at `http://localhost:8080` (see `src/api.js` to change this if needed).

## Build for production

```bash
npm run build
```

Outputs static files to `dist/` — deploy this folder to any static host (Render Static Site, Netlify, Vercel, etc.).
