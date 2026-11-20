package com.geodrone.geodrone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geodrone.geodrone.databinding.ItemFlightBinding
import com.geodrone.geodrone.model.Flight

class FlightsAdapter(
    private val flights: List<Flight>,
    private val onFlightClick: (Flight) -> Unit
) : RecyclerView.Adapter<FlightsAdapter.FlightViewHolder>() {

    class FlightViewHolder(val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flights[position]
        with(holder.binding) {
            textItemFlightName.text = flight.name
            textItemFlightDate.text = "Fecha: ${flight.date}"

            val details = StringBuilder()
            flight.altitude?.let { details.append("Altitud: ${it}m ") }
            flight.overlap?.let { details.append(" | Solapamiento: ${it}%") }

            if (details.isNotBlank()) {
                textItemFlightAltitude.visibility = View.VISIBLE
                textItemFlightAltitude.text = details.toString()
            } else {
                textItemFlightAltitude.visibility = View.GONE
            }

            if (!flight.notes.isNullOrBlank()) {
                textItemFlightNotes.visibility = View.VISIBLE
                textItemFlightNotes.text = "Notas: ${flight.notes}"
            } else {
                textItemFlightNotes.visibility = View.GONE
            }

            buttonFlightUploadPhotos.setOnClickListener {
                onFlightClick(flight)
            }

            root.setOnClickListener {
                onFlightClick(flight)
            }
        }
    }

    override fun getItemCount(): Int = flights.size
}
