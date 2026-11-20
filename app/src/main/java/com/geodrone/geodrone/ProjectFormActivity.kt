package com.geodrone.geodrone

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.api.ProjectRequest
import com.geodrone.geodrone.databinding.ActivityProjectFormBinding
import com.geodrone.geodrone.db.LocalAuthDatabase

class ProjectFormActivity : ApiActivity() {

    private lateinit var binding: ActivityProjectFormBinding
    private val statusOptions = arrayOf("active", "completed", "archived")
    private val selectedMedia = mutableListOf<Uri>()
    private var editingProjectId: String? = null

    private val selectGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            selectedMedia.clear()
            selectedMedia.addAll(uris)
            updateMediaCountUI()
        }

    private val selectFilesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            selectedMedia.clear()
            selectedMedia.addAll(uris)
            updateMediaCountUI()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarProjectForm.setNavigationOnClickListener {
            finish()
        }

        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            statusOptions.map { it.replaceFirstChar { char -> char.uppercase() } }
        )
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerProjectStatus.adapter = spinnerAdapter

        editingProjectId = intent.getStringExtra("EXTRA_PROJECT_ID")
        intent.getStringExtra("EXTRA_PROJECT_NAME")?.let {
            binding.inputProjectName.setText(it)
            binding.toolbarProjectForm.title = "Editar Proyecto"
        }
        intent.getStringExtra("EXTRA_PROJECT_DESC")?.let { binding.inputProjectDescription.setText(it) }
        intent.getStringExtra("EXTRA_PROJECT_LOC")?.let { binding.inputProjectLocation.setText(it) }
        intent.getStringExtra("EXTRA_PROJECT_STATUS")?.let { status ->
            val idx = statusOptions.indexOf(status)
            if (idx >= 0) binding.spinnerProjectStatus.setSelection(idx)
        }

        binding.buttonFormGallery.setOnClickListener {
            selectGalleryLauncher.launch("image/*")
        }

        binding.buttonFormFiles.setOnClickListener {
            selectFilesLauncher.launch(arrayOf("*/*"))
        }

        binding.buttonFormStartCalculations.setOnClickListener {
            val intent = Intent(this, CalculationActivity::class.java).apply {
                putExtra("EXTRA_MEDIA_COUNT", selectedMedia.size)
                putExtra("EXTRA_PROJECT_NAME", binding.inputProjectName.text?.toString().orEmpty())
            }
            startActivity(intent)
        }

        binding.buttonSaveProject.setOnClickListener {
            if (validateForm()) {
                val name = binding.inputProjectName.text.toString().trim()
                val description = binding.inputProjectDescription.text?.toString()?.trim().orEmpty()
                val location = binding.inputProjectLocation.text?.toString()?.trim().orEmpty()
                val selectedStatus = statusOptions[binding.spinnerProjectStatus.selectedItemPosition]

                val localDb = LocalAuthDatabase(this)
                val mediaUriStrings = selectedMedia.map { it.toString() }
                localDb.saveOrUpdateProject(
                    editingProjectId,
                    name,
                    description,
                    location,
                    selectedStatus,
                    mediaUriStrings
                )

                executeApi(
                    request = {
                        ApiClient.service.createProject(
                            ProjectRequest(
                                name = name,
                                description = description.ifBlank { null },
                                location = location.ifBlank { null },
                                status = selectedStatus
                            )
                        )
                    },
                    onSuccess = {
                        Toast.makeText(this, "Proyecto guardado en PostgreSQL (Docker) y localmente", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = {
                        Toast.makeText(this, "Guardado localmente (servidor Docker no disponible)", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onLoading = { loading -> binding.buttonSaveProject.isEnabled = !loading }
                )
            }
        }

        binding.buttonCancelProject.setOnClickListener {
            finish()
        }

        updateMediaCountUI()
    }

    private fun updateMediaCountUI() {
        val count = selectedMedia.size
        binding.textFormMediaCount.text = "$count archivo(s) seleccionado(s)"
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val name = binding.inputProjectName.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            binding.layoutProjectName.error = "El nombre del proyecto es obligatorio"
            isValid = false
        } else {
            binding.layoutProjectName.error = null
        }

        return isValid
    }
}
