package com.project.livechat.ui.screens.home.models

data class ChatCardModel(
    val id: Int,
    val photo: String?,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int?,
    val isOptionsRevealed: Boolean = false
)

val chatCardList = listOf(
    ChatCardModel(
        id = 0,
        photo = "https://www.w3schools.com/howto/img_avatar.png",
        name = "Rajesh",
        lastMessage = "Você bem que poderia passar aqui em casa hoje, o que acha?",
        lastMessageTime = "06:34 AM",
        unreadCount = 0
    ),
    ChatCardModel(
        id = 1,
        photo = "https://www.w3schools.com/howto/img_avatar.png",
        name = "Presidente Kam",
        lastMessage = "Eu já disse que sou bom em muitas coisas",
        lastMessageTime = "09:34 AM",
        unreadCount = 2
    ),
    ChatCardModel(
        id = 2,
        photo = "https://www.w3schools.com/howto/img_avatar.png",
        name = "Walter White",
        lastMessage = "I'm the one who knocks",
        lastMessageTime = "10:21 AM",
        unreadCount = 0
    ),
    ChatCardModel(
        id = 3,
        photo = "https://www.w3schools.com/howto/img_avatar.png",
        name = "Skyler",
        lastMessage = "But Walt, you can't just sbaklaslabslabsalbaskbjbajkbjkasbkbfaskbebasbfbasfbeasefaskefbkasbefkbaskfbeasbefbaskefbaskefjaskefbjkasbefjkbasjkefbjkasefjkbaskjbef",
        lastMessageTime = "04:12 AM",
        unreadCount = 0
    ),
)