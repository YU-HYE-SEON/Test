package com.dldmswo1209.sql_practice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dldmswo1209.sql_practice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var db: DBHelper
    var users = ArrayList<User>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        binding.registerButton.setOnClickListener {
            createUser().let {
                if (it != null) {
                    db.addUser(it)
                }
            }
        }
        binding.deleteButton.setOnClickListener {
            createUser().let {
                if (it != null) {
                    db.deleteUser(it)
                }
            }
        }
        binding.selectButton.setOnClickListener {
            users.clear() // 리스트 초기화 -> 초기화를 하지 않으면 데이터가 중복되서 쌓이게 된다.
            db.allUsers.forEach { // db에 저장되어 있는 유저 정보들을 가져와서 리스트에 저장
                users.add(it)
            }
            val intent = Intent(this, SelectActivity::class.java) // 인텐트 객체 생성
            // ArrayList 객체를 인텐트로 전달하려면 ArrayList 에 담기는 데이터 클래스가 Serializable(직렬화) 이 되어 있어야 함.
            intent.putExtra("users", users) // 인텐트에 데이터 전달
            startActivity(intent)

        }
        binding.loginButton.setOnClickListener {
            createUser().let {
                if (it != null) {
                    if (db.login(it)) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginSuccessActivity::class.java)
                        intent.putExtra("name", binding.nameEditText.text.toString())
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "로그인 실패!", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
    private fun createUser(): User?{
        val id = binding.idEditText.text.toString()
        val pw = binding.pwEditText.text.toString()
        val name = binding.nameEditText.text.toString()
        if(id == "" || pw == "" || name =="") // 입력 정보가 하나라도 비어있으면
            return null // Null 반환

        return User(id,pw,name)
    }


}