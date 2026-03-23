# Cross-Device Ecosystem

## Structure

- `frontend` - React desktop UI
- `backend` - Node.js server and signaling layer
- `android-app` - Android Studio mobile client

## Run

### Frontend

```bash
cd frontend
copy .env.example .env
npm install
npm start
```

### Backend

```bash
cd backend
copy .env.example .env
npm install
npm start
```

### Android App

Open `android-app` in Android Studio and let Gradle sync.

## Environment

- `frontend/.env`
  `REACT_APP_SERVER_URL=http://localhost:5000`
- `backend/.env`
  `PORT=5000`
  `CLIENT_URL=http://localhost:3000`
