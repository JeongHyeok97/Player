package com.gi.hybridplayer.model.enums

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