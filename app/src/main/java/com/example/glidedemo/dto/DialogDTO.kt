package com.example.glidedemo.dto


data class DialogDTO(
    val title: Int = -1,
    val content: String = "",
    val isShowClose: Boolean = false,
    val cancelText: Int = -1,
    val okText: Int = -1,
    val checkBoxText: Int = -1,

)