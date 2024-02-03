package com.dldmswo1209.sql_practice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dldmswo1209.sql_practice.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        // ArrayList 객체를 가져옴
        val users = intent.getSerializableExtra("users") as ArrayList<User>

        val userListAdapter = UserListAdapter()
        userListAdapter.submitList(users)
        binding.recyclerView.adapter = userListAdapter

    }
}