# Platelet Monitor (Wear OS)

Wear OS app prototype for Google Pixel Watch focused on donor-session monitoring with:

- GSR (galvanic skin response) as primary signal (vendor sensor discovery by sensor name/type)
- Heart rate support (`TYPE_HEART_RATE`)
- Per-session baseline learning
- Deviation detection with z-score thresholds
- Local persistence of sessions and readings via Room
- Real-time notification + vibration alerts when both metrics exceed baseline threshold

## Build

Open in Android Studio (Hedgehog+), sync Gradle, and run on a Wear OS device.

## Notes

- GSR availability depends on vendor-exposed sensor API on device firmware.
- Alert criteria are configurable in `SessionAnalyzer`.
