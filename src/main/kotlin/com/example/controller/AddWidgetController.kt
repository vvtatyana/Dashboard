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

class AddWidgetController {

    @FXML
    private lateinit var dataPane: AnchorPane
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

    private var typeWidget: Boolean = false

    private val request = RequestGeneration()
    private var dataType: String = ""
    var add = false

    fun initialize() {
        themePane(dataPane)

        Tooltip.install(addImageView, Tooltip("Добавить виджет"))
        unitLabel.isVisible = false
        unitTextField.isVisible = false
    }

    fun load(idModel: String, typeWidget: Boolean) {
        this.typeWidget = typeWidget
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

        addIndicators.setOnAction {
            name = addIndicators.value
            println(name)
            if (modelState.contains(name)) {
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

    @FXML
    private fun addClick() {
        add = true
        val type = if (!typeWidget && chartsType.value != null) chartsType.value
        else dataType
        println(name)
        returnData = Widget(name, nameTextField.text, unitTextField.text, type)
        val stage: Stage = addImageView.scene.window as Stage
        stage.close()
    }
}