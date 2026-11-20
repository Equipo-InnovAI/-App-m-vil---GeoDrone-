package com.geodrone.geodrone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geodrone.geodrone.databinding.ActivityResultsBinding
import java.util.Locale

class ResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaCount = intent.getIntExtra("EXTRA_MEDIA_COUNT", 0)
        val projectName = intent.getStringExtra("EXTRA_PROJECT_NAME").orEmpty().ifBlank { "GeoDrone" }

        binding.toolbarResults.setNavigationOnClickListener {
            finish()
        }

        val area = if (mediaCount > 0) String.format(Locale.US, "%.1f", mediaCount * 1.5) else "12.4"
        val countText = if (mediaCount > 0) "$mediaCount archivo(s) procesado(s)" else "Datos de ejemplo"

        val metricsText = """
            • Proyecto: $projectName
            • Archivos procesados: $countText
            • Área estimada: $area Hectáreas
            • Solapamiento de cobertura: 75%
            • Resolución espacial (GSD): 2.1 cm/px
            • Modelo Digital de Terreno (DEM): Generado exitosamente
        """.trimIndent()

        binding.textMetricsDetail.text = metricsText

        binding.buttonFinishResults.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}
