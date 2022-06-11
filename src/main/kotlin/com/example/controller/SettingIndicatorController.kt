package com.example.controller

import com.example.building.Widget
import com.example.building.WidgetDesigner
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.roundToInt

class SettingIndicatorController {

    @FXML
    private lateinit var unitLabel: Label

    @FXML
    private lateinit var nameLevel: TextField

    @FXML
    private lateinit var toLabel: Label

    @FXML
    private lateinit var fromLabel: Label

    @FXML
    private lateinit var valueIntervalTo: TextField

    @FXML
    private lateinit var borderPane: AnchorPane

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var deleteButton: Button

    @FXML
    private lateinit var unitIndicators: TextField

    @FXML
    private lateinit var nameIndicators: TextField

    @FXML
    private lateinit var valueIntervalFrom: TextField

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var nameIntervalComboBox: ComboBox<String>

    @FXML
    private lateinit var colorPicker: ColorPicker

    private lateinit var borderFrom: Map<String, String>
    private lateinit var borderTo: Map<String, String>
    private lateinit var borderColor: Map<String, String>

    private var oldNameLevel = ""
    private var oldValueFrom = ""
    private var oldValueTo = ""
    private var oldColor = ""

    private var idModel = ""
    private var name = ""

    private lateinit var type: String

    var dataWidget: WidgetDesigner? = null
    var delete = false
    var save = false
    var message: String = ""
    var typeNumber: Boolean = true

    fun load(idModel: String, indicator: Widget): Boolean {
        this.idModel = idModel
        this.name = indicator.getIdentifier()
        this.type = indicator.getType()

        nameIndicators.text = indicator.getName()
        if (indicator.getUnit().isNotEmpty())
            unitIndicators.text = indicator.getUnit()

        saveButton.tooltip = Tooltip("Сохранить изменения")

        val address = RequestGeneration().addressGeneration(ADDRESS, MODELS)
        val getData = RequestGeneration().getRequest(RequestGeneration().addressGeneration(address, idModel))
        return if (!checkRequest(message)) false
        else {
            val model = Gson().fromJson(getData, JsonObject::class.java)
            typeNumber = ProcessingJSON().readTypeNumeric(model, name)
            when (type) {
                TypeIndicator.NUMBER.type, TypeIndicator.BOOLEAN.type, TypeIndicator.STRING.type ->
                    settingData(model)
                else -> borderPane.isVisible = false
            }
            true
        }
    }

    private fun settingData(model: JsonObject) {
        borderFrom = if (type == TypeIndicator.NUMBER.type) {
            borderTo = ProcessingJSON().readBorderTo(model, name)
            ProcessingJSON().readBorderFrom(model, name)
        } else {
            unitIndicators.isVisible = false
            unitLabel.isVisible = false
            ProcessingJSON().readBorderBooleanOrString(model, name)
        }

        if (borderFrom.isNotEmpty()) {
            borderColor = ProcessingJSON().readBorderColor(model, name)

            val nameInterval = FXCollections.observableArrayList(borderFrom.keys.toList())
            nameIntervalComboBox.items = nameInterval
            nameIntervalComboBox.value = borderFrom.keys.toList()[0]

            nameLevel.text = nameIntervalComboBox.value
            oldNameLevel = nameLevel.text
            valueIntervalFrom.text = borderFrom[nameIntervalComboBox.value]
            oldValueFrom = valueIntervalFrom.text

            when (type) {
                TypeIndicator.NUMBER.type -> {
                    if (typeNumber) {
                        valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                        oldValueTo = valueIntervalTo.text
                    } else {
                        fromLabel.text = "Значение"
                        valueIntervalTo.isVisible = false
                        toLabel.isVisible = false
                    }
                }
                TypeIndicator.BOOLEAN.type -> {
                    fromLabel.isVisible = false
                    valueIntervalFrom.isVisible = false
                    toLabel.isVisible = false
                    valueIntervalTo.isVisible = false
                }
                else -> {
                    fromLabel.text = "Значение"
                    toLabel.isVisible = false
                    valueIntervalTo.isVisible = false
                }
            }

            oldColor = borderColor[nameIntervalComboBox.value].toString()
            colorPicker.value = Color.web(oldColor)

            nameIntervalComboBox.setOnAction {

                valueIntervalFrom.text = borderFrom[nameIntervalComboBox.value]
                oldValueFrom = valueIntervalFrom.text
                nameLevel.text = nameIntervalComboBox.value
                oldNameLevel = nameLevel.text
                if (type == "number" && typeNumber) {
                    valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                    oldValueTo = valueIntervalTo.text
                }
                oldColor = borderColor[nameIntervalComboBox.value].toString()
                colorPicker.value = Color.web(oldColor)
            }
        } else borderPane.isVisible = false
    }

    @FXML
    private fun saveClick() {
        save = true
        dataWidget = WidgetDesigner(nameIndicators.text, unitIndicators.text)
        val stage: Stage = saveButton.scene.window as Stage
        stage.close()
    }

    private fun update(field: String, value: String, property: String, border: Boolean = false) {
        var address = RequestGeneration().addressGeneration(ADDRESS, MODELS)
        address = RequestGeneration().addressGeneration(address, idModel)
        val getData = RequestGeneration().getRequest(address)
        if (checkRequest(getData)) {
            val jsonModel: JsonObject = Gson().fromJson(getData, JsonObject::class.java)
            var data = ""
            ProcessingJSON().readBorder(jsonModel, name)?.forEach {
                if (border && typeNumber) {
                    if (it.asJsonObject.get(NAME).asString == field) {
                        it.asJsonObject.get(VALUE).asJsonObject.addProperty(property, value)
                        data = jsonModel.toString()
                    }
                } else {
                    if (it.asJsonObject.get(NAME).asString == field) {
                        it.asJsonObject.addProperty(property, value)
                        data = jsonModel.toString()
                    }
                }
            }
            if (data.isNotEmpty()) {
                val path = RequestGeneration().patchRequest(address, data)
                checkRequest(path)
            }
        }
    }

    private fun checkRequest(message: String): Boolean =
        if (message == "403 Forbidden" || message == "404 Not Found" || message == "No connection") {
            this.message = message
            val stage: Stage = saveButton.scene.window as Stage
            stage.close()
            false
        } else true


    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteButton.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun updateLevelClick() {
        val newNameLevel = nameLevel.text
        val newColor = toHexString(colorPicker.value)
        val newValueFrom = valueIntervalFrom.text

        if (newNameLevel != "" && newNameLevel != oldNameLevel)
            update(nameIntervalComboBox.value, newNameLevel, NAME)

        when (type) {
            TypeIndicator.NUMBER.type -> {
                val newValueTo = valueIntervalTo.text
                if (typeNumber && newValueFrom != "" && oldValueFrom != newValueFrom) {
                    update(nameIntervalComboBox.value, newValueFrom, "a", true)
                    oldValueFrom = newValueFrom
                } else if (newValueFrom != "" && oldValueFrom != newValueFrom) {
                    update(nameIntervalComboBox.value, newValueFrom, "value", true)
                    oldValueFrom = newValueFrom
                }
                if (newValueTo != "" && oldValueTo != newValueTo) {
                    update(nameIntervalComboBox.value, newValueTo, "b", true)
                    oldValueTo = newValueTo
                }

                if (newValueFrom == "" && newColor == "" && newValueTo == "" && newNameLevel == "")
                    errorLabel.text = "Заполните поля значение"
                updateColor(newColor)
            }
            TypeIndicator.BOOLEAN.type -> {
                updateColor(newColor)
            }
            TypeIndicator.STRING.type -> {
                if (newValueFrom != "" && oldValueFrom != newValueFrom) {
                    update(nameIntervalComboBox.value, newValueFrom, "value")
                    oldValueFrom = newValueFrom
                }

                if (newValueFrom == "" && newColor == "" && newNameLevel == "")
                    errorLabel.text = "Заполните поля значение"

                updateColor(newColor)
            }
        }
    }

    private fun updateColor(newColor: String){
        if (newColor != "" && oldColor != newColor) {
            update(nameIntervalComboBox.value, newColor, "color")
            oldColor = newColor
            colorPicker.value = Color.web(oldColor)
        }
    }

    private fun toHexString(color: Color): String {
        val r = (color.red * 255).roundToInt() shl 24
        val g = (color.green * 255).roundToInt() shl 16
        val b = (color.blue * 255).roundToInt() shl 8
        val a = (color.opacity * 255).roundToInt()
        return String.format("#%08X", r + g + b + a)
    }
}
