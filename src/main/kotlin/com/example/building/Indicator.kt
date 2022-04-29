package com.example.building

class Indicator(private val id: Int?,
                private val idObject: Int,
                private val nameIndicator: String,
                private val layoutX: Double,
                private val layoutY: Double,
                private val name: String,
                private val unit: String,
                private val type: String) {

    fun getId(): Int {
        return id ?: 0
    }

    fun getIdObject(): Int {
        return idObject
    }

    fun getNameIndicator(): String {
        return nameIndicator
    }

    fun getLayoutX(): Double {
        return layoutX
    }

    fun getLayoutY(): Double {
        return layoutY
    }

    fun getName(): String {
        return name
    }

    fun getUnit(): String {
        return unit
    }

    fun getType(): String {
        return type
    }
}

