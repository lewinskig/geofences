package com.lewinskig.geofences.application.tracker

data class TrackerId(val id: String) {
    init {
        require(id.isNotBlank()) { "trackerId must not be blank" }
    }
    override fun toString(): String = id
}