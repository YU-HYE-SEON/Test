package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Timer
import kotlin.concurrent.timer

class GameActivity : AppCompatActivity() {
    //타이머
    var time = 0
    var timerTask: Timer? = null
    var sec = 0

    //초 텍스트
    lateinit var secTextView: TextView

    //Game 클래스 안의 timeOut() 메소드를 사용하기 위함
    lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        //status bar 검정색으로 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        secTextView = findViewById(R.id.secTextView)
        game = findViewById(R.id.gameView)

        timerTask = timer(period = 10) {
            time++

            sec = time / 100

            runOnUiThread {
                secTextView.text = "$sec"
            }

            //30초까지만 카운트
            if (sec == 30) {
                //Game의 timeOut()를 메소드 호출하여 배경음과 여러 효과음을 반납한 뒤, 게임오버 효과음을 재생함
                game.timeOut()
            }
        }
    }

    //액티비티 종료 시 타이머 종료
    override fun onDestroy() {
        timerTask?.cancel()
        super.onDestroy()
    }
}