package com.example.controller

import com.example.building.Widget
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

/**
* Класс создает окно для добавления нового виджета
*/
class AddWidgetController {

    @FXML
    private lateinit var dataPane: AnchorPane
    @FXML
    private lateinit var mainPane: AnchorPane
    @FXML
    private lateinit var chartsType: ComboBox<String>
    @FXML
    private lateinit var unitTextField: TextField
    @FXML
    private lateinit var nameTextField: TextField
    @FXML
    private lateinit var unitLabel: Label
    @FXML
    private lateinit var nameLabel: Label
    @FXML
    private lateinit var addImageView: ImageView
    @FXML
    private lateinit var addIndicators: ComboBox<String>

    private var name: String = ""
    lateinit var returnData: Widget

    private val request = RequestGeneration()
    private var dataType: String = ""
    var add = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        themePane(mainPane, dataPane)
        shadowPane(dataPane)

        Tooltip.install(addImageView, Tooltip("Добавить виджет"))
        unitLabel.isVisible = false
        unitTextField.isVisible = false
    }

    /**
     * Получение данных из основного окна
     * @obj - данные об объекте
     * @typeWidget - тип виджета
     */
    fun load(idModel: String, typeWidget: Boolean) {
        if (!typeWidget) {
            chartsType.items =
                FXCollections.observableArrayList(mutableListOf("AreaChart", "BarChart", "LineChart", "ScatterChart"))
            chartsType.value = "AreaChart"
        }

        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)

        val getData = request.addressAssemblyGET(address, idModel)
        val model = Gson().fromJson(getData, JsonObject::class.java)
        val modelState = ProcessingJSON().readModelState(model)
        val stateType = ProcessingJSON().readModelParams(model)

        addIndicators.items = FXCollections.observableArrayList(modelState)
        addIndicators.value = modelState[0]

        addIndicators.onMouseClicked = EventHandler {
            val newValue = addIndicators.selectionModel.selectedItem
            if (modelState.contains(newValue)) {
                name = newValue
                nameTextField.promptText = name
                dataType = stateType[name].toString()
                if (dataType == "number"){
                    nameLabel.isVisible = true
                    nameTextField.isVisible = true
                    unitLabel.isVisible = true
                    unitTextField.isVisible = true
                }
                else if (dataType == "string" || dataType == "boolean"){
                    nameLabel.isVisible = true
                    nameTextField.isVisible = true
                    unitLabel.isVisible = false
                    unitTextField.isVisible = false
                }
            }
        }
    }

    /**
     * Обработка нажатия кнопки добавтить
     */
    @FXML
    private fun addClick() {
        add = true
        val type = if (chartsType.value != null) chartsType.value
        else dataType
        returnData = Widget(name, nameTextField.text, unitTextField.text, type)
        val stage: Stage = addImageView.scene.window as Stage
        stage.close()
    }
}