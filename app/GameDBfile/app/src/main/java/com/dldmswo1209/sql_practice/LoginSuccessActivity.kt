package com.dldmswo1209.sql_practice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dldmswo1209.sql_practice.databinding.ActivityLoginSuccessBinding

class LoginSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val name = intent.getStringExtra("name")

        binding.textView.text = "안녕하세요 ${name}님"
    }
}