package com.example.building

/**
* Класс для индикатора
*/
class Indicator(private val id: Int?,
                private val idObject: String,
                private val nameIndicator: String,
                private val layoutX: Double,
                private val layoutY: Double,
                private val name: String,
                private val unit: String) {

    fun getId(): Int {
        return id!!
    }

    fun getIdObject(): String {
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
}

