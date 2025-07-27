package com.luminaredenero.fonetikku

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.MaterialSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var inputField: EditText
    private lateinit var processBtn: Button
    private lateinit var resetBtn: Button
    private lateinit var copyBtn: Button
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var indicatorText: TextView
    private lateinit var resultArea: TextView
    private lateinit var themeSwitch: MaterialSwitch
    private var isProcessing = false
    private var currentJob: Job? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("fonetikku_prefs", MODE_PRIVATE)
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
        setupListeners()
    }

    private fun applyTheme() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun initializeViews() {
        inputField = findViewById<EditText>(R.id.inputField)
        processBtn = findViewById<Button>(R.id.processBtn)
        resetBtn = findViewById<Button>(R.id.resetBtn)
        copyBtn = findViewById<Button>(R.id.copyBtn)
        modeRadioGroup = findViewById<RadioGroup>(R.id.modeRadioGroup)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        indicatorText = findViewById<TextView>(R.id.indicatorText)
        resultArea = findViewById<TextView>(R.id.resultArea)
        themeSwitch = findViewById<MaterialSwitch>(R.id.themeSwitch)
    }

    private fun setupListeners() {
        processBtn.setOnClickListener {
            if (isProcessing) {
                pauseProcessing()
            } else {
                startProcessing()
            }
        }
        resetBtn.setOnClickListener { resetAll() }
        copyBtn.setOnClickListener { copyResults() }
        themeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            applyTheme()
        }
    }

    private fun startProcessing() {
        val inputText = inputField.text.toString().trim()
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Input tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val linesToProcess = inputText.split(Regex("\\R")).filter { it.isNotBlank() }
        if (linesToProcess.isEmpty()) return

        isProcessing = true
        resetBtn.isEnabled = false
        processBtn.text = "Jeda"
        resultArea.text = ""
        progressBar.visibility = View.VISIBLE
        progressBar.max = linesToProcess.size
        progressBar.progress = 0
        indicatorText.text = "0/${linesToProcess.size}"

        currentJob = CoroutineScope(Dispatchers.Main).launch {
            val resultsBuilder = StringBuilder()
            linesToProcess.forEachIndexed { index, line ->
                if (!isActive) return@forEachIndexed
                val resultLine = processSingleLine(line)
                resultsBuilder.append(resultLine).append("\n")
                resultArea.text = resultsBuilder.toString()
                progressBar.progress = index + 1
                indicatorText.text = "${index + 1}/${linesToProcess.size}"
                delay(50)
            }
            finishProcessing()
        }
    }

    private fun processSingleLine(line: String): String {
        return if (modeRadioGroup.checkedRadioButtonId == R.id.directModeRadio) {
            Fonetikku.konversi(line.trim())
        } else { // Smart Mode
            val parts = line.split(";", 3)
            if (parts.size == 3) {
                val regex = Regex("^(.*?)\\(EN Asli\\)$")
                val matchResult = regex.find(parts[2].trim())
                if (matchResult != null) {
                    val ipa = matchResult.groupValues[1].trim()
                    val fonetikku = Fonetikku.konversi(ipa)
                    "${parts[0]};${parts[1]};$fonetikku(EN Asli)"
                } else {
                    line
                }
            } else {
                line
            }
        }
    }

    private fun pauseProcessing() {
        processBtn.text = "Proses"
        isProcessing = false
        resetBtn.isEnabled = true
        currentJob?.cancel()
        indicatorText.text = "Dijeda"
    }

    private fun finishProcessing() {
        isProcessing = false
        resetBtn.isEnabled = true
        processBtn.text = "Proses"
        indicatorText.text = "Selesai"
    }

    private fun resetAll() {
        currentJob?.cancel()
        isProcessing = false
        inputField.text.clear()
        resultArea.text = ""
        indicatorText.text = ""
        progressBar.visibility = View.INVISIBLE
        progressBar.progress = 0
        processBtn.text = "Proses"
        resetBtn.isEnabled = true
    }

    private fun copyResults() {
        val resultsText = resultArea.text.toString()
        if (resultsText.isBlank()) {
            Toast.makeText(this, "Tidak ada hasil untuk disalin", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Fonetikku Results", resultsText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Hasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }
}
