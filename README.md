# Platelet Monitor (Wear OS)

A personal Wear OS companion prototype for observing sensor trends during a platelet donation session.

## Current watch MVP

- Manually start and end a procedure session.
- Treat GSR/cEDA as the required primary signal; a session is not marked ready until 120 fresh GSR samples form a baseline.
- Show a large live GSR reading, heart rate, and accelerometer-derived movement as secondary context.
- Show the detected GSR sensor name/type so Pixel Watch access can be verified on-device.
- Store sessions and readings locally with Room, including motion context.
- Show a vibration and notification when GSR changes from the personal session baseline.

The Pixel Watch 4 hardware includes a cEDA skin-conductance sensor. The Android sensor scan in this prototype attempts to find a compatible sensor exposed to third-party apps. Confirm on the watch that the sensor is listed and actual readings arrive; if they do not, the app must remain in a GSR-unavailable state rather than implying it is monitoring.

GSR measures sweat-related skin conductance. It can change with stress, movement, temperature, and other factors; it does not measure blood calcium or identify a citrate reaction. Movement is displayed as context only and does not change alert behavior. This app is not a medical device, has not been clinically validated, and cannot direct changes to anticoagulant or calcium treatment. If you feel unwell during donation, tell clinic staff promptly.

## Build and run

Open the project in Android Studio, sync Gradle, and run the app configuration on a Wear OS emulator or watch. Grant body-sensor permission; notification access is optional. Keep the monitor app open during a session in this foreground-only version. Verify cEDA sensor discovery, reading cadence, motion display, and alerts on the Pixel Watch 4 before any procedure use.

## Device notes

- The Pixel Watch 4 also includes optical heart rate, ECG-compatible electrical sensors, SpO2 sensors, and far-field skin temperature, among other sensors. This MVP uses heart rate and accelerometer movement only as context; additional signals should be added only through supported APIs and validated against the target watch.
- Google's current public Google Health API data-type list does not enumerate EDA. The prototype therefore relies on direct Android sensor discovery for live cEDA and must verify that path on the watch. Fitbit/Google Health synced summaries are not a substitute for live cEDA.
- Current readings remain on the watch; this prototype does not sync to a phone or upload data.
