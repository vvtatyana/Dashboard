package com.example.controller

import com.example.building.Widget
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage

class AddWidgetController {

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
    private lateinit var addButton: Button

    @FXML
    private lateinit var addIndicators: ComboBox<String>

    lateinit var widget: Widget

    private var typeWidget: Boolean = false

    private val request = RequestGeneration()
    private var dataType: String = ""
    var add = false
    var message: String = ""

    fun initialize() {
        addButton.tooltip = Tooltip("Добавить виджет")
        unitLabel.isVisible = false
        unitTextField.isVisible = false
    }

    fun load(idModel: String, typeWidget: Boolean): Boolean {
        this.typeWidget = typeWidget

        val address = request.addressGeneration(ADDRESS, MODELS)

        val getData = request.getRequest(request.addressGeneration(address, idModel))
        return if (getData == "403 Forbidden" || getData == "404 Not Found" || getData == "600 No connection") {
            message = getData
            true
        } else {
            val model = Gson().fromJson(getData, JsonObject::class.java)
            val modelState = ProcessingJSON().readModelState(model)
            val stateType = ProcessingJSON().readModelParams(model)

            var itemsIndicator = mutableListOf<String>()
            if (!typeWidget) {
                val items = mutableListOf<String>()
                ChartType.values().forEach { items.add(it.type) }
                chartsType.items =
                    FXCollections.observableArrayList(items)
                chartsType.value = ChartType.values()[0].type

                modelState.forEach {
                    if (stateType[it] != TypeIndicator.STRING.type) {
                        itemsIndicator.add(it)
                    }
                }
            } else {
                itemsIndicator = modelState as MutableList<String>
            }

            addIndicators.items = FXCollections.observableArrayList(itemsIndicator)
            addIndicators.value = modelState[0]
            dataType = stateType[modelState[0]].toString()
            visibleField(dataType)

            addIndicators.setOnAction {
                if (modelState.contains(addIndicators.value)) {
                    nameTextField.promptText = addIndicators.value
                    dataType = stateType[addIndicators.value].toString()
                    visibleField(dataType)
                }
            }
            false
        }
    }

    private fun visibleField(dataType: String) {
        if (dataType == TypeIndicator.NUMBER.type) {
            nameLabel.isVisible = true
            nameTextField.isVisible = true
            unitLabel.isVisible = true
            unitTextField.isVisible = true
        } else if (dataType == TypeIndicator.STRING.type || dataType == TypeIndicator.BOOLEAN.type) {
            nameLabel.isVisible = true
            nameTextField.isVisible = true
            unitLabel.isVisible = false
            unitTextField.isVisible = false
        }
    }

    @FXML
    private fun addClick() {
        add = true
        val type = if (!typeWidget && chartsType.value != null) chartsType.value
        else dataType
        widget = Widget(addIndicators.value, nameTextField.text, unitTextField.text, type)
        val stage: Stage = addButton.scene.window as Stage
        stage.close()
    }
}