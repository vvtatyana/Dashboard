package com.example.widget

import com.example.restAPI.ProcessingJSON
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane

class StringWidget(
    id: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private var data: String?,
    private val typeWidget: String,
    private var strModel: String
) : AbstractWidget(id, layoutX, layoutY, pref, name) {

    private var string: Label

    init {
        string = createString()
        setColor("")
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(string)
        panel.children.add(setting)
    }

    private fun createString(): Label {
        val stringLabel = Label(data)
        stringLabel.id = "stringLabel"
        stringLabel.alignment = Pos.CENTER
        AnchorPane.setTopAnchor(stringLabel, 25.0)
        AnchorPane.setBottomAnchor(stringLabel, 30.0)
        AnchorPane.setRightAnchor(stringLabel, 15.0)
        AnchorPane.setLeftAnchor(stringLabel, 15.0)
        return stringLabel
    }

    override fun setColor(strModel: String) {
        if (strModel.isNotEmpty())
            this.strModel = strModel
        val model = Gson().fromJson(this.strModel, JsonObject::class.java)
        val border = ProcessingJSON().readBorderBooleanOrString(model, typeWidget)
        if (border.isNotEmpty() && border.containsValue(data)) {
            var name = ""
            border.forEach{
                if (it.value == data)
                    name = it.key
            }
            val borderColor = ProcessingJSON().readBorderColor(model, typeWidget)
            string.style = "-fx-background-color: ${borderColor[name]};"
        }
        else {
            string.style = "-fx-background-color: transparent;"
        }
    }

    override fun setValue(newValue: String) {
        string.text = newValue
        data = newValue
        setColor(strModel)
    }
}