package com.example.myapplication

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class GameoverActivity : AppCompatActivity() {
    lateinit var gameoverView: ImageView    //게임오버화면 이미지
    lateinit var gameoverText: TextView     //게임오버화면 텍스트 ("터치하면 대기화면으로 돌아갑니다")

    override fun onCreate(savedInstanceState: Bundle?) {
        //status bar 검정색으로 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameover)

        gameoverView = findViewById(R.id.gameoverView)
        gameoverText = findViewById(R.id.gameoverText)

        //텍스트가 깜빡거리는 애니메이션 설정
        val anim = ObjectAnimator.ofFloat(gameoverText, "alpha", 0f, 1f)
        //애니메이션 한 사이클의 시간 (0.5초)
        anim.duration = 500
        //애니메이션 반복 방식 (REVERSE: 순방향, 역방향 번갈아가며)
        anim.repeatMode = ObjectAnimator.REVERSE
        // 애니메이션 무한 반복
        anim.repeatCount = ObjectAnimator.INFINITE
        // 애니메이션 시작
        anim.start()

        //화면 터치 시 대기화면으로 돌아감
        gameoverView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                var intent = Intent(this, WaitActivity::class.java)
                startActivity(intent)

                //액티비티 종료
                finish()

                //애니메이션 해제
                anim.cancel()
            }
            true
        }
    }
}