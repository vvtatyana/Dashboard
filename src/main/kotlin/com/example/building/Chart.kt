package com.example.building

class Chart (private val id: Int?,
             private val idObject: Int,
             private val nameChart: String,
             private val layoutX: Double,
             private val layoutY: Double,
             private val name: String,
             private val unit: String,
             private val type: String) {

    fun getId(): Int = id ?: 0

    fun getIdObject(): Int = idObject

    fun getNameChart(): String = nameChart

    fun getLayoutX(): Double = layoutX

    fun getLayoutY(): Double = layoutY

    fun getName(): String = name

    fun getUnit(): String = unit

    fun getType(): String = type
}
