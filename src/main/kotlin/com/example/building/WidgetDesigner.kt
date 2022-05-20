package com.example.building

class WidgetDesigner {

    private lateinit var widget: String
    private var name: String
    private var unit: String
    private lateinit var type: String
    private lateinit var date: String
    private lateinit var from: String
    private lateinit var to: String

    constructor(name: String, unit: String, type: String, date: String, from: String, to: String){
        this.name = name
        this.unit = unit
        this.type = type
        this.date = date
        this.from = from
        this.to = to
    }

    constructor(name: String, unit: String){
        this.name = name
        this.unit = unit
    }

    constructor(widget: String, name: String, unit: String, type: String){
        this.widget = widget
        this.name = name
        this.unit = unit
        this.type = type
    }

    fun getWidget(): String = widget

    fun getName(): String = name

    fun getUnit(): String = unit

    fun getType(): String = type

    fun getDate(): String = date

    fun getFrom(): String = from

    fun getTo(): String = to
}

