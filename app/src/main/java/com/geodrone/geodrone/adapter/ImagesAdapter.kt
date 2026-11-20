package com.geodrone.geodrone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geodrone.geodrone.api.ApiImage
import com.geodrone.geodrone.databinding.ItemImageBinding

class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {
    private var images: List<ApiImage> = emptyList()

    class ImageViewHolder(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        with(holder.binding) {
            textImageName.text = image.originalName ?: image.filename
            textImageCapturedAt.text = image.capturedAt.orEmpty().ifBlank { "Fecha EXIF no disponible" }
            val coordinate = image.coordinate
            textImageCoordinates.text = if (coordinate?.latitude != null && coordinate.longitude != null) {
                "GPS: ${coordinate.latitude}, ${coordinate.longitude}"
            } else {
                "GPS: no disponible"
            }
        }
    }

    override fun getItemCount(): Int = images.size

    fun submitList(items: List<ApiImage>) {
        images = items
        notifyDataSetChanged()
    }
}
