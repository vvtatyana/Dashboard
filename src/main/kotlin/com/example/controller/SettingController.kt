package com.example.controller

import com.example.building.User
import com.example.util.THEME
import com.example.util.wayToImage
import javafx.fxml.FXML
import javafx.scene.control.Spinner
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import java.io.FileInputStream

class SettingController {
    @FXML
    private lateinit var time: Spinner<Int>

    @FXML
    private lateinit var dayNight: ImageView

    @FXML
    private lateinit var saveImageView: ImageView

    var save: Boolean = false
    lateinit var user: User

    fun initialize() {
        dayNight.image = Image(FileInputStream(wayToImage("other/$THEME")))

        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
        Tooltip.install(dayNight, Tooltip("Тема приложения"))
    }

    fun loader(user: User){
        this.user = user
        time.valueFactory.value = user.getTimer()
    }

    @FXML
    private fun saveClick() {
        save = true
        user.setTimer(time.value)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun dayNightClick() {
        THEME = when (THEME) {
            "light" -> {
                "dark"
            }
            "dark" -> {
                "light"
            }
            else -> {
                "light"
            }
        }
        dayNight.image = Image(FileInputStream(wayToImage("other/$THEME")))
    }
}