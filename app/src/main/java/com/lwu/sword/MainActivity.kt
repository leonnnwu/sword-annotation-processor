package com.lwu.sword

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.lwu.Navigator
import com.lwu.sword_annotation.NewIntent

@NewIntent
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Navigator.startMainActivity(this)
    }
}
