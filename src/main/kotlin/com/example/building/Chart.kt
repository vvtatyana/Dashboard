package com.example.building

/* *
* Класс для графика
*/
class Chart (private val id: Int?,
             private val idObject: String,
             private val nameChart: String,
             private val layoutX: Double,
             private val layoutY: Double,
             private val name: String,
             private val unit: String,
             private val type: String) {

    fun getId(): Int {
        return id!!
    }

    fun getIdObject(): String {
        return idObject
    }

    fun getNameChart(): String {
        return nameChart
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
