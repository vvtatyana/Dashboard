package com.example.controller

import com.example.building.User
import com.example.util.*
import dropShadow
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
import wayToImage
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
    private var icon: Int = 0

    /**
     * Инициализация окна
     */
    fun initialize() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        headerPane.effect = dropShadow()
        dataPane.style = getAdditionalColor()
        dataPane.effect = dropShadow()
        for(ch in headerPane.children){
            ch.effect = dropShadow()
        }
        for(ch in dataPane.children){
            ch.effect = dropShadow()
        }

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
        accountIcon.image = Image(FileInputStream(wayToImage("animals\\${user.getIcon()}")))
    }

    /**
     * Обработка нажатия кнопки сохранения
     */
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

        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
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

    @FXML
    private fun accountIconClick(){
        if (icon < 25) icon ++
        else icon = 1
        accountIcon.image = Image(FileInputStream(wayToImage("animals\\$icon")))
    }
}
