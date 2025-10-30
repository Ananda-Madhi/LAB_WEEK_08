package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.R
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.lab_week_08.worker.ThirdWorker


class MainActivity : AppCompatActivity() {
    private val workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val Id = "001"

        val firstRequest = OneTimeWorkRequestBuilder<FirstWorker>()
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, Id))
            .build()

        val secondRequest = OneTimeWorkRequestBuilder<SecondWorker>()
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, Id))
            .build()

        val thirdRequest = OneTimeWorkRequestBuilder<ThirdWorker>()
            .setConstraints(networkConstraints)
            .build()


        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .then(thirdRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info?.state?.isFinished == true) {
                    showResult("First process is done")
                    launchNotificationService()
                }
            }

        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info?.state?.isFinished == true) {
                    showResult("Second process is done")
                }
            }
        workManager.getWorkInfoByIdLiveData(thirdRequest.id).observe(this) { info ->
            if (info?.state?.isFinished == true) {
                showResult("Third process is done")
                launchSecondNotificationService()
            }
            }
    }

    private fun getIdInputData(IdKey: String, IdValue: String) = Data.Builder()
        .putString(IdKey, IdValue)
        .build()

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun launchNotificationService() {
        NotificationService.trackingCompletion.observe(this) { id ->
            showResult("Process for Notification Channel ID $id is done!")
        }

        val serviceIntent = Intent(this, NotificationService::class.java).apply {
            putExtra(EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun launchSecondNotificationService() {
        SecondNotificationService.trackingCompletion.observe(this) { id ->
            showResult("Final process for Channel ID $id is done!")
        }
        val serviceIntent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(SecondNotificationService.EXTRA_ID, "002")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }


    companion object {
        const val EXTRA_ID = "Id"
    }
}