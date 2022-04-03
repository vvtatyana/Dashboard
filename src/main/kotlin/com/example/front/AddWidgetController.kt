package com.example.front

import STYLE_FONT
import com.example.back.Parsing
import com.example.back.RequestGeneration
import com.example.util.DEFAULT_ADDRESS
import com.example.util.MODELS
import com.example.util.requestCharacters
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
import com.example.building.Object

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
    var list = mutableListOf<String>()

    private val request = RequestGeneration()

    fun initialize() {
        mainPane.style = getMainColor()
        addIndicators.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
    }

    fun load(objectData: Object, flag: Boolean) {
        if (!flag){
            chartsType.items = FXCollections.observableArrayList(mutableListOf("AreaChart", "BarChart", "LineChart", "ScatterChart"))
        }
        else chartsType.isVisible = false

        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)

        val strM = request.addressAssemblyGET(address, objectData.getIdModel())
        val jsonM = Gson().fromJson(strM, JsonObject::class.java)
        val modelState = Parsing().readModelParams(jsonM)

        val modelST = mutableListOf<String>()
        for (state in modelState)
            modelST.add(state.key)

        addIndicators.items = FXCollections.observableArrayList(modelST)
        addIndicators.style = STYLE_FONT
        addIndicators.onMouseClicked = EventHandler {
            val newValue = addIndicators.selectionModel.selectedItem
            if (modelST.contains(newValue)) {
                name = newValue
            }
        }
    }

    @FXML
    private fun addClick() {
        val type = if(chartsType.value==null) ""
        else chartsType.value
        list = mutableListOf(name, nameIndicators.text, unitIndicators.text, type)
        val stage: Stage = addImageView.scene.window as Stage
        stage.close()
    }
}