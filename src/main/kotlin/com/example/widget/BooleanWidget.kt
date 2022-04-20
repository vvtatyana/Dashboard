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
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle

class BooleanWidget(
    objectData: Object,
    indicator: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val data: String?,
    private val typeWidget: String
) : AbstractWidget(indicator, layoutX, layoutY, pref, name) {

    private val request = RequestGeneration()

    private var circle: Circle
    private var text: Label

    private val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
    private val getData = request.addressAssemblyGET(address, objectData.getIdModel())
    private val model = Gson().fromJson(getData, JsonObject::class.java)

    init {
        circle = createCircle(74.0, 24.0, 19.0, 23.0, 23.0, true)
        text = createCircleText()
        panel.children.add(title)
        panel.children.add(circle)
        panel.children.add(createCircle(34.0, 65.0, 62.0, 64.0, 64.0, false))
        panel.children.add(text)
        panel.children.add(setting)
    }

    private fun createCircle(
        radius: Double,
        top: Double,
        bottom: Double,
        right: Double,
        left: Double,
        flag: Boolean
    ): Circle {
        val circle = Circle()

        val border = ProcessingJSON().readBorderBoolean(model, typeWidget)

        if (flag && data != null) {
            val borderColor = if (border.isNotEmpty()) {
                ProcessingJSON().readBorderColor(model, typeWidget)
            } else mutableMapOf("True" to "#6bdb6b", "False" to "#ff4e33")
            if (data.toBoolean()) {
                circle.fill = Paint.valueOf(borderColor["True"])
            } else {
                circle.fill = Paint.valueOf(borderColor["False"])
            }
        } else if (!flag) {
            circle.fill = Paint.valueOf("#636161")
        }
        circle.radius = radius
        circle.stroke = Paint.valueOf("white")
        circle.strokeWidth = 3.0
        AnchorPane.setTopAnchor(circle, top)
        AnchorPane.setBottomAnchor(circle, bottom)
        AnchorPane.setRightAnchor(circle, right)
        AnchorPane.setLeftAnchor(circle, left)
        return circle
    }

    private fun createCircleText(): Label {
        val circleLabel = Label()
        if (data.toBoolean()) {
            circleLabel.text = "Да"
        } else {
            circleLabel.text = "Нет"
        }
        circleLabel.alignment = Pos.CENTER
        circleLabel.style = textStyle(20, "white")

        AnchorPane.setTopAnchor(circleLabel, 70.0)
        AnchorPane.setBottomAnchor(circleLabel, 70.0)
        AnchorPane.setRightAnchor(circleLabel, 70.0)
        AnchorPane.setLeftAnchor(circleLabel, 70.0)
        return circleLabel
    }

    fun setValue(newValue: Boolean) {
        val border = ProcessingJSON().readBorderBoolean(model, typeWidget)
        val borderColor = if (border.isNotEmpty()) ProcessingJSON().readBorderColor(model, typeWidget)
        else mutableMapOf("True" to "#6bdb6b", "False" to "#ff4e33")
        if (newValue) {
            text.text = "Да"
            circle.fill = Paint.valueOf(borderColor["True"])
        } else {
            text.text = "Нет"
            circle.fill = Paint.valueOf(borderColor["False"])
        }
    }
}