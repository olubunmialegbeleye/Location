package com.example.locationarticle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    override fun onStart() {
        super.onStart()
        findViewById<Button>(R.id.single_java).setOnClickListener { startActivity(Intent(this, SingleJavaActivity::class.java)) }
        findViewById<Button>(R.id.observable_java).setOnClickListener { startActivity(Intent(this, ObserveJavaActivity::class.java)) }
        findViewById<Button>(R.id.single_kt).setOnClickListener { startActivity(Intent(this, SingleKtActivity::class.java)) }
        findViewById<Button>(R.id.observable_kt).setOnClickListener { startActivity(Intent(this, ObserveKtActivity::class.java)) }
    }
}