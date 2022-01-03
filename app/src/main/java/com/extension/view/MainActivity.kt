package com.extension.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.extension.lib_view.TestMaven

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TestMaven(2)

    }
}