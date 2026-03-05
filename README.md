# GhostWatcher (MR. ROBOT themed Android app)

A Kotlin + Jetpack Compose Android app with a dark neon hacker aesthetic.

## Features
- "START" button with pulsing/glitch animation.
- GET request to `http://127.0.0.1:8000/ghostwatcher`.
- Displays returned image with animated reveal.
- Short synthetic beat sequence for "viral reel" vibe when START is pressed.

## Run
1. Open in Android Studio (Hedgehog+).
2. Let Gradle sync.
3. Run the `app` module.

> If you run on an emulator and the API is on your host machine, Android usually needs `http://10.0.2.2:8000/ghostwatcher` instead of `127.0.0.1`.
