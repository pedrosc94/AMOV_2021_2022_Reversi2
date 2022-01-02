package com.amov.reversi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeOnline: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_online)
    }

    fun onPlayAsServer(view: android.view.View) {
        onPlayOnline(0)
    }

    fun onPlayAsClient(view: android.view.View) {
        onPlayOnline(1)
    }

    private fun onPlayOnline(mode: Int) {
        val intent = Intent(this, GameOnline::class.java)
        intent.putExtra("mode", mode);
        startActivity(intent)
    }
}