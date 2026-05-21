# TaskFlow Backend

REST API for the Android app, kept separate from the mobile code but in the same repository.

## Setup

1. Create a Supabase project.
2. Run `supabase/schema.sql` in the Supabase SQL editor.
3. Copy `.env.example` to `.env` and fill in the values.
4. Install dependencies:

```bash
npm install
```

5. Run locally:

```bash
npm run dev
```

The Android emulator calls the backend through:

```text
http://10.0.2.2:3000/v1/
```

Keep `SUPABASE_SERVICE_ROLE_KEY` only in this backend. Never put it in the Android app.
