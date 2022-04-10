package com.example.controller

import THEME
import dropShadow
import getAdditionalColor
import getMainColor
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

/**
* Класс создает окно для настройки приложения
*/
class SettingController {

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var headerPane: AnchorPane

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var themeBox: ComboBox<String>

    @FXML
    private lateinit var saveImageView: ImageView

    /**
    * Инициализация окна
    */
    fun initialize() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        headerPane.effect = dropShadow()
        for(ch in headerPane.children){
            ch.effect = dropShadow()
        }
        dataPane.style = getAdditionalColor()
        dataPane.effect = dropShadow()
        for(ch in dataPane.children){
            ch.effect = dropShadow()
        }
        themeBox.items = FXCollections.observableArrayList(mutableListOf("light", "dark"))
        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
    }

    /**
     * Обработка нажатия кнопки сохранения
     */
    @FXML
    private fun saveClick() {
        if (themeBox.value != null) {
            THEME = themeBox.value
        }
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

}