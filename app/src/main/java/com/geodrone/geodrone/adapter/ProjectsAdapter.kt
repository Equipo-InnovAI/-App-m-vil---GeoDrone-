package com.geodrone.geodrone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geodrone.geodrone.databinding.ItemProjectBinding
import com.geodrone.geodrone.model.Project

class ProjectsAdapter(
    private val projects: List<Project>,
    private val onProjectClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        with(holder.binding) {
            textItemProjectName.text = project.name
            textItemProjectStatus.text = project.status.replaceFirstChar { it.uppercase() }
            
            if (project.description.isNotBlank()) {
                textItemProjectDesc.visibility = View.VISIBLE
                textItemProjectDesc.text = project.description
            } else {
                textItemProjectDesc.visibility = View.GONE
            }

            if (project.location.isNotBlank()) {
                textItemProjectLocation.visibility = View.VISIBLE
                textItemProjectLocation.text = "Ubicación: ${project.location}"
            } else {
                textItemProjectLocation.visibility = View.GONE
            }

            buttonViewFlights.setOnClickListener {
                onProjectClick(project)
            }

            root.setOnClickListener {
                onProjectClick(project)
            }
        }
    }

    override fun getItemCount(): Int = projects.size
}
