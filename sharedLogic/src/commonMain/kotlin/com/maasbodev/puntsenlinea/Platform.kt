package com.maasbodev.puntsenlinea

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform