package com.example.controller

import com.example.util.THEME
import com.example.building.User
import com.example.util.*
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import com.example.util.themePane
import com.example.util.wayToImage
import java.io.FileInputStream

class AccountController {

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var accountIcon: ImageView

    @FXML
    private lateinit var exitImageView: ImageView

    @FXML
    private lateinit var login: Label

    @FXML
    private lateinit var username: Label

    @FXML
    private lateinit var tokenText: TextField

    @FXML
    private lateinit var hostText: TextField

    @FXML
    private lateinit var saveImageView: ImageView

    @FXML
    private lateinit var dayNight: ImageView

    private var icon: Int = 0

    lateinit var user: User

    var exit: Boolean = false
    var save: Boolean = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        themePane(dataPane)

        dayNight.image = Image(FileInputStream(wayToImage("other/$THEME")))

        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
        Tooltip.install(exitImageView, Tooltip("Выйти из аккаунта"))
        Tooltip.install(dayNight, Tooltip("Тема приложения"))
    }

    fun load(user: User) {
        this.user = user
        username.text = user.getUsername()
        login.text = user.getLogin()
        accountIcon.image = Image(FileInputStream(wayToImage("animals\\${user.getIcon()}")))
    }

    @FXML
    private fun saveClick() {
        save = true
        val token = tokenText.text
        var host = hostText.text

        if (token != "")
            HEADERS_AUTH = "Bearer $token"

        if (host != "") {
            DEFAULT_ADDRESS = "https://${host}/api/v1"
            host = DEFAULT_ADDRESS
        }

        if (token != "" || host != "" || icon != user.getIcon()) {
            user = User(
                user.getId(),
                user.getIdUser(),
                user.getUsername(),
                user.getLogin(),
                host,
                token,
                user.getCastle(),
                icon,
                user.getTheme()
            )
        }
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun exitClick() {
        exit = true
        val stage: Stage = exitImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun accountIconClick() {
        if (icon < 30) icon++
        else icon = 1
        accountIcon.image = Image(FileInputStream(wayToImage("animals\\$icon")))
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
