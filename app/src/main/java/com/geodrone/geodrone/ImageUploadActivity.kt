package com.geodrone.geodrone

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.geodrone.geodrone.adapter.ImagesAdapter
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.ApiImage
import com.geodrone.geodrone.databinding.ActivityImageUploadBinding
import com.geodrone.geodrone.model.Flight
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class ImageUploadActivity : ApiActivity() {

    private lateinit var binding: ActivityImageUploadBinding
    private val selectedImages = mutableListOf<Uri>()
    private var availableFlights = emptyList<Flight>()
    private var imagesAdapter: ImagesAdapter? = null

    private val selectGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            selectedImages.clear()
            selectedImages.addAll(uris)
            updatePhotoCountUI()
        }

    private val selectFilesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            selectedImages.clear()
            selectedImages.addAll(uris)
            updatePhotoCountUI()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarImageUpload.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerPhotos.layoutManager = LinearLayoutManager(this)
        imagesAdapter = ImagesAdapter()
        binding.recyclerPhotos.adapter = imagesAdapter

        binding.buttonSelectGallery.setOnClickListener {
            selectGalleryLauncher.launch("image/*")
        }

        binding.buttonSelectFiles.setOnClickListener {
            selectFilesLauncher.launch(arrayOf("*/*"))
        }

        binding.buttonUploadPhotos.setOnClickListener {
            uploadSelectedImages()
        }

        binding.buttonStartCalculations.setOnClickListener {
            val intent = Intent(this, CalculationActivity::class.java)
            startActivity(intent)
        }

        updatePhotoCountUI()
        loadFlights()
    }

    private fun loadFlights() {
        executeApi(
            request = { ApiClient.service.flights() },
            onSuccess = { flights ->
                availableFlights = flights
                val spinnerAdapter = ArrayAdapter(
                    this,
                    R.layout.simple_spinner_item,
                    flights.map { "${it.name} (${it.date})" }.ifEmpty {
                        listOf("No hay vuelos disponibles")
                    }
                )
                spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinnerFlight.adapter = spinnerAdapter
                binding.buttonUploadPhotos.isEnabled = flights.isNotEmpty()

                val passedFlightId = intent.getStringExtra("EXTRA_FLIGHT_ID")
                val selectedIndex = flights.indexOfFirst { it.id == passedFlightId }
                if (selectedIndex >= 0) {
                    binding.spinnerFlight.setSelection(selectedIndex)
                }

                binding.spinnerFlight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        flights.getOrNull(position)?.let(::loadImages)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

                if (flights.isEmpty()) {
                    showImages(emptyList())
                    Toast.makeText(this, "Crea un vuelo antes de subir imágenes.", Toast.LENGTH_LONG).show()
                } else {
                    flights.getOrNull(binding.spinnerFlight.selectedItemPosition)?.let(::loadImages)
                }
            },
            onLoading = { loading ->
                binding.spinnerFlight.isEnabled = !loading
                binding.buttonSelectGallery.isEnabled = !loading
                binding.buttonSelectFiles.isEnabled = !loading
            }
        )
    }

    private fun loadImages(flight: Flight) {
        executeApi(
            request = { ApiClient.service.images(flight.id) },
            onSuccess = ::showImages
        )
    }

    private fun showImages(images: List<ApiImage>) {
        imagesAdapter?.submitList(images)
        if (images.isEmpty()) {
            binding.textEmptyPhotos.visibility = View.VISIBLE
            binding.recyclerPhotos.visibility = View.GONE
        } else {
            binding.textEmptyPhotos.visibility = View.GONE
            binding.recyclerPhotos.visibility = View.VISIBLE
        }
    }

    private fun uploadSelectedImages() {
        val selectedFlight = availableFlights.getOrNull(binding.spinnerFlight.selectedItemPosition)
        if (selectedFlight == null) {
            Toast.makeText(this, "Selecciona un vuelo válido.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un archivo antes de subir.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageParts = try {
            selectedImages.map { uri ->
                val fileName = displayName(uri)
                val rawType = contentResolver.getType(uri)
                val mediaType = (rawType ?: "application/octet-stream").toMediaTypeOrNull()
                val body = object : RequestBody() {
                    override fun contentType(): MediaType? = mediaType

                    override fun writeTo(sink: BufferedSink) {
                        contentResolver.openInputStream(uri)?.use { input ->
                            input.source().use { source -> sink.writeAll(source) }
                        } ?: throw IOException("No se pudo leer el archivo seleccionado")
                    }
                }
                MultipartBody.Part.createFormData("images", fileName, body)
            }
        } catch (error: Exception) {
            handleApiError(error, authenticated = false)
            return
        }

        val flightPart = selectedFlight.id.toRequestBody("text/plain".toMediaType())
        executeApi(
            request = { ApiClient.service.uploadImages(flightPart, imageParts) },
            onSuccess = { response ->
                Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                selectedImages.clear()
                updatePhotoCountUI()
                loadImages(selectedFlight)
            },
            onLoading = { loading ->
                binding.buttonUploadPhotos.isEnabled = !loading
                binding.buttonSelectGallery.isEnabled = !loading
                binding.buttonSelectFiles.isEnabled = !loading
            }
        )
    }

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf(String::isNotBlank) ?: "archivo.dat"
    }

    private fun updatePhotoCountUI() {
        val count = selectedImages.size
        binding.textPhotoCount.text = "$count archivo(s) seleccionado(s)"
    }
}
