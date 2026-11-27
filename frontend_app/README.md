# 31 day Micro-SaaS Challenges - Frontend

Modern React frontend for the fraud detection platform, built with Vite, TypeScript-ready, and Tailwind CSS.

## Stack

- **React 19** — UI framework
- **Vite** — Build tool and dev server
- **React Router v7** — Client-side routing
- **Zustand** — Lightweight state management
- **Axios** — HTTP client
- **React Hook Form + Yup** — Form handling and validation
- **React Hot Toast** — Toast notifications
- **Recharts** — Data visualization
- **Tailwind CSS v4** — Styling

## Getting Started

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment

Copy the example environment file and update values:

```bash
cp .env.example .env
```

**.env** (default configuration):
```
VITE_API_BASE_URL=http://localhost:8000/api/v1
```

### 3. Start Development Server

```bash
npm run dev
```

The app will be available at **http://localhost:5173** (or the next available port).

### 4. Build for Production

```bash
npm run build
```

Built files will be in the `dist/` folder.

## Backend Connection

This frontend connects to the FastAPI backend via the URL configured in `.env`. **Ensure the backend is running** before using the app:

1. Navigate to the backend directory
2. Start the FastAPI server (default: `http://localhost:8000`)
3. The backend must have **CORS configured** to allow requests from `http://localhost:5173` (or your frontend URL)

### Adding CORS to Backend

If you get CORS errors, add this to `backend/fraud-detection-backend/app/main.py`:

```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # Frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

## Features

### Authentication

- **Login** (`/login`) — JWT bearer token authentication
- **Register** (`/register`) — Self-registration
- **Forgot Password** (`/forgot-password`) — Request password reset email
- **Reset Password** (`/reset-password?token=...`) — Set new password via token

### Protected Routes

All routes inside `/dashboard`, `/admin/*`, etc. require authentication.

- **Dashboard** (`/dashboard`) — Overview (placeholder)
- **Admin > Users** (`/admin/users`) — Requires `Admin` role
- **Admin > Roles** (`/admin/roles`) — Requires `Admin` role
- **Alerts, Cases, Rules, Compliance, MLOps** — Placeholder pages

### Role-Based Access Control

The app supports the backend's role and permission system:

- Users can have multiple roles (array)
- Each role has multiple permissions
- Superusers bypass all role checks
- The `<PrivateRoute requiredRole="Admin">` component enforces access control

## Project Structure

```
src/
├── api/                   # API client and modules
│   ├── modules/
│   │   ├── auth.api.js   # Auth endpoints
│   │   └── admin.api.js  # Admin endpoints (roles, permissions, users)
│   └── ...
├── assets/               # Images, styles
├── components/           # Reusable UI components
│   ├── layout/          # Header, Sidebar, AppLayout
│   ├── ui/              # Button, Card, Modal, Spinner, Table, etc.
│   ├── forms/           # Input, Select, DatePicker, etc.
│   ├── charts/          # Recharts wrappers
│   └── feedback/        # AlertBanner, Toast, ConfirmationDialog
├── config/              # App configuration
│   └── api.config.js    # API base URL and timeout
├── features/            # Feature-based modules
│   ├── auth/           # Login, Register, ForgotPassword, ResetPassword
│   ├── dashboards/     # Dashboard page
│   ├── admin/          # User and role management
│   └── ...             # alerts, cases, rules, compliance, mlops (placeholders)
├── hooks/              # Custom React hooks
├── routes/             # React Router setup
│   ├── AppRoutes.jsx  # Main route definitions
│   └── PrivateRoute.jsx # Auth guard component
├── store/              # Zustand state management
│   └── slices/
│       ├── auth.slice.js    # Auth state (login, logout, register)
│       └── users.slice.js   # User management state
├── types/              # TypeScript types (if migrating)
├── utils/              # Utility functions
│   └── apiClient.js   # Axios instance with interceptors (JWT injection, token refresh)
├── App.jsx            # Root component
├── main.jsx           # Entry point
└── index.css          # Global styles + Tailwind config

```

## Key Files

| File | Purpose |
|------|---------|
| **src/utils/apiClient.js** | Axios instance with JWT token injection and automatic refresh on 401 |
| **src/store/slices/auth.slice.js** | Zustand store for authentication (login, register, logout) |
| **src/routes/PrivateRoute.jsx** | Route guard that checks authentication and role |
| **src/api/modules/auth.api.js** | All auth-related API calls |
| **src/api/modules/admin.api.js** | Admin endpoints (roles, permissions, user management) |

## API Integration

All API calls go through `src/utils/apiClient.js`, which:

1. Injects the JWT token from localStorage into every request
2. Catches 401 errors and attempts to refresh the access token using the refresh token
3. Redirects to `/login` if refresh fails

### Token Storage

Tokens are stored in **localStorage** via Zustand persist middleware (`auth-storage` key):

```json
{
  "state": {
    "user": { "id": 1, "email": "user@ahadu.com", ... },
    "token": "access_jwt_here",
    "refreshToken": "refresh_jwt_here"
  }
}
```

## Scripts

```bash
npm run dev       # Start dev server (Vite)
npm run build     # Build for production
npm run preview   # Preview production build locally
npm run lint      # Run ESLint
```

## Notes

- **Backend must be running** at the configured `VITE_API_BASE_URL` (default: `http://localhost:8000/api/v1`)
- **CORS must be enabled** on the backend for the frontend origin
- The backend currently **does not have** a `GET /admin/users` endpoint to list all users — the admin pages will need this added
- Password reset emails are logged to console in development mode (check backend logs)
- Email verification is a DB flag but has no backend endpoints yet (incomplete flow)

## License

Proprietary — Ayoub Moustahfid S.C.
