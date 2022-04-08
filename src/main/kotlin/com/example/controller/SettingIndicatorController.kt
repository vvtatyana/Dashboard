package com.example.controller

import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
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
import javafx.scene.control.Tooltip

/**
* Класс создает окно для настройки индикатора
*/
class SettingIndicatorController {

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var headerPane: AnchorPane

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var minTextField: TextField

    @FXML
    private lateinit var midTextField: TextField

    @FXML
    private lateinit var maxTextField: TextField

    @FXML
    private lateinit var saveImageView: ImageView

    @FXML
    private lateinit var deleteImageView: ImageView

    @FXML
    private lateinit var unitIndicators: TextField

    @FXML
    private lateinit var nameIndicators: TextField

    private lateinit var border: Map<String, String>
    private var idModel = ""
    private var name = ""

    var list = mutableListOf<String>()
    var delete = false

    /**
    * Получение данных из основного окна
    * @idModel - id модели
    */
    fun load(idModel: String, name: String) {
        this.name = name
        this.idModel = idModel

        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))

        val address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = RequestGeneration().addressAssemblyGET(address, idModel)
        val model = Gson().fromJson(getData, JsonObject::class.java)

        border = ProcessingJSON().readBorder(model, name)
        if (border[MIN] != null)
            minTextField.text = border[MIN].toString()
        if (border[MID] != null)
            midTextField.text = border[MID].toString()
        if (border[MAX] != null)
            maxTextField.text = border[MAX].toString()
    }

    /**
     * Обработка нажатия кнопки сохранения
     */
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

    /**
     * Изменяет данные о границах модели
     * @field - поле
     * @value - новое значение
     */
    private fun updateBorder(field: String, value: String) {
        var address = RequestGeneration().addressGeneration(DEFAULT_ADDRESS, MODELS)
        address = RequestGeneration().addressGeneration(address, idModel)
        val dataGet = RequestGeneration().getRequest(address).toString()
        val data = ProcessingJSON().updateModel(dataGet, name, field, value)
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
}