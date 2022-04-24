package com.example.widget

import com.example.building.Object
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane

class StringWidget(
    private val objectData: Object,
    indicator: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val data: String?,
    private val typeWidget: String
) : AbstractWidget(indicator, layoutX, layoutY, pref, name) {

    private val request = RequestGeneration()
    private var string: Label

    init {
        string = createString()
        updateString()
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(string)
        panel.children.add(setting)
    }

    private fun createString(): Label {
        val stringLabel = Label(data)
        stringLabel.alignment = Pos.CENTER

        AnchorPane.setTopAnchor(stringLabel, 25.0)
        AnchorPane.setBottomAnchor(stringLabel, 30.0)
        AnchorPane.setRightAnchor(stringLabel, 15.0)
        AnchorPane.setLeftAnchor(stringLabel, 15.0)

        return stringLabel
    }

    fun setValue(newValue: String) {
        string.text = newValue
    }

    fun updateString() {
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = request.addressAssemblyGET(address, objectData.getIdModel())
        val model = Gson().fromJson(getData, JsonObject::class.java)
        val border = ProcessingJSON().readBorderBoolean(model, typeWidget)

//        if (border.isNotEmpty()) {
//            val borderColor = ProcessingJSON().readBorderColor(model, typeWidget)
//            if (data != null) {
//                if (borderColor.containsKey(data)) {
//                    string.style =
//                        textStyle(14) + "-fx-background-color: ${borderColor[data]}; -fx-border-color: ${textTheme()}; -fx-border-width: 3;"
//                } else string.style = textStyle(14)
//            }
//        } else string.style = textStyle(14)
    }
}