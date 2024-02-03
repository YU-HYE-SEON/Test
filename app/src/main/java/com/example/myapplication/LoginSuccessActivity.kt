package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.myapplication.databinding.ActivityLoginSuccessBinding

class LoginSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val name = intent.getStringExtra("name")

        binding.textView.text = "안녕하세요 ${name}님"

        //로그인 성공 시 1초 후 대기화면으로 화면 전환
        Handler(Looper.getMainLooper()).postDelayed({
            val intent2= Intent(this, WaitActivity::class.java)
            startActivity(intent2)
            //액티비티 종료
            finish()
        },1000)
    }
}