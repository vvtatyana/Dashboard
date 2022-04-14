package com.example.controller

import com.example.building.Widget
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
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

/**
 * Класс создает окно для настройки индикатора
 */
class SettingIndicatorController {

    lateinit var toLabel: Label
    lateinit var fromLabel: Label
    lateinit var valueIntervalTo: TextField

    @FXML
    private lateinit var borderPane: AnchorPane

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var headerPane: AnchorPane

    @FXML
    private lateinit var dataPane: AnchorPane

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

    private var oldValueFrom = ""
    private var oldValueTo = ""
    private var oldColor = ""

    private var idModel = ""
    private var name = ""

    private lateinit var type: String

    var dataWidget: Widget? = null
    var delete = false
    var save = false

    fun initialise() {
        themePane(mainPane, dataPane, headerPane)
        shadowPane(dataPane, headerPane)
    }

    /**
     * Получение данных из основного окна
     * @idModel - id модели
     */
    fun load(idModel: String, name: String, type: String) {
        this.name = name
        this.idModel = idModel
        this.type = type

        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))

        val address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = RequestGeneration().addressAssemblyGET(address, idModel)
        val model = Gson().fromJson(getData, JsonObject::class.java)

        when (type) {
            "number" -> {
                settingData(model)
            }
            "boolean", "string" -> {
                settingData(model)
            }
            else -> borderPane.isVisible = false
        }
    }

    private fun settingData(model: JsonObject){
        borderFrom = if (type == "number") {
            borderTo = ProcessingJSON().readBorderTo(model, name)
            ProcessingJSON().readBorderFrom(model, name)
        }
        else{
            ProcessingJSON().readBorderBoolean(model, name)
        }
        if (borderFrom.isNotEmpty()) {
            borderColor = ProcessingJSON().readBorderColor(model, name)

            val nameInterval = FXCollections.observableArrayList(borderFrom.keys.toList())
            nameIntervalComboBox.items = nameInterval
            nameIntervalComboBox.value = borderFrom.keys.toList()[0]

            valueIntervalFrom.text = borderFrom[nameIntervalComboBox.value]
            oldValueFrom = valueIntervalFrom.text

            when (type) {
                "number" -> {
                    valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                    oldValueTo = valueIntervalTo.text
                }
                "boolean" -> {
                    fromLabel.isVisible = false
                    valueIntervalFrom.isVisible = false
                    toLabel.isVisible = false
                    valueIntervalTo.isVisible = false
                }
                else -> {
                    toLabel.isVisible = false
                    valueIntervalTo.isVisible = false
                }
            }

            colorInterval.text = borderColor[nameIntervalComboBox.value]
            oldColor = colorInterval.text

            colorPane.style = "-fx-background-color:${oldColor}"
            nameIntervalComboBox.setOnAction {
                valueIntervalFrom.text = borderFrom[nameIntervalComboBox.value]
                oldValueFrom = valueIntervalFrom.text

                if (type == "number") {
                    valueIntervalTo.text = borderTo[nameIntervalComboBox.value]
                    oldValueTo = valueIntervalTo.text
                }

                colorInterval.text = borderColor[nameIntervalComboBox.value]
                oldColor = colorInterval.text
                colorPane.style = "-fx-background-color:${oldColor}"
            }
        }
        else  borderPane.isVisible = false
    }


    /**
     * Обработка нажатия кнопки сохранения
     */
    @FXML
    private fun saveClick() {
        save = true
        dataWidget = Widget(nameIndicators.text, unitIndicators.text)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    /**
     * Изменяет данные о границах модели
     * @field - поле
     * @value - новое значение
     */

    private fun updateBorder(field: String, value: String, level: String) {
        var address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        address = RequestGeneration().addressGeneration(address, idModel)
        val dataGet = RequestGeneration().getRequest(address).toString()
        val data = ProcessingJSON().updateModel(dataGet, name, field, value, level)
        if (data != "")
            RequestGeneration().patchRequest(address, data)
    }

    private fun updateBorderColor(field: String, value: String) {
        var address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        address = RequestGeneration().addressGeneration(address, idModel)
        val dataGet = RequestGeneration().getRequest(address).toString()
        val data = ProcessingJSON().updateModelColor(dataGet, name, field, value)
        if (data != "")
            RequestGeneration().patchRequest(address, data)
    }

    /**
     * Обработка нажатия кнопки удаления
     */
    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun updateLevelClick() {
        val newColor = colorInterval.text
        val newValueFrom = valueIntervalFrom.text
        if (type == "number") {
            val newValueTo = valueIntervalTo.text
            if (newValueFrom != "" && oldValueFrom != newValueFrom) {
                updateBorder(nameIntervalComboBox.value, newValueFrom, "a")
                oldValueFrom = newValueFrom
            }
            if (newValueTo != "" && oldValueTo != newValueTo) {
                updateBorder(nameIntervalComboBox.value, newValueTo, "b")
                oldValueTo = newValueTo
            }

            if (newValueFrom == "" && newColor == "" && newValueTo == "") {
                errorLabel.text = "Заполните поля значение или цвет"
            }

            if (newColor != "" && oldColor != newColor) {
                if (newColor.length != 7 || newColor[0] != '#') {
                    errorLabel.text = "Введена не верная кодировка цвета"
                } else {
                    updateBorderColor(nameIntervalComboBox.value, newColor)
                    colorPane.style = "-fx-background-color:${newColor}"
                    oldColor = newColor
                }
            }
        } else if (type == "boolean" || type == "string") {
            if (newColor != "" && oldColor != newColor) {
                if (newColor.length != 7 || newColor[0] != '#') {
                    errorLabel.text = "Введена не верная кодировка цвета"
                } else {
                    updateBorderColor(nameIntervalComboBox.value, newColor)
                    colorPane.style = "-fx-background-color:${newColor}"
                    oldColor = newColor
                }
            }
        }
    }
}
