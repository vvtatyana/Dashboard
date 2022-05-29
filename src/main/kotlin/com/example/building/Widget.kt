package com.example.building

class Widget(private val id: Int?,
             private val idUser: Int,
             private val idObject: String,
             private val typeWidgets: String,
             private val identifier: String,
             private val layoutX: Double,
             private val layoutY: Double,
             private var name: String,
             private var unit: String,
             private var type: String) {

    fun getId(): Int = id ?: 0
    fun getIdUser(): Int = idUser
    fun getIdObject(): String = idObject
    fun getTypeWidgets(): String = typeWidgets
    fun getIdentifier(): String = identifier
    fun getLayoutX(): Double = layoutX
    fun getLayoutY(): Double = layoutY
    fun getName(): String = name
    fun setName(newValue: String) {
        name = newValue
    }
    fun getUnit(): String = unit
    fun setUnit(newValue: String) {
        unit = newValue
    }
    fun getType(): String = type
    fun setType(newValue: String) {
        type = newValue
    }
}

