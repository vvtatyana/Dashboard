package com.example.controller

import com.example.building.Widget
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.DEFAULT_ADDRESS
import com.example.util.MODELS
import com.example.util.NAME
import com.example.util.TypeIndicator
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

class SettingIndicatorController {

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
    private lateinit var saveImageView: ImageView

    @FXML
    private lateinit var deleteImageView: ImageView

    @FXML
    private lateinit var unitIndicators: TextField

    @FXML
    private lateinit var nameIndicators: TextField

    @FXML
    private lateinit var valueIntervalFrom: TextField

    @FXML
    private lateinit var colorInterval: TextField

    @FXML
    private lateinit var colorPane: Label

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var nameIntervalComboBox: ComboBox<String>

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

    var dataWidget: Widget? = null
    var delete = false
    var save = false

    fun load(idModel: String, name: String, type: String) {
        this.idModel = idModel
        this.name = name
        this.type = type

        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))

        val address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = RequestGeneration().addressAssemblyGET(address, idModel)
        val model = Gson().fromJson(getData, JsonObject::class.java)

        when (type) {
            TypeIndicator.NUMBER.type, TypeIndicator.BOOLEAN.type, TypeIndicator.STRING.type -> {
                settingData(model)
            }
            else -> borderPane.isVisible = false
        }
    }

    private fun settingData(model: JsonObject) {
        borderFrom = if (type == TypeIndicator.BOOLEAN.type) {
            borderTo = ProcessingJSON().readBorderTo(model, name)
            ProcessingJSON().readBorderFrom(model, name)
        } else {
            ProcessingJSON().readBorderBoolean(model, name)
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
                    valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                    oldValueTo = valueIntervalTo.text
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

            colorInterval.text = borderColor[nameIntervalComboBox.value]
            oldColor = colorInterval.text

            colorPane.style = "-fx-background-color:${oldColor}"
            nameIntervalComboBox.setOnAction {
                if (nameIntervalComboBox.value == "Добавить уровень") {
                    nameLevel.text = ""
                    colorInterval.text = ""
                    colorPane.style = "-fx-background-color: #ffffff"
                    valueIntervalFrom.text = ""
                    valueIntervalTo.text = ""
                } else {
                    valueIntervalFrom.text = borderFrom[nameIntervalComboBox.value]
                    oldValueFrom = valueIntervalFrom.text
                    nameLevel.text = nameIntervalComboBox.value
                    oldNameLevel = nameLevel.text
                    if (type == "number") {
                        valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                        oldValueTo = valueIntervalTo.text
                    }

                    colorInterval.text = borderColor[nameIntervalComboBox.value]
                    oldColor = colorInterval.text
                    colorPane.style = "-fx-background-color:${oldColor}"
                }
            }
        } else borderPane.isVisible = false
    }

    @FXML
    private fun saveClick() {
        save = true
        dataWidget = Widget(nameIndicators.text, unitIndicators.text)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    private fun update(field: String, value: String, property: String, border: Boolean = false) {
        var address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        address = RequestGeneration().addressGeneration(address, idModel)
        val dataGet = RequestGeneration().getRequest(address).toString()
        val data = if (border) ProcessingJSON().updateBorder(dataGet, name, field, value, property)
        else ProcessingJSON().updateModel(dataGet, name, field, value, property)
        if (data != "")
            RequestGeneration().patchRequest(address, data)
    }

    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun updateLevelClick() {
        val newNameLevel = nameLevel.text
        val newColor = colorInterval.text
        val newValueFrom = valueIntervalFrom.text

        if (newNameLevel != "" && newNameLevel != oldNameLevel) {
            update(nameIntervalComboBox.value, newNameLevel, NAME)
        }

        if (type == TypeIndicator.NUMBER.type) {
            val newValueTo = valueIntervalTo.text
            if (newValueFrom != "" && oldValueFrom != newValueFrom) {
                update(nameIntervalComboBox.value, newValueFrom, "a", true)
                oldValueFrom = newValueFrom
            }
            if (newValueTo != "" && oldValueTo != newValueTo) {
                update(nameIntervalComboBox.value, newValueTo, "b", true)
                oldValueTo = newValueTo
            }

            if (newValueFrom == "" && newColor == "" && newValueTo == "" && newNameLevel == "") {
                errorLabel.text = "Заполните поля значение или цвет"
            }

            if (newColor != "" && oldColor != newColor) {
                if (newColor.length != 7 || newColor[0] != '#') {
                    errorLabel.text = "Введена не верная кодировка цвета"
                } else {
                    update(nameIntervalComboBox.value, newColor, "color")
                    colorPane.style = "-fx-background-color:${newColor}"
                    oldColor = newColor
                }
            }
        } else if (type == TypeIndicator.BOOLEAN.type) {
            if (newColor != "" && oldColor != newColor) {
                if (newColor.length != 7 || newColor[0] != '#') {
                    errorLabel.text = "Введена не верная кодировка цвета"
                } else {
                    update(nameIntervalComboBox.value, newColor, "color")
                    colorPane.style = "-fx-background-color:${newColor}"
                    oldColor = newColor
                }
            }
        } else if (type == TypeIndicator.STRING.type) {
            if (newValueFrom != "" && oldValueFrom != newValueFrom) {
                update(nameIntervalComboBox.value, newValueFrom, "color")
                oldValueFrom = newValueFrom
            }

            if (newValueFrom == "" && newColor == "" && newNameLevel == "") {
                errorLabel.text = "Заполните поля значение или цвет"
            }

            if (newColor != "" && oldColor != newColor) {
                if (newColor.length != 7 || newColor[0] != '#') {
                    errorLabel.text = "Введена не верная кодировка цвета"
                } else {
                    update(nameIntervalComboBox.value, newColor, "color")
                    colorPane.style = "-fx-background-color:${newColor}"
                    oldColor = newColor
                }
            }
        }
    }
}
