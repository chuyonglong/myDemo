package com.example.glidedemo.extensions

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)


