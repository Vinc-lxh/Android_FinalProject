package edu.rosehulman.photovoicememo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        val topAnimation = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        val middleAnimation = AnimationUtils.loadAnimation(this,R.anim.middle_animation)
        val bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        val topTextView = findViewById<TextView>(R.id.topTextView)
        topTextView.startAnimation(topAnimation)

        val middleTextView = findViewById<TextView>(R.id.middleTextView)
        middleTextView.startAnimation(middleAnimation)

        val bottomTextView = findViewById<TextView>(R.id.bottomTextView)
        bottomTextView.startAnimation(bottomAnimation)

        val splashScreenTimeOut = 2200
        val homeIntent = Intent(this@SplashScreen, MainActivity::class.java)
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                startActivity(homeIntent)
                finish()
            },splashScreenTimeOut.toLong())
        }
    }
}