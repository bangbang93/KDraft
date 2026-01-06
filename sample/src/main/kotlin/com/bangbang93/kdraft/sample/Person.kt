package com.bangbang93.kdraft.sample

import com.bangbang93.kdraft.annotations.GenerateBuilder

@GenerateBuilder
data class Person(
    var name: String = "",
    var age: Int = 0,
    var email: String = ""
)
