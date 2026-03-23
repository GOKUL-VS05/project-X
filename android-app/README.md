# Android App

Open this folder in Android Studio and let Gradle sync.

To change the backend URL without editing Kotlin code:

1. Copy `local.defaults.properties` to `local.properties` if needed.
2. Set `SERVER_URL=http://YOUR_COMPUTER_IP:5000`

Example:

```properties
SERVER_URL=http://192.168.1.5:5000
```

The app reads this value into `BuildConfig.SERVER_URL`.
