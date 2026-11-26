# Office Directory Frontend

React and Vite frontend for the office directory assignment. The page lists
available offices, supports searching by office name or number, and supports
deleting an office. Update functionality is not implemented.

## Requirements

- Node.js and npm
- The Spring Boot backend running on port `8081`

## Run locally

```powershell
npm install
npm run dev
```

Vite proxies `/api` requests to `http://localhost:8081`.

## Checks

```powershell
npm run lint
npm run build
```
