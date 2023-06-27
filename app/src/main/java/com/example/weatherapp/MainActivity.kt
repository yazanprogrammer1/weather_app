package com.example.weatherapp

import android.app.DownloadManager.Request
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.ClientError
import com.android.volley.NetworkResponse
import com.android.volley.ServerError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.utils.TOKEN
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //.....
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // view binding
        binding.apply {
            btnSearch.setOnClickListener {
                val location = etLocation.text.toString().trim()
                tvError.visibility = View.GONE
                tvError.text = ""
                if (location.isNotEmpty()) {
                    // request to server
                    requestToServer(location)
                } else {
                    tvError.visibility = View.VISIBLE
                    tvError.text = "Enter a location"
                }
            }
        }

    }

    private fun requestToServer(location: String) {
        // Volley
        val queue = Volley.newRequestQueue(this)
        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$TOKEN"
        val stringRequest = StringRequest(com.android.volley.Request.Method.GET, url, { response ->
            // Handel Api response
            try {
                val jsonResponse = JSONObject(response)
                val mainObject: JSONObject = jsonResponse.getJSONObject("main")
                val temperature = mainObject.getDouble("temp")
                val humidity = mainObject.getInt("humidity")
                val windObject: JSONObject = jsonResponse.getJSONObject("wind")
                val windSpeed = windObject.getDouble("speed")
                val weatherArray: JSONArray = jsonResponse.getJSONArray("weather")
                var description: String? = null
                var main: String? = null
                if (weatherArray.length() > 0) {
                    val weatherObject = weatherArray.getJSONObject(0)
                    description = weatherObject.getString("description")
                    main = weatherObject.getString("main")
                }

                binding.layoutWeather.apply {
                    root.visibility = View.VISIBLE
                    // set Data
                    locationTitle.text = location
                    val roundedValue = ceil(temperature).toInt().toString()
                    tvTemperature.text = roundedValue
                    tvWindSpeed.text = "$windSpeed m/s"
                    tvHumidityValue.text = "$humidity%"
                    tvDescription.text = description.toString()
                    when (main) {
                        "Clear" -> {
                            ivStatus.setImageResource(R.drawable.clear)
                        }

                        "Rain" -> {
                            ivStatus.setImageResource(R.drawable.rain)
                        }

                        "Snow" -> {
                            ivStatus.setImageResource(R.drawable.snow)
                        }

                        "Clouds" -> {
                            ivStatus.setImageResource(R.drawable.cloud)
                        }
                    }

                    //click Listener
                    ivBack.setOnClickListener {
                        root.visibility = View.GONE
                    }
                }

            } catch (e: JSONException) {
                Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            if (error is ClientError || error is ServerError) {
                val response: NetworkResponse = error.networkResponse
                val statusCode = response.statusCode
                if (statusCode == 404) {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "Location Not Found"
                } else {
                    Toast.makeText(applicationContext, "Error To Connect Api", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        queue.add(stringRequest)
    }
}