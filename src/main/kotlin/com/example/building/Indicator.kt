package com.example.building

class Indicator(private val id: Int?,
                private val idObject: Int,
                private val nameIndicator: String,
                private val layoutX: Double,
                private val layoutY: Double,
                private val name: String,
                private val unit: String,
                private val type: String) {

    fun getId(): Int = id ?: 0

    fun getIdObject(): Int = idObject

    fun getNameIndicator(): String = nameIndicator

    fun getLayoutX(): Double = layoutX

    fun getLayoutY(): Double = layoutY

    fun getName(): String = name

    fun getUnit(): String = unit

    fun getType(): String = type
}

