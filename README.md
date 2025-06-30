![img_journey](https://github.com/user-attachments/assets/30282bb4-c05c-43c7-a823-0ad4e2f2569c)# 🚗 Advanced Mileage Tracker App

An Android application to **accurately track user mileage** with robust background location tracking, persistence, and real-world reliability.

---

## 🧩 Objective

Track real-time user journeys (distance, time, path) using foreground location tracking, background services, and local data persistence.

---

## 🎯 Features

### ✅ Core Features
- Start/Stop Journey from the Home Screen
- Accurate distance tracking using GPS
- Foreground location service with persistent notification
- Background & killed-app support with `START_STICKY` and `BootReceiver`
- Real-time distance update while tracking
- Summary screen with:
  - Distance (KM/MI)
  - Duration
  - Start & End Time
  - Route on Google Map
- Local storage of journeys using Room
- Past journeys list with detail screen and map preview

### 🔧 Technical Features
- Architecture: MVVM + Repository Pattern
- Location: `FusedLocationProviderClient` in high accuracy mode
- Location update interval: 5-10 seconds, 10 meters minimum
- Location filtering (accuracy, jitter)
- Real-time UI updates with `LiveData` and `Flow`
- Foreground Service + `NotificationChannel`
- Room Database for storing journeys
- Google Maps SDK for polyline routes
- Battery optimization handling
- Runtime permission management
- Persistence across device reboot via `BroadcastReceiver`
  
---



