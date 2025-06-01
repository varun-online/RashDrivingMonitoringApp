# Rash Driving Control Device 🚗📍

This project is an **Android-based application** integrated with an **ESP32 microcontroller** designed to detect and prevent rash driving behavior. It monitors real-time speed, tracks GPS location, sends alerts when speed exceeds safe limits, and logs these events in **Firebase Realtime Database**. Ideal for smart vehicle monitoring and safety systems.

## 📱 Features

- 🚘 **Real-time Vehicle Tracking** using GPS and Google Maps
- ⚙️ **Speed Monitoring** via IR sensor connected to ESP32
- 🔥 **Over-speeding Alerts** triggered when speed exceeds 50 km/h
- ☁️ **Firebase Integration** to store speed, location, and alert history
- 🔔 **Push Notifications** for rash driving events
- 📊 **Speed History View** inside the Android app
- 🔊 **Buzzer Activation** for in-vehicle feedback

## 📡 Technology Stack

- **ESP32** with IR sensor & GPS module
- **Android Studio** (Java/Kotlin)
- **Firebase Realtime Database**
- **Google Maps API**
- **TinyGPS++** (for ESP32 location parsing)

## 🛠️ How It Works

1. ESP32 reads vehicle speed using IR sensor and GPS location.
2. If speed ≥ 50 km/h, it sends data to Firebase and activates a buzzer.
3. Android app fetches this data, displays it on the dashboard, and notifies the user.
4. Alert logs are stored and can be reviewed anytime in the app.

## 🔧 Requirements

### Hardware
- ESP32
- IR Sensor
- GPS Module (like NEO-6M)
- Buzzer
- SIM Module
- Motor (optional)

### Software
- Android Studio
- Arduino IDE
- Firebase Account

## License

This project is licensed under the MIT License - see the [LICENSE](./app/LICENSE) file for details.
