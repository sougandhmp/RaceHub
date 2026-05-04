package org.gce.racehub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform