package com.example.myapplication

import java.io.Serializable

class User(
    var id: String,
    var pw: String,
    var name: String
): Serializable {
    constructor(): this("","","")

}