package com.example.controller

import THEME
import com.example.restAPI.*
import com.example.building.User
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import getAdditionalColor
import getMainColor
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.stage.Modality
import javafx.stage.Stage

/**
* Класс создает окно для регистрации
*/
class RegistrationController {

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var hostText: TextField

    @FXML
    private lateinit var nameText: TextField

    @FXML
    private lateinit var loginText: TextField

    @FXML
    private lateinit var tokenText: TextField

    @FXML
    private lateinit var memoryCheck: CheckBox

    @FXML
    private lateinit var loginButton: Button

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var nameInfoOne: ImageView

    @FXML
    private lateinit var nameInfoTwo: ImageView

    @FXML
    private lateinit var nameInfoThree: ImageView

    @FXML
    private lateinit var nameInfoFour: ImageView

    /**
     * Инициализация окна
     */
    fun initialize() {
        mainPane.style = getMainColor()
        dataPane.style = getAdditionalColor()
        Tooltip.install(nameInfoOne, Tooltip("Справка"))
        Tooltip.install(nameInfoTwo, Tooltip("Справка"))
        Tooltip.install(nameInfoThree, Tooltip("Справка"))
        Tooltip.install(nameInfoFour, Tooltip("Справка"))
    }

    /**
     * Обработка нажатия кнопки зарегестрироваться
     */
    @FXML
    private fun onRegistrationButtonClick() {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        val username: String = nameText.text
        val login: String = loginText.text
        val token: String = tokenText.text
        if (hostText.text != "") {
            val defAddress = "https://${hostText.text}/api/v1"
            DEFAULT_ADDRESS = defAddress
        }
        val request = RequestGeneration()
        HEADERS_AUTH += token
        val address = request.addressGeneration(DEFAULT_ADDRESS, USERS)
        val getData = request.getRequest(address)
        if (getData != null) {
            val json: JsonArray = Gson().fromJson(getData, JsonArray::class.java)
            val users = ProcessingJSON().readAllUsers(json)

            for (user in users)
                if (user.getUsername() == username && user.getLogin() == login) {

                    val animal = (1..25).random()
                    if (memoryCheck.isSelected)
                        queriesDB.insertIntoUser(
                            User(
                                null,
                                user.getIdUser(),
                                username,
                                login,
                                DEFAULT_ADDRESS,
                                token,
                                true,
                                animal,
                                THEME
                            )
                        )
                    else
                        queriesDB.insertIntoUser(
                            User(
                                null,
                                user.getIdUser(),
                                username,
                                login,
                                DEFAULT_ADDRESS,
                                token,
                                false,
                                animal,
                                THEME
                            )
                        )

                    database.closeBD()
                    showWindow("window.fxml", "Window")
                }
            errorLabel.text = "Не верные имя или логин."
        }
        errorLabel.text = "Не верные токен или хост."
        nameText.text = ""
        loginText.text = ""
        tokenText.text = ""
        hostText.text = ""
    }

    /**
     * Обработка нажатия кнопки войти
     */
    @FXML
    private fun onLoginButtonClick() {
        showWindow("loginWindow.fxml", "Login")
    }

    /**
     * Совершает переход в другие окна
     */
    private fun showWindow(nameFile: String, title: String) {
        var stage: Stage = loginButton.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource(nameFile))
        stage = Stage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = title
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }

    /**
     * Обработка нажатия кнопки справки
     */
    @FXML
    private fun nameInfoClick() {
        showAlert(
            "Имя пользователя",
            "Имя пользователя — это уникальный идентификатор пользователя в системе RIC. Узнать его можно на платформе RIC, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
        )
    }

    /**
     * Обработка нажатия кнопки справки
     */
    @FXML
    private fun loginInfoClick() {
        showAlert(
            "Логин",
            "Логин — идентификатор пользователя. Логин совпадает с email пользователя платформы RIC. Узнать его можно на платформе RIC, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
        )
    }

    /**
     * Обработка нажатия кнопки справки
     */
    @FXML
    private fun tokenInfoClick() {
        showAlert(
            "Токен",
            "Токен — это средство авторизации для каждого запроса от клиента к серверу. Токен выдается только один раз при регистрации клиента в системе RIC. Его необходимо сохранить для дальнейшего использования."
        )
    }

    /**
     * Обработка нажатия кнопки справки
     */
    @FXML
    private fun addressInfoClick() {
        showAlert(
            "Хост",
            "Хост, на который будет отправлен запрос. Является полным адресом ресурса. По умолчанию dev.rightech.io."
        )
    }

    /**
     * Показ справки
     */
    private fun showAlert(name: String, text: String) {
        val alert = Alert(AlertType.INFORMATION)
        alert.dialogPane.style = getAdditionalColor()
        alert.dialogPane.minWidth = Region.USE_PREF_SIZE
        alert.dialogPane.minHeight = Region.USE_PREF_SIZE
        alert.title = "Справка"
        alert.headerText = name
        alert.contentText = text
        alert.showAndWait()
    }
}