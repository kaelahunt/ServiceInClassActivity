package edu.temple.basictimer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerInfo {
    private val _currentTime = MutableStateFlow(0)
    val currentTime = _currentTime.asStateFlow()


    fun postNewTime(currentTime: Int) {
        _currentTime.value = currentTime
    }
}

class TimerService : Service() {

    private var isRunning = false

    lateinit var t: TimerThread

    private var paused = false

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val isPaused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            if (!isPaused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }


        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()

        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused
        }
    }

    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {
                    Log.d("Countdown", i.toString())

                    TimerInfo.postNewTime(i)

                    while (paused);
                    sleep(1000)

                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }


}