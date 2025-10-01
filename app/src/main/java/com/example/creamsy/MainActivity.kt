package com.example.creamsy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivityKtx : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}