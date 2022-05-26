package com.rafaelboban.data.event.ws

import com.rafaelboban.data.event.EventController
import com.rafaelboban.utils.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
    var phase: EventController.Phase,
    val eventId: String
) : BaseModel(TYPE_PHASE_CHANGE)