package com.atuma.appruvechallenge.network.model

import java.io.File

data class Request(
    val document: File,
    val user_id: String
)