package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.geodrone.geodrone.adapter.ProjectsAdapter
import com.geodrone.geodrone.api.ApiActivity
import com.geodrone.geodrone.api.ApiClient
import com.geodrone.geodrone.databinding.ActivityProjectsBinding
import com.geodrone.geodrone.db.LocalAuthDatabase
import com.geodrone.geodrone.model.Project

class ProjectsActivity : ApiActivity() {

    private lateinit var binding: ActivityProjectsBinding
    private lateinit var adapter: ProjectsAdapter
    private var projectList = emptyList<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarProjects.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerProjects.layoutManager = LinearLayoutManager(this)

        binding.buttonNewProject.setOnClickListener {
            val intent = Intent(this, ProjectFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateProjectsList()
    }

    private fun updateProjectsList() {
        val localDb = LocalAuthDatabase(this)
        val localProjects = localDb.getAllProjects()

        executeApi(
            request = { ApiClient.service.projects() },
            onSuccess = { remoteProjects ->
                val combined = (remoteProjects + localProjects).distinctBy { it.id }
                showProjects(combined)
            },
            onError = {
                showProjects(localProjects)
            },
            onLoading = { loading -> binding.buttonNewProject.isEnabled = !loading }
        )
    }

    private fun showProjects(projects: List<Project>) {
        projectList = projects
        if (projects.isEmpty()) {
            binding.recyclerProjects.visibility = View.GONE
            binding.layoutEmptyProjects.visibility = View.VISIBLE
        } else {
            binding.recyclerProjects.visibility = View.VISIBLE
            binding.layoutEmptyProjects.visibility = View.GONE

            adapter = ProjectsAdapter(projectList) { project ->
                val intent = Intent(this, ProjectFormActivity::class.java).apply {
                    putExtra("EXTRA_PROJECT_ID", project.id)
                    putExtra("EXTRA_PROJECT_NAME", project.name)
                    putExtra("EXTRA_PROJECT_DESC", project.description)
                    putExtra("EXTRA_PROJECT_LOC", project.location)
                    putExtra("EXTRA_PROJECT_STATUS", project.status)
                }
                startActivity(intent)
            }
            binding.recyclerProjects.adapter = adapter
        }
    }
}
