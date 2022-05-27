package com.rafaelboban.data.event

import com.rafaelboban.data.location.LocationPoint

data class EventData(
    val id: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val distance: Double,
    val startTimestampParent: Long,
    val endTimestampParent: Long,
    val distanceParent: Double,
    val participants: List<String>,
    val points: List<LocationPoint>
)