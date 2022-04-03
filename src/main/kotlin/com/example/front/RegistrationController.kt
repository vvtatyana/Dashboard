package com.example.front

import THEME
import com.example.back.*
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

class RegistrationController {

    @FXML
    private lateinit var dataPane: AnchorPane
    @FXML
    private lateinit var mainPane: AnchorPane
    @FXML
    private lateinit var addressText: TextField
    @FXML
    private lateinit var addressLabel: Label
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

    fun initialize() {
        mainPane.style = getMainColor()
        dataPane.style = getAdditionalColor()
    }

    @FXML
    private fun onRegistrationButtonClick() {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        val username: String = nameText.text
        val login: String = loginText.text
        val token: String = tokenText.text
        if(addressText.text != null) {
            val defAddress = "https://${addressText.text}/api/v1"
            DEFAULT_ADDRESS = defAddress
        }
        val request = RequestGeneration()
        HEADERS_AUTH += token
        val address = request.addressGeneration(DEFAULT_ADDRESS, USERS, requestCharacters.SLESH.code)
        val str = request.GETRequest(address)
        val json: JsonArray = Gson().fromJson(str, JsonArray::class.java)
        val listUsers = Parsing().readAllUsers(json)

        for (user in listUsers)
            if (user.getUsername() == username && user.getLogin() == login) {

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
                            THEME
                        )
                    )

                database.closeBD()
                showWindow("window.fxml", "Window")
            }
        errorLabel.text = "Неверные данные о пользователе"
    }

    @FXML
    private fun onLoginButtonClick() {
        showWindow("loginWindow.fxml", "Login")
    }

    private fun showWindow(nameFile: String, title: String){
        var stage: Stage = loginButton.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource(nameFile))
        stage = Stage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = title
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }

    @FXML
    private fun nameInfoClick() {
        showAlert(
            "Имя пользователя",
            "Имя пользователя — это уникальный идентификатор пользователя в системе RIC. Получить можно, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
        )
    }

    @FXML
    private fun loginInfoClick() {
        showAlert(
            "Логин",
            "Логин — идентификатор пользователя (учётной записи). Логин совпадает с email пользователя. Получить можно, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
        )
    }

    @FXML
    private fun tokenInfoClick() {
        showAlert(
            "Токен",
            "Токен — это средство авторизации для каждого запроса от клиента к серверу. Токен выдается только один раз при регистрации клиента в системе RIC. Его необходимо сохранить для дальнейшего использования."
        )
    }

    @FXML
    private fun addressInfoClick() {
        showAlert(
            "Хост",
            "URL-адрес, на который будет отправлен запрос. Является полным адресом ресурса. По умолчанию dev.rightech.io, поэтому это поле не обязательно для заполнения. "
        )
    }

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