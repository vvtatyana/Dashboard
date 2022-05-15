package com.example.building

class Widget(private val id: Int?,
             private val idObject: Int,
             private val nameWidget: String,
             private val layoutX: Double,
             private val layoutY: Double,
             private val name: String,
             private val unit: String,
             private val type: String) {

    fun getId(): Int = id ?: 0

    fun getIdObject(): Int = idObject

    fun getNameWidget(): String = nameWidget

    fun getLayoutX(): Double = layoutX

    fun getLayoutY(): Double = layoutY

    fun getName(): String = name

    fun getUnit(): String = unit

    fun getType(): String = type
}

