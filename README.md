## Android (Java) Demo App using InfluxDB for storing and quering sensor data

This Android Java app is used to store sensor data from accelerometer and gyroscope of the smartphone in X, Y, Z axis at regular time intervals into an InfluxDB Serverless database bucket.

1. [InfluxDB Java SDK](https://github.com/influxdata/influxdb-java)
2. [MPAndroid Charts library for Data Visualization](https://github.com/PhilJay/MPAndroidChart)

<img src="https://github.com/InfluxCommunity/SensorLogger/blob/main/app/src/main/res/drawable/screenshot_main_activity.png" width="300">
<img src="https://github.com/InfluxCommunity/SensorLogger/blob/main/app/src/main/res/drawable/screenshot_graph_activity.png" width="300">

### Build & Run

1. Download/Clone this project
2. Build the project using Android Studio or Android CLI and Run the app onto an Android smartphone (preferred) or emulator
3. Tilt and rotate the phone up/down and left and right direction to capture sensor readings.
5. Open Settings in the "Menu" and provide your InfluxDB account information that you can find in the [portal](https://cloud2.influxdata.com/login)
6. Hit "Save" and "Start Logging" to log the data
7. Open 'View Data' to see the graph visualization of the past few minutes of the data that was stored and quried from your InfluxDB.
8. Optionally navigate to your InfluxDB [dashboard](https://cloud2.influxdata.com/login) and query to see the smartphone sensor data that was just stored.

