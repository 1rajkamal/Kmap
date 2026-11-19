package com.breadcrumbradar.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CompassData(
    val azimuthDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isCalibrated: Boolean = true,
    val sensorAvailable: Boolean = true
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassFlow = MutableStateFlow(CompassData())
    val compassFlow: StateFlow<CompassData> = _compassFlow.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Accelerometer + Magnetometer fallback buffers
    private val gravityValues = FloatArray(3)
    private val geoMagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeoMagnetic = false

    // Circular low-pass filter accumulators
    private var smoothedSin = 0.0
    private var smoothedCos = 1.0
    private val filterAlpha = 0.2 // Smoothing factor

    private var isListening = false

    fun startListening() {
        if (isListening || sensorManager == null) return

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
            _compassFlow.value = _compassFlow.value.copy(sensorAvailable = true)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            isListening = true
            _compassFlow.value = _compassFlow.value.copy(sensorAvailable = true)
        } else {
            _compassFlow.value = _compassFlow.value.copy(sensorAvailable = false)
        }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var azimuthRad = 0.0

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuthRad = orientationAngles[0].toDouble()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geoMagneticValues, 0, 3)
            hasGeoMagnetic = true
        }

        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR && hasGravity && hasGeoMagnetic) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                geoMagneticValues
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                azimuthRad = orientationAngles[0].toDouble()
            }
        }

        // Circular low-pass filter to prevent jump between 359 and 0
        val targetSin = sin(azimuthRad)
        val targetCos = cos(azimuthRad)
        smoothedSin = smoothedSin + filterAlpha * (targetSin - smoothedSin)
        smoothedCos = smoothedCos + filterAlpha * (targetCos - smoothedCos)

        val filteredRad = atan2(smoothedSin, smoothedCos)
        var degrees = Math.toDegrees(filteredRad).toFloat()
        if (degrees < 0) degrees += 360f

        val isCalibrated = event.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE &&
                event.accuracy != SensorManager.SENSOR_STATUS_ACCURACY_LOW

        _compassFlow.value = CompassData(
            azimuthDegrees = degrees,
            accuracy = event.accuracy,
            isCalibrated = isCalibrated,
            sensorAvailable = true
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val isCalibrated = accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE &&
                accuracy != SensorManager.SENSOR_STATUS_ACCURACY_LOW
        _compassFlow.value = _compassFlow.value.copy(
            accuracy = accuracy,
            isCalibrated = isCalibrated
        )
    }
}
