package com.gi.hybridplayer.model

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class TimeUtils {
    companion object{

        fun calculateTimeDifferenceInMillis(zoneId1: String): Long {
            val zoneId1: ZoneId = ZoneId.of(zoneId1) // 예: "Asia/Seoul"
            val zoneId2: ZoneId = ZoneId.systemDefault() // 예: "America/New_York"
            val now1: ZonedDateTime = ZonedDateTime.now(zoneId1)
            val now2: ZonedDateTime = ZonedDateTime.now(zoneId2)
            val offset = (now1.offset.totalSeconds - now2.offset.totalSeconds)*1000L
            return offset
        }


        fun currentTimeMillis(zoneId: String): Long {
            val offset = calculateTimeDifferenceInMillis(zoneId1 = zoneId)

            val currentTime = System.currentTimeMillis() + offset
            return currentTime
        }


        fun getZonedTime(zoneId: String, millis:Long): ZonedDateTime {
//            val offset = calculateTimeDifferenceInMillis(zoneId1 = zoneId, zoneId2 = ZoneId.systemDefault().id)
            val instant = Instant.ofEpochMilli(millis)
            val zone: ZoneId = ZoneId.of(zoneId)
            val zonedDateTime = ZonedDateTime.ofInstant(instant, zone)
            return zonedDateTime
        }
    }
}
