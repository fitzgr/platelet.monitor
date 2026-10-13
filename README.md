# Platelet Monitor (Wear OS)

A personal Wear OS companion prototype for showing changes in skin conductance and pulse during a platelet donation session.

## Current watch MVP

- Manually start and end a procedure session.
- Use GSR/cEDA as the primary signal and pulse as a second signal.
- Learn a session baseline from 120 fresh GSR samples and 30 fresh pulse samples.
- Alert with vibration and a notification when either fresh signal moves at least 1.8 baseline standard deviations above or below its session baseline.
- Show the Android-discovered GSR sensor name/type and the current pulse reading.
- Store sessions and readings locally with Room.

The Pixel Watch 4 hardware includes a cEDA skin-conductance sensor. The Android sensor scan attempts to find a compatible sensor exposed to third-party apps. Confirm on the watch that the sensor is listed and actual readings arrive; if they do not, the app remains in a GSR-unavailable state.

These sensor changes are general body-signal alerts. Skin conductance reflects sweat-related electrical changes, and pulse can change for many reasons; neither signal identifies why a change happened or measures blood calcium. The app is a monitor, not a medical device, and cannot direct anticoagulant or calcium treatment. If you feel unwell during donation, tell clinic staff promptly.

## Build and run

Open in Android Studio, sync Gradle, and run the app on a Wear OS emulator or watch. Grant body-sensor permission; notification access is optional. Keep the app open during a session in this foreground-only version. Confirm live cEDA access, pulse readings, baseline progress, and alert behavior on Pixel Watch 4 before relying on the prototype.

## Device notes

- Pixel Watch 4 includes optical heart rate, ECG-compatible electrical sensors, SpO2 sensors, accelerometer, gyroscope, and far-field skin temperature, among others. This MVP uses GSR/cEDA and pulse only.
- Google's current public Google Health API data-type list does not enumerate EDA. The prototype relies on direct Android sensor discovery for live cEDA and must verify access on the watch. Synced summaries are not a substitute for live cEDA.
- Readings remain on the watch; this prototype does not sync to a phone or upload data.
