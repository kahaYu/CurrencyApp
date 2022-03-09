package com.raywenderlich.currencyapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.ActivityMainBinding
import com.raywenderlich.currencyapp.utils.hideSystemUI

class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        hideSystemUI()
        supportActionBar?.hide()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            false

    }
}