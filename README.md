# Platelet Monitor (Wear OS)

A personal Wear OS companion prototype for observing readings during a platelet donation session.

## Current watch MVP

- Manually start and end a procedure session.
- Show the latest heart-rate reading and whether a heart-rate sensor is available.
- Learn a fresh baseline from the current session (120 heart-rate samples).
- Treat vendor-exposed GSR as optional; the session can run without it.
- Store sessions and readings locally with Room.
- Show a vibration and notification when readings cross a personal-baseline threshold.

The alert is a personal trend cue only. It is not a medical device, does not detect platelet-donation reactions, and has not been clinically validated. If you feel unwell, tell clinic staff. Do not rely on this app for care decisions.

## Build and run

Open the project in Android Studio, sync Gradle, and run the app configuration on a Wear OS emulator or watch. Grant the requested sensor and notification permissions. Check actual sensor availability on the target watch before a procedure.

## Device notes

- Android devices do not have a standard GSR sensor type. GSR support is optional and depends on a vendor exposing a compatible sensor through Android's sensor API.
- Heart-rate availability and delivery cadence vary by watch, firmware, permissions, and app state. Validate sampling and notification behavior on the Pixel Watch model you intend to use.
- Current readings remain on the watch; this prototype does not sync to a phone or upload data.
