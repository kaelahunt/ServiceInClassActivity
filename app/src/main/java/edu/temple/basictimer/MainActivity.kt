package edu.temple.basictimer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    var timerBinder : TimerService.TimerBinder? = null

    val textView : TextView by lazy {
        findViewById(R.id.textView)
    }

    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            timerBinder = p1 as TimerService.TimerBinder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            timerBinder = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                TimerInfo.currentTime.collect {
                    textView.text = it.toString()
                }
            }
        }

        bindService(
            Intent ( this, TimerService::class.java ),
            serviceConnection,
            BIND_AUTO_CREATE

        )

        findViewById<Button>(R.id.startButton).setOnClickListener {
            with(it as Button){
                timerBinder?.run{
                    if(!isRunning){
                        timerBinder?.start(100)
                        (it as Button).text = "Pause"
                    } else {
                        timerBinder?.pause()
                        (it as Button).text = if (isPaused){
                            "resume"
                        } else {
                            "pause"
                        }
                    }
                }
            }
        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
            findViewById<Button>(R.id.startButton).text = "Start"

        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem
    ) = when (item.itemId) {
            R.id.action_start_timer  -> {
                timerBinder?.start(100)
                true
            }
            R.id.action_stop_timer -> {
                timerBinder?.stop();
                true
            }
        else -> false
    }
}