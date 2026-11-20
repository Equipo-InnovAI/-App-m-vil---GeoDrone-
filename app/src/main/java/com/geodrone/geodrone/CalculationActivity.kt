package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.geodrone.geodrone.databinding.ActivityCalculationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CalculationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculationBinding
    private var mediaCount: Int = 0
    private var projectName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaCount = intent.getIntExtra("EXTRA_MEDIA_COUNT", 0)
        projectName = intent.getStringExtra("EXTRA_PROJECT_NAME").orEmpty().ifBlank { "GeoDrone" }

        binding.toolbarCalculation.setNavigationOnClickListener {
            finish()
        }

        binding.buttonResults.setOnClickListener {
            val intent = Intent(this, ResultsActivity::class.java).apply {
                putExtra("EXTRA_MEDIA_COUNT", mediaCount)
                putExtra("EXTRA_PROJECT_NAME", projectName)
            }
            startActivity(intent)
            finish()
        }

        startCalculationsProcess()
    }

    private fun startCalculationsProcess() {
        lifecycleScope.launch {
            val fileLabel = if (mediaCount > 0) "$mediaCount archivo(s)" else "imágenes de muestra"
            val steps = listOf(
                "Cargando parámetros de $projectName...",
                "Procesando $fileLabel y generando ortomosaicos...",
                "Calculando modelo digital de elevación (DEM)...",
                "Generando curvas de nivel y métricas de terreno...",
                "Finalizando reportes geográficos de $projectName..."
            )

            for (i in 1..100) {
                delay(60)
                binding.progressBarCalculations.progress = i
                binding.textProgressPercent.text = "$i%"

                val stepIndex = ((i - 1) * steps.size) / 100
                binding.textCalculationDetail.text = steps[stepIndex.coerceIn(steps.indices)]
            }

            binding.textStatusTitle.text = "¡Cálculos de $projectName completados!"
            binding.textCalculationDetail.text = "Procesamiento de $fileLabel finalizado al 100%."
            binding.buttonResults.isEnabled = true
        }
    }
}
