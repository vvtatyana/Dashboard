package com.example.controller

import com.example.building.User
import com.example.util.*
import getAdditionalColor
import getMainColor
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.io.FileInputStream

/**
* Класс создает окно аккаунта пользователя
*/
class AccountController {

    @FXML
    private lateinit var mainPane: AnchorPane
    @FXML
    private lateinit var headerPane: AnchorPane
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

    lateinit var user: User

    var exit: Boolean = false
    var save: Boolean = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
        val animal = (1..25).random()
        accountIcon.image = Image(FileInputStream("./src/main/resources/com/example/controller/front/animals/$animal.png"))

        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
        Tooltip.install(exitImageView, Tooltip("Выйти из аккаунта"))
    }

    /**
     * Получение данных о пользователе из основного окна
     * @user - данные о пользователе
     */
    fun load(user: User) {
        this.user = user
        username.text = user.getUsername()
        login.text = user.getLogin()
    }

    /**
     * Обработка нажатия кнопки сохранения
     */
    @FXML
    private fun saveClick() {
        save = true
        if (tokenText.text != null)
            HEADERS_AUTH = "Bearer ${tokenText.text}"
        user = User(
            user.getId(),
            user.getIdUser(),
            user.getUsername(),
            user.getLogin(),
            user.getAddress(),
            tokenText.text,
            user.getCastle(),
            user.getTheme()
        )
        if (hostText.text != null) {
            DEFAULT_ADDRESS = "https://${hostText.text}/api/v1"
            user = User(
                user.getId(),
                user.getIdUser(),
                user.getUsername(),
                user.getLogin(),
                DEFAULT_ADDRESS,
                user.getToken(),
                user.getCastle(),
                user.getTheme()
            )
        }
    }

    /**
     * Обработка нажатия кнопки выхода из аккаунта
     */
    @FXML
    private fun exitClick() {
        exit = true
        val stage: Stage = exitImageView.scene.window as Stage
        stage.close()
    }
}
