package com.raywenderlich.currencyapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.currencyapp.R
import com.raywenderlich.currencyapp.databinding.ActivityMainBinding
import com.raywenderlich.currencyapp.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
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

        vm.toastMessage.observe(this, Observer {
            showSafeToast(window.decorView, it)
        })
    }

    private fun showSafeToast(view: View, text: String) { // Prevent appearance of multiply toasts at once
        if (Calendar.getInstance().timeInMillis >= vm.toastShowTime + 4000L) {
            val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
            //snackbar.view.background = resources.getDrawable(R.drawable.snackbar_background)
            snackbar.show()
            vm.toastShowTime = Calendar.getInstance().timeInMillis
        }
    }
}