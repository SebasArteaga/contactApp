package com.example.contactapp

data class Message(
    val id: Int,
    val senderId: Int,
    val recipientId: Int,
    val messageText: String
)
