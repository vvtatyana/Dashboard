package com.example.front

import com.example.back.Parsing
import com.example.back.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import getAdditionalColor
import getMainColor
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import com.example.building.Object

class SettingIndicatorController {

    @FXML
    lateinit var mainPane: AnchorPane

    @FXML
    lateinit var headerPane: AnchorPane

    @FXML
    lateinit var dataPane: AnchorPane

    @FXML
    lateinit var minTextField: TextField

    @FXML
    lateinit var midTextField: TextField

    @FXML
    lateinit var maxTextField: TextField

    @FXML
    lateinit var saveImageView: ImageView

    @FXML
    lateinit var deleteImageView: ImageView

    @FXML
    lateinit var unitIndicators: TextField

    @FXML
    lateinit var nameIndicators: TextField

    private lateinit var border: Map<String, String>
    private var idModel = ""

    var list = mutableListOf<String>()
    var delete = false

    fun load(objectDate: Object) {
        idModel = objectDate.getIdModel()
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()

        val address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)
        val strM = RequestGeneration().addressAssemblyGET(address, idModel)
        val jsonM = Gson().fromJson(strM, JsonObject::class.java)

        border = Parsing().readBorder(jsonM)
        if (border[MIN] != null)
            minTextField.text = border[MIN].toString()
        if (border[MID] != null)
            midTextField.text = border[MID].toString()
        if (border[MAX] != null)
            maxTextField.text = border[MAX].toString()
    }

    @FXML
    private fun saveClick() {
        if (minTextField.text != border[MIN]!!.toString()) {
            updateBorder(MIN, minTextField.text)
        }
        if (midTextField.text != border[MID]!!.toString()) {
            updateBorder(MID, midTextField.text)
        }
        if (maxTextField.text != border[MAX]!!.toString()) {
            updateBorder(MAX, maxTextField.text)
        }
        list = mutableListOf(nameIndicators.text, unitIndicators.text)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    private fun updateBorder(field: String, value: String) {
        var address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)
        address = RequestGeneration().addressGeneration(address, idModel, requestCharacters.SLESH.code)
        val dataGet = RequestGeneration().GETRequest(address).toString()
        val data = Parsing().updateModel(dataGet, field, value)
        if (data != "")
            RequestGeneration().PATCHRequest(address, data)
    }

    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }
}