package com.gi.database.model.enums

import androidx.annotation.NonNull

enum class EPGMode(){
    Normal{
        override fun toString(): String {
            return name
        }
    },
    UTC{
        override fun toString(): String {
            return name
        }
    };
}