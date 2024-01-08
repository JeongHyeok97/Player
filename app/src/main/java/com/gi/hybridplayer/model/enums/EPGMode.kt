package com.gi.hybridplayer.model.enums

enum class EPGMode(){
    Normal{
        override fun toString(): String {
            return "EPG Mode: $name"
        }
    },
    UTC{
        override fun toString(): String {
            return "EPG Mode: $name"
        }
    };
}