package com.example.controller

import STYLE_FONT
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.DEFAULT_ADDRESS
import com.example.util.MODELS
import com.google.gson.Gson
import com.google.gson.JsonObject
import getAdditionalColor
import getMainColor
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import javafx.scene.control.Tooltip

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
    private lateinit var unitIndicators: TextField
    @FXML
    private lateinit var nameIndicators: TextField
    @FXML
    private lateinit var addImageView: ImageView
    @FXML
    private lateinit var addIndicators: ListView<String>

    private var name: String = ""
    var returnData = mutableListOf<String>()

    private val request = RequestGeneration()

    /**
     * Инициализация окна
     */
    fun initialize() {
        mainPane.style = getMainColor()
        addIndicators.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
        Tooltip.install(addImageView, Tooltip("Добавить виджет"))
    }

    /**
     * Получение данных из основного окна
     * @obj - данные об объекте
     * @type - тип виджета
     */
    fun load(idModel: String, type: Boolean) {
        if (!type) {
            chartsType.items =
                FXCollections.observableArrayList(mutableListOf("AreaChart", "BarChart", "LineChart", "ScatterChart"))
        } else chartsType.isVisible = false

        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)

        val getData = request.addressAssemblyGET(address, idModel)
        val model = Gson().fromJson(getData, JsonObject::class.java)
        val modelState = ProcessingJSON().readModelState(model)

        addIndicators.items = FXCollections.observableArrayList(modelState)
        addIndicators.style = STYLE_FONT
        addIndicators.onMouseClicked = EventHandler {
            val newValue = addIndicators.selectionModel.selectedItem
            if (modelState.contains(newValue)) {
                name = newValue
            }
        }
    }

    /**
     * Обработка нажатия кнопки добавтить
     */
    @FXML
    private fun addClick() {
        val type = if (chartsType.value == null) ""
        else chartsType.value
        returnData = mutableListOf(name, nameIndicators.text, unitIndicators.text, type)
        val stage: Stage = addImageView.scene.window as Stage
        stage.close()
    }
}