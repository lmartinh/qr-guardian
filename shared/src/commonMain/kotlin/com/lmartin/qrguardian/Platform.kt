package com.lmartin.qrguardian

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform