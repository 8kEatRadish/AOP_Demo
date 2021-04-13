package com.shawn.krouter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.shawn.krouter.layout.SecondActivityLayout

class SecondActivity : AppCompatActivity() {
    lateinit var layout: SecondActivityLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = SecondActivityLayout(this)
        setContentView(layout)
        layout.button.setOnClickListener {
            Snackbar.make(it, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}