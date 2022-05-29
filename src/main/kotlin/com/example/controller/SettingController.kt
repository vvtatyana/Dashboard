package com.example.controller

import com.example.building.User
import com.example.util.THEME
import com.example.util.createImageView
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Spinner
import javafx.scene.control.Tooltip
import javafx.stage.Stage

class SettingController {
    @FXML
    private lateinit var time: Spinner<Int>

    @FXML
    private lateinit var dayNight: Button

    @FXML
    private lateinit var saveButton: Button

    var save: Boolean = false
    lateinit var user: User

    fun initialize() {
        dayNight.graphic = createImageView(THEME, 30.0)
        saveButton.tooltip = Tooltip("Сохранить изменения")
        dayNight.tooltip = Tooltip("Тема приложения")
    }

    fun loader(user: User){
        this.user = user
        time.valueFactory.value = user.getTimer()
    }

    @FXML
    private fun saveClick() {
        save = true
        user.setTimer(time.value)
        val stage: Stage = saveButton.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun dayNightClick() {
        THEME = when (THEME) {
            "light" ->  "dark"
            "dark" -> "light"
            else -> "light"
        }
        dayNight.graphic = createImageView(THEME, 30.0)
    }
}