package com.geodrone.geodrone

import android.app.DatePickerDialog
import android.os.Bundle
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.FlightRequest
import com.geodrone.geodrone.databinding.ActivityFlightFormBinding
import java.util.Calendar
import java.util.Locale

class FlightFormActivity : ApiActivity() {

    private lateinit var binding: ActivityFlightFormBinding
    private var projectId: String = ""
    private var projectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlightFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectId = intent.getStringExtra("EXTRA_PROJECT_ID").orEmpty()
        projectName = intent.getStringExtra("EXTRA_PROJECT_NAME").orEmpty()

        binding.toolbarFlightForm.setNavigationOnClickListener {
            finish()
        }

        binding.textProjectTitle.text = "Proyecto asociado: $projectName"

        setupDatePicker()

        binding.buttonSaveFlight.setOnClickListener {
            if (validateForm()) {
                val name = binding.inputFlightName.text.toString().trim()
                val date = binding.inputFlightDate.text.toString().trim()
                val altitude = binding.inputFlightAltitude.text?.toString()?.toDoubleOrNull()
                val overlap = binding.inputFlightOverlap.text?.toString()?.toDoubleOrNull()
                val notes = binding.inputFlightNotes.text?.toString()?.trim().orEmpty()

                executeApi(
                    request = {
                        ApiClient.service.createFlight(
                            FlightRequest(
                                name = name,
                                date = date,
                                altitude = altitude,
                                overlap = overlap,
                                notes = notes.ifBlank { null },
                                projectId = projectId
                            )
                        )
                    },
                    onSuccess = {
                        finish()
                    },
                    onLoading = { loading -> binding.buttonSaveFlight.isEnabled = !loading }
                )
            }
        }

        binding.buttonCancelFlight.setOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            binding.inputFlightDate.setText(formattedDate)
            binding.layoutFlightDate.error = null
        }

        val openPicker = {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.inputFlightDate.setOnClickListener { openPicker() }
        binding.layoutFlightDate.setEndIconOnClickListener { openPicker() }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.inputFlightName.text?.toString()?.trim().orEmpty()
        val date = binding.inputFlightDate.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            binding.layoutFlightName.error = "El nombre del vuelo es obligatorio"
            isValid = false
        } else {
            binding.layoutFlightName.error = null
        }

        if (date.isEmpty()) {
            binding.layoutFlightDate.error = "La fecha es obligatoria"
            isValid = false
        } else {
            binding.layoutFlightDate.error = null
        }

        return isValid
    }
}
