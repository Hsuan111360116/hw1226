package com.example.lab17

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btnQuery: Button
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Apply insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnQuery = findViewById(R.id.btnQuery)
        progressDialog = ProgressDialog(this).apply {
            setMessage("Fetching data, please wait...")
            setCancelable(false)
        }

        // Set button click listener
        btnQuery.setOnClickListener {
            btnQuery.isEnabled = false
            progressDialog.show()
            fetchAirQualityData()
        }
    }

    private fun fetchAirQualityData() {
        val url = "https://api.italkutalk.com/api/air"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { progressDialog.dismiss() }
                if (!response.isSuccessful) {
                    runOnUiThread {
                        showError("Failed to fetch data. Code: ${response.code}")
                        btnQuery.isEnabled = true
                    }
                    return
                }

                val json = response.body?.string()
                if (json == null) {
                    runOnUiThread {
                        showError("Received empty response.")
                        btnQuery.isEnabled = true
                    }
                    return
                }

                try {
                    val myObject = Gson().fromJson(json, MyObject::class.java)
                    runOnUiThread { showAirQualityDialog(myObject) }
                } catch (e: JsonSyntaxException) {
                    runOnUiThread {
                        showError("Failed to parse response.")
                        btnQuery.isEnabled = true
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressDialog.dismiss()
                    showError("Request failed: ${e.message}")
                    btnQuery.isEnabled = true
                }
            }
        })
    }

    private fun showAirQualityDialog(myObject: MyObject) {
        val items = myObject.result.records.map { "Region: ${it.SiteName}, Status: ${it.Status}" }

        AlertDialog.Builder(this)
            .setTitle("Air Quality in Taipei")
            .setItems(items.toTypedArray(), null)
            .setPositiveButton("OK") { _, _ -> }
            .show()

        btnQuery.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

// Define data classes for JSON parsing
