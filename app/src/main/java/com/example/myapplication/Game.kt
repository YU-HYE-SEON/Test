package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

//공용 클래스 Game의 뷰 확장
class Game(context: Context?, attr: AttributeSet?) : View(context, attr) {
    //이미지 파일 (플레이어, 방향키, 공격 버튼, 적, 생명)
    lateinit var player: Bitmap
    lateinit var moveButton: Bitmap
    lateinit var attackButton: Bitmap

    //스크린의 너비값과 높이값
    var scrw = 0
    var scrh = 0

    //플레이어 관련 변수
    var xd = -120f      //플레이어 이미지 기준점 x좌표  (*게임 시작 시 중앙에 배치하기 위해 임의로 -120f값 넣음)
    var yd = -80f       //플레이어 이미지 기준점 y좌표  (*게임 시작 시 중앙에 배치하기 위해 임의로 -80f값 넣음)
    var count = 0       //플레이어의 이동을 위한 카운트 수를 저장할 변수
    var n = 0           //플레이어 이미지 전환을 판별하기 위한 변수
    var start = false   //방향키 클릭 유무 (false(=0)(*안 누름)/true(=1)(*누름))
    var hp = 3          //플레이어 최대 체력
    var LN = IntArray(3)                //플레이어 라이프 번호
    private var DirButton: String? = null   //플레이어가 정지 상태에 있는 동안 어떤 방향키를 클릭했는지 저장할 문자열 변수
    private var DirButton2: String? = null  //플레이어가 이동하고 있는 상태에서 어떤 방향키를 클릭했는지 저장할 문자열 변수

    //적 관련 변수   (화면에 적을 5개까지 표시) (적 1~5번의 각 좌표/카운트 수/생명 값/이동 방향을 제어하기 위해 배열형식으로 설정)
    var exd = FloatArray(5)     //적의 x좌표
    var eyd = FloatArray(5)     //적의 y좌표
    var count2 = IntArray(5)    //적의 이동을 위한 카운트 수를 저장할 변수
    var eLife = IntArray(5)     //적의 생명 값
    var random = Random              //랜덤 변수
    var EC = 5                       //화면에 보이는 적 수
    var EN = IntArray(5)        //적 번호
    var ED = IntArray(5)        //적의 방향
    private val EDirButton = arrayOfNulls<String>(5)    //적의 이동 방향을 저장할 문자열 변수

    //미사일 관련 변수 (화면에 미사일을 10개까지 표시) (미사일 1~10번의 각 좌표/번호/방향을 제어하기 위해 배열형식으로 설정)
    var mx = FloatArray(10)         //미사일 x좌표
    var my = FloatArray(10)         //미사일 y좌표
    var missileCount = 0                 //미사일 수
    var missileNum = IntArray(10)   //미사일 번호
    var md = IntArray(10)           //미사일 방향
    var MD = 4                           //미사일 초기 방향 (좌:1 우:2 상:3 하:4)

    //thread라는 이름의 게임 스레드를 설정
    var thread: GameThread? = null

    //효과음   (배경음, 미사일 발사, 플레이어 데미지, 적 데미지, 게임오버, 게임 클리어)
    var bgBGM = MediaPlayer.create(context, R.raw.background_bgm)
    var soundPool = SoundPool.Builder().build()
    val playerAttack = soundPool.load(context, R.raw.player_attack, 1)
    val playerDamage = soundPool.load(context, R.raw.player_damage, 1)
    val enemyDamage = soundPool.load(context, R.raw.enemy_damage, 1)
    var gameoverSound = MediaPlayer.create(context, R.raw.gameover_sound)
    var gameClearSound = MediaPlayer.create(context, R.raw.gameclear_sound)

    //초기화 블록
    //생성자 생성 -> Context는 앱에 대한 다양한 정보가 들어 있다. AttributeSet은 xml정보를 가져온다.
    init {
        //부모 클래스의 생성자를 불러와서 초기화시킨다.

        //배경음 재생
        bgBGM.start()
        //배경음 무한재생
        bgBGM.setLooping(true)
    }

    //뷰의 크기가 변경될 때 호출
    protected override fun onSizeChanged(sw: Int, sh: Int, esw: Int, esh: Int) {
        super.onSizeChanged(sw, sh, esw, esh)

        scrw = sw   //뷰의 폭 정보 저장
        scrh = sh   //뷰의 높이 정보 저장

        //1~5번 적의 생명력 저장
        for (i in 0..4) {
            //모든 적이 미사일에 2번까지 견디도록 설정
            eLife[i] = 2
            //적 활성화 (1:활성화 0:비활성화)
            EN[i] = 1
        }

        //플레이어 라이프 활성화
        for (i in 2 downTo 0) {
            LN[i] = 1
        }

        //스레드 값이 비었다면
        if (thread == null) {
            //GameThread()함수를 돌린 값을 넣어줌
            thread = GameThread()
            //스레드 시작
            thread!!.start()
        }
    }

    // 뷰가 윈도우에서 분리될 때마다 발생
    protected override fun onDetachedFromWindow() {
        //스레드의 run 값으로 false 값을 줌
        thread!!.run = false
        //부모 클래스의 멤버 변수를 참조
        super.onDetachedFromWindow()
    }

    //캔버스 위에 그리기
    protected override fun onDraw(canvas: Canvas) {
        //플레이어 이미지 파일 설정
        when (n) {
            //게임시작 시
            0 -> {
                player = BitmapFactory.decodeResource(getResources(), R.drawable.player04)
            }
            //좌
            in 1..3 -> {
                player = BitmapFactory.decodeResource(getResources(), R.drawable.player01)
            }
            //우
            in 4..6 -> {
                player = BitmapFactory.decodeResource(getResources(), R.drawable.player02)
            }
            //상
            in 7..9 -> {
                player = BitmapFactory.decodeResource(getResources(), R.drawable.player03)
            }
            //하
            in 10..12 -> {
                player = BitmapFactory.decodeResource(getResources(), R.drawable.player04)
            }
        }

        //플레이어 이미지 크기 설정
        player = Bitmap.createScaledBitmap(player, scrw / 8, scrh / 4, true)
        //캔버스에 플레이어 그리기
        canvas.drawBitmap(player, (scrw / 2f) + xd, (scrh / 2f) + yd, null)

        //enemy에 5개까지의 비트맵 정보를 저장함
        val enemy: Array<Bitmap?> = arrayOfNulls<Bitmap>(5)

        for (i in 0..4) {
            //i번째 적 번호가 1이면(적이 활성화된 상태라면)
            if (EN[i] == 1) {
                //방향에 따라 적 이미지 파일 설정
                when (ED[i]) {
                    //좌
                    1 -> {
                        enemy[i] = BitmapFactory.decodeResource(getResources(), R.drawable.enemy01)
                    }
                    //우
                    2 -> {
                        enemy[i] = BitmapFactory.decodeResource(getResources(), R.drawable.enemy02)
                    }
                    //상
                    3 -> {
                        enemy[i] = BitmapFactory.decodeResource(getResources(), R.drawable.enemy03)
                    }
                    //하
                    4 -> {
                        enemy[i] = BitmapFactory.decodeResource(getResources(), R.drawable.enemy04)
                    }
                }

                //적 이미지 크기 설정
                enemy[i] = Bitmap.createScaledBitmap(enemy[i]!!, scrw / 8, scrh / 4, true)

                //적의 체력이 남아있으면 캔버스에 적 그리기
                if (eLife[i] > 0) {
                    canvas.drawBitmap(enemy[i]!!, scrw / 2 + exd[i], scrh / 2 + eyd[i], null)
                }
                //적의 체력이 없으면 i번째 적 비활성화 후 적 수 -1
                else {
                    EN[i] = 0
                    EC -= 1
                }
            }
        }

        //missile에 10개까지의 비트맵 정보를 저장함
        val missile: Array<Bitmap?> = arrayOfNulls<Bitmap>(10)

        //미사일 수는 0으로 설정
        missileCount = 0

        for (i in 0..9) {
            //i번째 미사일 번호가 0이면(미사일이 비활성화된 상태라면)
            if (missileNum[i] == 0) {
                //미사일 카운트를 1 증가
                missileCount += 1
            }

            //i번째 미사일 번호가 1이면(미사일이 활성화된 상태라면)
            if (missileNum[i] == 1) {
                //좌
                //i번째 md가 1이라면
                if (md[i] == 1) {
                    //i번째 미사일 이미지를 왼쪽 방향으로 나가는 미사일 이미지로 설정
                    missile[i] = BitmapFactory.decodeResource(getResources(), R.drawable.missile01)
                    //i번째 미사일 이미지 크기 설정
                    missile[i] = Bitmap.createScaledBitmap(missile[i]!!, scrw / 16, scrw / 16, true)
                    //캔버스에 i번째 미사일 그리기
                    canvas.drawBitmap(missile[i]!!, mx[i], my[i], null)

                    //i번째 mx값을 scrw/64만큼 감소
                    //이미지를 왼쪽으로 이동시킴으로써 미사일 발사를 구현
                    mx[i] -= scrw / 64f
                }
                //우
                //i번째 md가 2라면
                if (md[i] == 2) {
                    //i번째 미사일 이미지를 오른쪽 방향으로 나가는 미사일 이미지로 설정
                    missile[i] = BitmapFactory.decodeResource(getResources(), R.drawable.missile02)
                    //i번째 미사일 이미지 크기 설정
                    missile[i] = Bitmap.createScaledBitmap(missile[i]!!, scrw / 16, scrw / 16, true)
                    //캔버스에 i번째 미사일 그리기
                    canvas.drawBitmap(missile[i]!!, mx[i], my[i], null)

                    //i번째 mx값을 scrw/64만큼 증가
                    //이미지를 오른쪽으로 이동시킴으로써 미사일 발사를 구현
                    mx[i] += scrw / 64f
                }
                //상
                //i번째 md가 3이라면
                if (md[i] == 3) {
                    //i번째 미사일 이미지를 위쪽 방향으로 나가는 미사일 이미지로 설정
                    missile[i] = BitmapFactory.decodeResource(getResources(), R.drawable.missile03)
                    //i번째 미사일 이미지 크기 설정
                    missile[i] = Bitmap.createScaledBitmap(missile[i]!!, scrw / 16, scrw / 16, true)
                    //캔버스에 i번째 미사일 그리기
                    canvas.drawBitmap(missile[i]!!, mx[i], my[i], null)

                    //i번째 my값을 scrh/32만큼 감소
                    //이미지를 위쪽으로 이동시킴으로써 미사일 발사를 구현
                    my[i] -= scrh / 32f
                }
                //하
                //i번째 md가 4라면
                if (md[i] == 4) {
                    //i번째 미사일 이미지를 아래쪽 방향으로 나가는 미사일 이미지로 설정
                    missile[i] = BitmapFactory.decodeResource(getResources(), R.drawable.missile04)
                    //i번째 미사일 이미지 크기 설정
                    missile[i] = Bitmap.createScaledBitmap(missile[i]!!, scrw / 16, scrw / 16, true)
                    //캔버스에 i번째 미사일 그리기
                    canvas.drawBitmap(missile[i]!!, mx[i], my[i], null)

                    //i번째 my값을 scrh/32만큼 증가
                    //이미지를 아래쪽으로 이동시킴으로써 미사일 발사를 구현
                    my[i] += scrh / 32f
                }

                for (j in 0..4) {
                    //미사일 이미지의 좌표가(mx[i], my[i])
                    // 적 이미지의 좌표 범위 안에 (ⓧ:scrw / 2 + exd[j] ~ scrw / 2 + (scrw - scrw % 64) / 8 + exd[j],
                    // ⓨ:scrh / 2 + eyd[j] ~ scrh / 2 + (scrh - scrh % 32) / 4 + eyd[j]) 들어가면 적이 미사일에 맞은 것으로 간주
                    //-----------------------------------------------------------------------------------------------------
                    //생명력이 존재하는 j번째 적이 i번째 미사일에 맞았다면
                    if (
                        eLife[j] > 0
                        && mx[i] <= scrw / 2 + (scrw - scrw % 64) / 8 + exd[j]
                        && mx[i] >= scrw / 2 + exd[j]
                        && my[i] >= scrh / 2 + eyd[j]
                        && my[i] <= scrh / 2 + (scrh - scrh % 32) / 4 + eyd[j]
                    ) {
                        //j번째 적의 생명력 1감소
                        eLife[j] -= 1

                        //적 데미지 효과음 재생
                        soundPool.play(enemyDamage, 1.0f, 1.0f, 0, 0, 1.0f)

                        //i번째 미사일을 비활성화
                        missileNum[i] = 0
                    }
                }
            }

            //i번째 mx값이 scrw-scrw/16보다 크거나 i번째 mx값이 0보다 작거나 i번째 my값이 scrh-scrw/16보다 크거나 i번째 my값이 0보다 작다면
            //미사일이 스크린 밖을 벗어나면
            if (mx[i] > scrw - scrw / 16 || mx[i] < 0 || my[i] > scrh - scrw / 16 || my[i] < 0) {
                //i번째 미사일을 비활성화 시킨다.
                missileNum[i] = 0
            }
        }

        //방향키 이미지 파일 설정
        moveButton = BitmapFactory.decodeResource(getResources(), R.drawable.move_button)
        //방향키 이미지 크기 설정
        moveButton = Bitmap.createScaledBitmap(moveButton, scrw / 16 * 3, scrh / 8 * 3, true)
        //캔버스에 방향키 그리기
        canvas.drawBitmap(moveButton, 0f, (scrh - moveButton.height).toFloat(), null)

        //공격 버튼 이미지 파일 설정
        attackButton = BitmapFactory.decodeResource(getResources(), R.drawable.attack_button)
        //공격 버튼 이미지 크기 설정
        attackButton = Bitmap.createScaledBitmap(attackButton, scrw / 16 * 3, scrh / 8 * 3, true)
        //캔버스에 공격 버튼 그리기
        canvas.drawBitmap(
            attackButton,
            (scrw - attackButton.width).toFloat(),
            (scrh - attackButton.height).toFloat(),
            null
        )

        val life01: Array<Bitmap?> = arrayOfNulls<Bitmap>(3)    //검정하트 비트맵 배열
        val life02: Array<Bitmap?> = arrayOfNulls<Bitmap>(3)    //빨간하트 비트맵 배열

        for (i in 0..2) {
            //검정하트를 밑에 먼저 그리기
            life01[i] = BitmapFactory.decodeResource(getResources(), R.drawable.life01)
            life01[i] = Bitmap.createScaledBitmap(life01[i]!!, scrw / 32 * 3, scrh / 16 * 3, true)
            canvas.drawBitmap(
                life01[i]!!,
                (scrw + ((i - 3) * life01[i]!!.width)).toFloat(),
                0f,
                null
            )

            //LN[i]가 1이면 라이프 활성화상태
            //검정하트 위에 빨간하트 그리기
            if (LN[i] == 1) {
                life02[i] = BitmapFactory.decodeResource(getResources(), R.drawable.life02)
                life02[i] =
                    Bitmap.createScaledBitmap(life02[i]!!, scrw / 32 * 3, scrh / 16 * 3, true)
                canvas.drawBitmap(
                    life02[i]!!,
                    (scrw + ((i - 3) * life02[i]!!.width)).toFloat(),
                    0f,
                    null
                )
            }
        }
    }

    //터치이벤트 처리
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //만약 화면을 터치했다면
        if (event.getAction() == MotionEvent.ACTION_MOVE
            || event.getAction() == MotionEvent.ACTION_DOWN
            || event.getAction() == MotionEvent.ACTION_POINTER_DOWN
        ) {
            //좌
            //터치한 영역이 왼쪽 방향키의 좌표 범위 안일 때    (x:15~115 / y:715~835)
            if (event.getX().toInt() > 15 && event.getX().toInt() < 115
                && event.getY().toInt() > 715 && event.getY().toInt() < 835
            ) {
                //버튼을 클릭한 상태가 아니고, 플레이어가 이동 중이 아니라면
                if (start == false && count == 0) {
                    //버튼을 클릭했음을 알림
                    start = true
                    //왼쪽 버튼을 클릭했음을 알림
                    DirButton = "Left"
                }
                //왼쪽 버튼을 클릭했음을 알림
                DirButton2 = "Left"
            }
            //우
            //터치한 영역이 오른쪽 방향키의 좌표 범위 안일 때   (x:235~335 / y:715~835)
            else if (event.getX().toInt() > 235 && event.getX().toInt() < 335
                && event.getY().toInt() > 715 && event.getY().toInt() < 835
            ) {
                //버튼을 클릭한 상태가 아니고, 플레이어가 이동 중이 아니라면
                if (start == false && count == 0) {
                    //버튼을 클릭했음을 알림
                    start = true
                    //오른쪽 버튼을 클릭했음을 알림
                    DirButton = "Right"
                }
                //오른쪽 버튼을 클릭했음을 알림
                DirButton2 = "Right"
            }
            //상
            //터치한 영역이 위쪽 방향키의 좌표 범위 안일 때    (x:115~235 / y:615~715)
            else if (event.getX().toInt() > 115 && event.getX().toInt() < 235
                && event.getY().toInt() > 615 && event.getY().toInt() < 715
            ) {
                //버튼을 클릭한 상태가 아니고, 플레이어가 이동 중이 아니라면
                if (start == false && count == 0) {
                    //버튼을 클릭했음을 알림
                    start = true
                    //위쪽 버튼을 클릭했음을 알림
                    DirButton = "Up"
                }
                //위쪽 버튼을 클릭했음을 알림
                DirButton2 = "Up"
            }
            //하
            //터치한 영역이 아래쪽 방향키의 좌표 범위 안일 때    (x:115~235 / y:835~935)
            else if (event.getX().toInt() > 115 && event.getX().toInt() < 235
                && event.getY().toInt() > 835 && event.getY().toInt() < 935
            ) {
                //버튼을 클릭한 상태가 아니고, 플레이어가 이동 중이 아니라면
                if (start == false && count == 0) {
                    //버튼을 클릭했음을 알림
                    start = true
                    //아래 버튼을 클릭했음을 알림
                    DirButton = "Down"
                }
                //아래 버튼을 클릭했음을 알림
                DirButton2 = "Down"
            }
            //공격
            //터치한 영역이 공격버튼의 좌표 범위 안일 때
            else if (event.getX().toInt() > scrw - attackButton.width
                && event.getX().toInt() < scrw
                && event.getY().toInt() > scrh - attackButton.height
                && event.getY().toInt() < scrh
            ) {
                for (i in 0..9) {
                    //i번째 미사일이 비활성화 상태라면
                    if (missileNum[i] == 0) {
                        //i번째 미사일에 x좌표값을 부여한다.
                        mx[i] = scrw / 2 + (scrw / 8 - scrw / 16) / 2 + xd
                        //i번째 미사일에 y좌표값을 부여한다.
                        my[i] = scrh / 2 + (scrh / 4 - scrw / 16) / 2 + yd
                        //i번째 미사일 방향을 저장한다.
                        md[i] = MD
                        //i번째 미사일을 활성화시킨다.
                        missileNum[i] = 1
                        //잔여 미사일 숫자가 0이 아니라면, 미사일 숫자를 1감소시킨다.
                        if (missileCount != 0) missileCount -= 1
                        //함수 종료
                        break
                    }
                }

                //미사일 발사 효과음 재생
                soundPool.play(playerAttack, 1.0f, 1.0f, 0, 0, 1.0f)
            }
            //방향키와 공격 버튼이 아닌 곳을 터치한 경우라면
            else {
                //버튼을 클릭하고 있는 상태가 아니라고 선언함
                start = false
            }
        }

        //화면에서 손을 땠을때
        if (event.getAction() == MotionEvent.ACTION_UP
            || event.getAction() == MotionEvent.ACTION_POINTER_UP
        ) {
            //좌
            //손을 뗀 영역이 왼쪽 방향키의 좌표 범위 안일 때    (x:15~115 / y:715~835)
            if (event.getX().toInt() > 15 && event.getX().toInt() < 115
                && event.getY().toInt() > 715 && event.getY().toInt() < 835
            ) {
                //방향키 버튼을 클릭하고 있는 상태가 아님을  선언
                start = false
            }
            //우
            //손을 뗀 영역이 오른쪽 방향키의 좌표 범위 안일 때    (x:235~335 / y:715~835)
            else if (event.getX().toInt() > 235 && event.getX().toInt() < 335
                && event.getY().toInt() > 715 && event.getY().toInt() < 835
            ) {
                //방향키 버튼을 클릭하고 있는 상태가 아님을  선언
                start = false
            }
            //상
            //손을 뗀 영역이 위쪽 방향키의 좌표 범위 안일 때    (x:115~235 / y:615~715)
            else if (event.getX().toInt() > 115 && event.getX().toInt() < 235
                && event.getY().toInt() > 615 && event.getY().toInt() < 715
            ) {
                //방향키 버튼을 클릭하고 있는 상태가 아님을  선언
                start = false
            }
            //하
            //손을 뗀 영역이 아래쪽 방향키의 좌표 범위 안일 때    (x:115~235 / y:835~935)
            else if (event.getX().toInt() > 115 && event.getX().toInt() < 235
                && event.getY().toInt() > 835 && event.getY().toInt() < 935
            ) {
                //방향키 버튼을 클릭하고 있는 상태가 아님을  선언
                start = false
            }
        }

        return true
    }

    inner class GameThread : Thread() {
        //run은 0 또는 1의 값을 가질 수 있으며, true 값을 넣어줌 (true = 1, false = 0)
        var run = true

        //run 함수를 만들어줌
        override fun run() {
            //run의 값이 1인 동안
            while (run) {
                try {
                    //뷰에서 이미지를 분리시킨다.
                    postInvalidate()
                    //카운트가 8이라면
                    if (count == 8) {
                        //카운트는 0
                        count = 0
                        //방향키 정보를 저장한다.
                        DirButton = DirButton2
                    }

                    //방향키를 클릭한 상태이고, 카운트 수가 0이라면
                    if (start == true && count == 0) {
                        //카운트 수는 1씩 증가한다.
                        count += 1
                        //또한
                    } else {
                        //카운트 수가 0보다 크고, 8보다 작다면 카운트 수는 1씩 증가한다.
                        if (count > 0 && count < 8) count += 1
                    }

                    for (i in 0..4) {
                        //i번째 count2가 8이라면
                        if (count2[i] == 8) {
                            //i번째 count2는 0
                            count2[i] = 0
                        }

                        //i번째 count2가 0이라면
                        if (count2[i] == 0) {
                            //정수형 변수 r은 1,2,3,4 중에 하나의 값을 가짐
                            val r: Int = random.nextInt(4) + 1

                            //좌
                            //r이 1이라면
                            if (r == 1) {
                                //i번째 EDirButton은 왼쪽임을 저장
                                EDirButton[i] = "Left"
                            }
                            //우
                            //r이 2라면
                            if (r == 2) {
                                //i번째 EDirButton은 오른쪽임을 저장
                                EDirButton[i] = "Right"
                            }
                            //상
                            //r이 3이라면
                            if (r == 3) {
                                //i번째 EDirButton은 위쪽임을 저장
                                EDirButton[i] = "Up"
                            }
                            //하
                            //r이 4라면
                            if (r == 4) {
                                //i번째 EDirButton은 아래쪽임을 저장
                                EDirButton[i] = "Down"
                            }
                        }

                        //좌
                        //i번째 적의 생명이 남아있고, 방향이 왼쪽이라면
                        if (eLife[i] > 0 && EDirButton[i] == "Left") {
                            ED[i] = 1
                            //scrw / 2 + i번째 exd 값이 (scrw을 64로 나눈 나머지)/2보다 크다면
                            if (scrw / 2 + exd[i] > scrw % 64 / 2) {
                                //i번째 exd값은 scrh/64만큼 감소
                                //i번째 적의 좌표를 왼쪽으로 옮겨서 적의 이동을 제어
                                exd[i] -= scrw / 64f
                            }
                        }
                        //우
                        //i번째 적의 생명이 남아있고, 방향이 오른쪽이라면
                        if (eLife[i] > 0 && EDirButton[i] == "Right") {
                            ED[i] = 2
                            //scrw / 2 + i번째 exd 값이 scrw - scrw /8 - (scrw을 64로 나눈 나머지)/2보다 작다면
                            if (scrw / 2 + exd[i] < scrw - scrw / 8 - scrw % 64 / 2) {
                                //i번째 exd값은 scrh/64만큼 증가
                                //i번째 적의 좌표를 오른쪽으로 옮겨서 적의 이동을 제어
                                exd[i] += scrw / 64f
                            }
                        }
                        //상
                        //i번째 적의 생명이 남아있고, 방향이 위쪽이라면
                        if (eLife[i] > 0 && EDirButton[i] == "Up") {
                            ED[i] = 3
                            //scrh / 2 + i번째 eyd 값이 (scrh을 32로 나눈 나머지)/2보다 크다면
                            if (scrh / 2 + eyd[i] > scrh % 32 / 2) {
                                //i번째 eyd값은 scrh/32만큼 감소
                                //i번째 적의 좌표를 위쪽으로 옮겨서 적의 이동을 제어
                                eyd[i] -= scrh / 32f
                            }
                        }
                        //하
                        //i번째 적의 생명이 남아있고, 방향이 아래쪽이라면
                        if (eLife[i] > 0 && EDirButton[i] == "Down") {
                            ED[i] = 4
                            //scrh / 2 + i번째 eyd 값이 scrh - scrh /4 - (scrh을 32로 나눈 나머지)/2보다 작다면
                            if (scrh / 2 + eyd[i] < scrh - scrh / 4 - scrh % 32 / 2) {
                                //i번째 eyd값은 scrh/32만큼 증가
                                //i번째 적의 좌표를 아래쪽으로 옮겨서 적의 이동을 제어
                                eyd[i] += scrh / 32f
                            }
                        }
                    }

                    //좌
                    //왼쪽 버튼을 클릭했거나 왼쪽으로 이동중이라면
                    if (start == true && DirButton == "Left" && count < 8
                        || start == false && count > 0 && count < 8 && DirButton == "Left"
                    ) {
                        //scrw / 2 + xd가 (scrw를 64로 나눈 나머지)/2 보다 크다면
                        if (scrw / 2 + xd > (scrw % 64) / 2) {
                            //count를 4로 나눈 나머지가 0이라면
                            if (count % 4 == 0) {
                                //xd값을 scrw/64만큼 감소
                                //플레이어의 좌표를 왼쪽으로 옮겨서 플레이어의 이동을 제어
                                xd -= scrw / 64f
                                n = 1
                                //MD는 1
                                MD = 1
                                //count를 4로 나눈 나머지가 1 또는 3이라면
                            } else if (count % 4 == 1 || count % 4 == 3) {
                                //xd값을 scrw/64만큼 감소
                                //플레이어의 좌표를 왼쪽으로 옮겨서 플레이어의 이동을 제어
                                xd -= scrw / 64f
                                n = 2
                                //count를 4로 나눈 나머지가 2라면
                            } else if (count % 4 == 2) {
                                //xd값을 scrw/64만큼 감소
                                //플레이어의 좌표를 왼쪽으로 옮겨서 플레이어의 이동을 제어
                                xd -= scrw / 64f
                                n = 3
                            }
                        }
                    }

                    //우
                    //오른쪽 버튼을 클릭했거나 오른쪽으로 이동중이라면
                    if (start == true && DirButton == "Right" && count < 8
                        || start == false && count > 0 && count < 8 && DirButton == "Right"
                    ) {
                        //scrw / 2 + xd가 scrw - scrw /8 - (scrw를 64로 나눈 나머지)/2 보다 작다면
                        if (scrw / 2 + xd < scrw - scrw / 8 - (scrw % 64) / 2) {
                            //count를 4로 나눈 나머지가 0이라면
                            if (count % 4 == 0) {
                                //xd값을 scrw/64만큼 증가
                                //플레이어의 좌표를 오른쪽으로 옮겨서 플레이어의 이동을 제어
                                xd += scrw / 64f
                                n = 4
                                //MD는 2
                                MD = 2
                                //count를 4로 나눈 나머지가 1 또는 3이라면
                            } else if (count % 4 == 1 || count % 4 == 3) {
                                //xd값을 scrw/64만큼 증가
                                //플레이어의 좌표를 오른쪽으로 옮겨서 플레이어의 이동을 제어
                                xd += scrw / 64f
                                n = 5
                                //count를 4로 나눈 나머지가 2라면
                            } else if (count % 4 == 2) {
                                //xd값을 scrw/64만큼 증가
                                //플레이어의 좌표를 오른쪽으로 옮겨서 플레이어의 이동을 제어
                                xd += scrw / 64f
                                n = 6
                            }
                        }
                    }

                    //상
                    //위쪽 버튼을 클릭했거나 위쪽으로 이동중이라면
                    if (start == true && DirButton == "Up" && count < 8
                        || start == false && count > 0 && count < 8 && DirButton == "Up"
                    ) {
                        //scrh / 2 + yd가 (scrh를 32로 나눈 나머지)/2 보다 크다면
                        if (scrh / 2 + yd > (scrh % 32) / 2) {
                            //count를 4로 나눈 나머지가 0이라면
                            if (count % 4 == 0) {
                                //yd값을 scrh/32만큼 감소
                                //플레이어의 좌표를 위쪽으로 옮겨서 플레이어의 이동을 제어
                                yd -= scrh / 32f
                                n = 7
                                //MD는 3
                                MD = 3
                                //count를 4로 나눈 나머지가 1 또는 3이라면
                            } else if (count % 4 == 1 || count % 4 == 3) {
                                //yd값을 scrh/32만큼 감소
                                //플레이어의 좌표를 위쪽으로 옮겨서 플레이어의 이동을 제어
                                yd -= scrh / 32f
                                n = 8
                                //count를 4로 나눈 나머지가 2라면
                            } else if (count % 4 == 2) {
                                //yd값을 scrh/32만큼 감소
                                //플레이어의 좌표를 위쪽으로 옮겨서 플레이어의 이동을 제어
                                yd -= scrh / 32f
                                n = 9
                            }
                        }
                    }

                    //하
                    if (start == true && DirButton == "Down" && count < 8
                        || start == false && count > 0 && count < 8 && DirButton == "Down"
                    ) {
                        //scrh / 2 + yd가 scrh - scrh /4 - (scrh를 32로 나눈 나머지)/2 보다 작다면
                        if (scrh / 2 + yd < scrh - scrh / 4 - (scrh % 32) / 2) {
                            //count를 4로 나눈 나머지가 0이라면
                            if (count % 4 == 0) {
                                //yd값을 scrh/32만큼 증가
                                //플레이어의 좌표를 아래쪽으로 옮겨서 플레이어의 이동을 제어
                                yd += scrh / 32f
                                n = 10
                                //MD는 4
                                MD = 4
                                //count를 4로 나눈 나머지가 1 또는 3이라면
                            } else if (count % 4 == 1 || count % 4 == 3) {
                                //yd값을 scrh/32만큼 증가
                                //플레이어의 좌표를 아래쪽으로 옮겨서 플레이어의 이동을 제어
                                yd += scrh / 32f
                                n = 11
                                //count를 4로 나눈 나머지가 2라면
                            } else if (count % 4 == 2) {
                                //yd값을 scrh/32만큼 증가
                                //플레이어의 좌표를 아래쪽으로 옮겨서 플레이어의 이동을 제어
                                yd += scrh / 32f
                                n = 12
                            }
                        }
                    }

                    for (i in 0..4) {
                        //적의 생명이 0보다 크다면
                        if (eLife[i] > 0) {
                            //카운트 수를 1씩 증가시킨다.
                            count2[i] += 1
                        }
                    }

                    //플레이어 이미지의 좌표가 적 이미지의 좌표 범위 안에 들어가면 플레이어와 적이 충돌한 것으로 간주
                    //스크린 상에서 플레이어와 적이 같은 위치지만 좌표값이 다르게 나와서
                    //(적과 플레이어가 모두 좌측 상단에 위치할 때, 적의 좌표값이 (0,0)이면 플레이어는 (-960,-467.5625)로 나왔음)
                    //차이나는 좌표값만큼 직접 플레이어의 좌표값에 더해 비교함
                    //-----------------------------------------------------------------------------------------------------
                    //플레이어의 체력이 남아있고, 활성화상태의 i번째 적과 플레이어가 충돌했다면
                    for (i in 0..4) {
                        if (EN[i] == 1) {
                            if (
                                hp > 0
                                && xd + 960 <= scrw / 2 + (scrw - scrw % 64) / 8 + exd[i]
                                && xd + 960 >= scrw / 2 + exd[i]
                                && yd + 467.5625 >= scrh / 2 + eyd[i]
                                && yd + 467.5625 <= scrh / 2 + (scrh - scrh % 32) / 4 + eyd[i]
                            ) {
                                //i번째 적 비활성화
                                EN[i] = 0
                                //적 수 -1
                                EC--
                                //플레이어 체력 -1
                                hp--
                                //가장 오른쪽의 하트이미지부터 비활성화
                                LN[hp] = 0
                                //플레이어 데미지 효과음 재생
                                soundPool.play(playerDamage, 1.0f, 1.0f, 0, 0, 1.0f)
                            }
                        }
                    }

                    //가장 왼쪽의 하트. 즉, 마지막 하트까지 비활성화된 상태라면 게임오버
                    if (LN[0] == 0) {
                        gameOver()
                    }
                    //하트가 남아있고, 모든 적을 처치했다면 게임 클리어
                    if (LN[0] != 0 && EC == 0) {
                        gameClear()
                    }

                    //0.05초 지연한다.
                    sleep(50)

                } catch (e: Exception) {
                }
            }
        }
    }

    //30초를 센 후, GameActivity 안에서 호출됨
    fun timeOut() {
        //스레드 중지
        thread!!.run = false
        //배경음 반납
        bgBGM.release()
        //여러 효과음 반납
        soundPool.release()

        //0.5초 후 게임오버화면으로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(context, GameoverActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(intent)

            //게임오버 효과음 재생
            gameoverSound.start()

            //액티비티 종료
            (context as? Activity)?.finish()

        }, 500)

        //2초 후 게임오버 효과음 반납
        Handler(Looper.getMainLooper()).postDelayed({
            gameoverSound.release()
        }, 2000)
    }

    fun gameOver() {
        //게임오버
        //가장 왼쪽의 하트. 즉, 마지막 하트까지 비활성화된 상태라면
        if (LN[0] == 0) {
            //스레드 중지
            thread!!.run = false
            //배경음 반납
            bgBGM.release()
            //여러 효과음 반납
            soundPool.release()

            //0.5초 후 게임오버화면으로 전환
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, GameoverActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)

                //게임오버 효과음 재생
                gameoverSound.start()

                //액티비티 종료
                (context as? Activity)?.finish()

            }, 500)

            //2초 후 게임오버 효과음 반납
            Handler(Looper.getMainLooper()).postDelayed({
                gameoverSound.release()
            }, 2000)
        }
    }

    fun gameClear() {
        //모든 적을 처치했다면
        //하트가 하나라도 남아있는 경우라면 게임 클리어
        if (EC == 0) {
            //스레드 중지
            thread!!.run = false
            //배경음 반납
            bgBGM.release()
            //여러 효과음 반납
            soundPool.release()

            //0.5초 후 게임오버화면으로 전환
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, GameoverActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)

                //효과음 소리가 너무 커서 볼륨을 조절함
                gameClearSound.setVolume(0.25f, 0.25f)
                //게임 클리어 효과음 재생
                gameClearSound.start()

                //액티비티 종료
                (context as? Activity)?.finish()

            }, 500)

            //5초 후 게임 클리어 효과음 반납
            Handler(Looper.getMainLooper()).postDelayed({
                gameClearSound.release()
            }, 5000)
        }
    }
}