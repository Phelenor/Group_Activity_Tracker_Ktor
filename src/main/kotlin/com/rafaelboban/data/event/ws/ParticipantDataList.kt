package com.rafaelboban.data.event.ws

import com.rafaelboban.data.event.ParticipantData
import com.rafaelboban.utils.Constants.TYPE_USER_STATUS

data class ParticipantDataList(
    val list: List<ParticipantData>
) : BaseModel(TYPE_USER_STATUS)
