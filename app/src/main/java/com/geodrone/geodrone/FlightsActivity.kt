package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.adapter.FlightsAdapter
import com.geodrone.geodrone.databinding.ActivityFlightsBinding

class FlightsActivity : ApiActivity() {

    private lateinit var binding: ActivityFlightsBinding
    private var projectId: String = ""
    private var projectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectId = intent.getStringExtra("EXTRA_PROJECT_ID").orEmpty()
        projectName = intent.getStringExtra("EXTRA_PROJECT_NAME").orEmpty()

        binding.toolbarFlights.setNavigationOnClickListener {
            finish()
        }

        binding.textProjectName.text = "Proyecto: $projectName"
        binding.recyclerFlights.layoutManager = LinearLayoutManager(this)

        binding.buttonNewFlight.setOnClickListener {
            val intent = Intent(this, FlightFormActivity::class.java).apply {
                putExtra("EXTRA_PROJECT_ID", projectId)
                putExtra("EXTRA_PROJECT_NAME", projectName)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateFlightsList()
    }

    private fun updateFlightsList() {
        executeApi(
            request = { ApiClient.service.flights(projectId) },
            onSuccess = { flights ->
                if (flights.isEmpty()) {
                    binding.recyclerFlights.visibility = View.GONE
                    binding.layoutEmptyFlights.visibility = View.VISIBLE
                } else {
                    binding.recyclerFlights.visibility = View.VISIBLE
                    binding.layoutEmptyFlights.visibility = View.GONE

                    val adapter = FlightsAdapter(flights) { flight ->
                        val intent = Intent(this, ImageUploadActivity::class.java).apply {
                            putExtra("EXTRA_FLIGHT_ID", flight.id)
                            putExtra("EXTRA_FLIGHT_NAME", flight.name)
                        }
                        startActivity(intent)
                    }
                    binding.recyclerFlights.adapter = adapter
                }
            },
            onLoading = { loading -> binding.buttonNewFlight.isEnabled = !loading }
        )
    }
}
