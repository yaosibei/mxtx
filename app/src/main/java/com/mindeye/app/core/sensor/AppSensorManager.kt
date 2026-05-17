package com.mindeye.app.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.mindeye.app.core.model.SensorData

/**
 * 传感器管理器
 * 负责采集加速度计、陀螺仪、光线传感器等数据
 */
class AppSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var lightSensor: Sensor? = null

    private var accelerometerData = floatArrayOf(0f, 0f, 0f)
    private var gyroscopeData = floatArrayOf(0f, 0f, 0f)
    private var lightLevel = 0f

    private var isListening = false

    /**
     * 开始监听传感器
     */
    fun startListening() {
        if (isListening) return

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        isListening = true
        Log.d(TAG, "传感器监听已启动")
    }

    /**
     * 停止监听传感器
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        isListening = false
        Log.d(TAG, "传感器监听已停止")
    }

    /**
     * 获取当前传感器数据
     */
    fun getCurrentSensorData(): SensorData {
        return SensorData(
            accelerometer = accelerometerData.clone(),
            gyroscope = gyroscopeData.clone(),
            light = lightLevel
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerData[0] = event.values[0]
                accelerometerData[1] = event.values[1]
                accelerometerData[2] = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeData[0] = event.values[0]
                gyroscopeData[1] = event.values[1]
                gyroscopeData[2] = event.values[2]
            }
            Sensor.TYPE_LIGHT -> {
                lightLevel = event.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 传感器精度变化时的回调（可忽略）
    }

    /**
     * 检测用户是否在移动
     */
    fun isMoving(): Boolean {
        val acceleration = kotlin.math.sqrt(
            accelerometerData[0] * accelerometerData[0] +
            accelerometerData[1] * accelerometerData[1] +
            accelerometerData[2] * accelerometerData[2]
        )
        return kotlin.math.abs(acceleration - 9.8) > 1.5
    }

    /**
     * 检测运动状态 — 返回描述性字符串。
     */
    fun detectMotionState(): String {
        val acceleration = kotlin.math.sqrt(
            accelerometerData[0] * accelerometerData[0] +
            accelerometerData[1] * accelerometerData[1] +
            accelerometerData[2] * accelerometerData[2]
        )
        val deviation = kotlin.math.abs(acceleration - 9.8)
        return when {
            deviation < 0.5 -> "still"
            deviation < 2.0 -> "walking"
            deviation < 5.0 -> "running"
            else -> "vehicle"
        }
    }

    /**
     * 完全释放传感器资源。
     */
    fun release() {
        stopListening()
    }

    /**
     * 检测用户是否跌倒（突然的加速度变化）
     */
    fun detectFall(): Boolean {
        val acceleration = kotlin.math.sqrt(
            accelerometerData[0] * accelerometerData[0] +
            accelerometerData[1] * accelerometerData[1] +
            accelerometerData[2] * accelerometerData[2]
        )
        // 跌倒时会有突然的加速度峰值
        return acceleration > 15.0
    }

    companion object {
        private const val TAG = "AppSensorManager"
    }
}
