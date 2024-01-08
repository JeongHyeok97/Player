package com.gi.hybridplayer.model.enums

enum class GroupChannelNumbering(enabled:Boolean) {
    Off(false){
        override fun toString(): String {
            return "Group Channel Numbering: $name"
        }
              },
    On(true){
        override fun toString(): String {
            return "Group Channel Numbering: $name"
        }
    };
}
