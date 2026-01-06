package com.bangbang93.kdraft.sample

import com.bangbang93.kdraft.Draftable

@Draftable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int
)
