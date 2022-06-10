package com.example.widget

import com.example.restAPI.ProcessingJSON
import com.example.util.dropShadow
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle

class BooleanWidget(
    id: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private var data: String?,
    private val typeWidget: String,
    private var strModel: String
) : AbstractWidget(id, layoutX, layoutY, pref, name) {

    private var circle: Circle
    private var text: Label
    private val defaultColor = mutableMapOf("True" to "#6bdb6b", "False" to "#ff4e33")
    private val model: () -> JsonObject = {
        Gson().fromJson(strModel, JsonObject::class.java)
    }

    init {
        circle = createCircle(74.0, 24.0, 19.0, 23.0, 23.0, true)
        circle.style = dropShadow()
        text = createCircleText()
        panel.children.add(title)
        panel.children.add(circle)
        panel.children.add(createCircle(34.0, 65.0, 62.0, 64.0, 64.0, false))
        panel.children.add(text)
        panel.children.add(setting)
    }

    private fun createCircle(radius: Double, top: Double, bottom: Double,
                             right: Double, left: Double, flag: Boolean ): Circle {
        val model = model()
        val circle = Circle()
        val border = ProcessingJSON().readBorderBooleanOrString(model, typeWidget)
        if (flag && data != null) {
            circle.fill = borderColor(border, model)
        } else if (!flag) circle.fill = Paint.valueOf("#636161")
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
        circleLabel.id = "circleLabel"
        circleLabel.text = text(data.toBoolean())
        circleLabel.alignment = Pos.CENTER
        AnchorPane.setTopAnchor(circleLabel, 70.0)
        AnchorPane.setBottomAnchor(circleLabel, 70.0)
        AnchorPane.setRightAnchor(circleLabel, 69.0)
        AnchorPane.setLeftAnchor(circleLabel, 69.0)
        return circleLabel
    }

    override fun setValue(newValue: String) {
        data = newValue
        val model = model()
        val border = ProcessingJSON().readBorderBooleanOrString(model, typeWidget)
        circle.fill = borderColor(border, model)
        text.text = text(newValue.toBoolean())
    }

    override fun setColor(strModel: String){
        this.strModel = strModel
        val model = model()
        val border = ProcessingJSON().readBorderBooleanOrString(model, typeWidget)
        if (data != null) {
            circle.fill = borderColor(border, model)
        }
    }

    private fun borderColor(border: Map<String, String>, model: JsonObject): Paint {
        val color = if (border.isNotEmpty()) ProcessingJSON().readBorderColor(model, typeWidget)
        else defaultColor

        var name = ""
        border.forEach{ if(it.value == data) name = it.key }
        return Paint.valueOf(color[name])
    }

    private fun text(value: Boolean): String =
        if (value) "Да"
        else "Нет"
}