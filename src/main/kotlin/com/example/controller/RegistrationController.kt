package com.example.controller

import com.example.util.THEME
import com.example.restAPI.*
import com.example.building.User
import com.example.database.QueriesDB
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.stage.Modality
import javafx.stage.Stage
import org.springframework.security.crypto.factory.PasswordEncoderFactories

class RegistrationController {

    @FXML
    private lateinit var passwordTextTwo: TextField

    @FXML
    private lateinit var passwordTextOne: TextField

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

    private lateinit var queriesDB: QueriesDB
    fun initialize() {
        Tooltip.install(nameInfoOne, Tooltip("Справка"))
        Tooltip.install(nameInfoTwo, Tooltip("Справка"))
        Tooltip.install(nameInfoThree, Tooltip("Справка"))
        Tooltip.install(nameInfoFour, Tooltip("Справка"))

        queriesDB = QueriesDB()
    }

    @FXML
    private fun onRegistrationButtonClick() {
        val username: String = nameText.text
        val login: String = loginText.text
        val token: String = tokenText.text

        if (username.isEmpty()) errorLabel.text = "Введите имя"
        else if (login.isEmpty()) errorLabel.text = "Введите логин"
        else if (token.isEmpty()) errorLabel.text = "Введите токен"
        else if (passwordTextOne.text.isEmpty()) errorLabel.text = "Введите пароль"
        else if (passwordTextTwo.text.isEmpty()) errorLabel.text = "Введите второй пароль"
        else if (username.isNotEmpty() && login.isNotEmpty() && token.isNotEmpty() && passwordTextOne.text.isNotEmpty() && passwordTextTwo.text.isNotEmpty()) {
            if (hostText.text.isNotEmpty()) {
                val defAddress = "https://${hostText.text}/api/v1"
                ADDRESS = defAddress
            }

            val request = RequestGeneration()
            HEADERS_AUTH = "Bearer $token"
            val address = request.addressGeneration(ADDRESS, USERS)
            val getData = request.getRequest(address)
            val userBD = QueriesDB().selectUser(UsersTable.LOGIN.name, loginText.text)
            if (userBD != null) {
                errorLabel.text = "Пользователь с таким логином уже существует."
            } else {
                if (!errorMessage(getData)) {
                    val json: JsonArray = Gson().fromJson(getData, JsonArray::class.java)
                    val users = ProcessingJSON().readAllUsers(json)
                    for (user in users) {
                        if (user.getUsername() == username && user.getLogin() == login) {
                            val icon = (1..20).random()
                            if (passwordTextOne.text == passwordTextTwo.text) {
                                if (memoryCheck.isSelected)
                                    queriesDB.insertIntoUser(
                                        User(
                                            user.getIdUser(),
                                            username,
                                            login,
                                            PasswordEncoderFactories.createDelegatingPasswordEncoder()
                                                .encode(passwordTextOne.text),
                                            ADDRESS,
                                            token,
                                            castle = true,
                                            icon = icon,
                                            theme = THEME,
                                            timer = 1
                                        )
                                    )
                                else {
                                    queriesDB.insertIntoUser(
                                        User(
                                            user.getIdUser(),
                                            username,
                                            login,
                                            PasswordEncoderFactories.createDelegatingPasswordEncoder()
                                                .encode(passwordTextOne.text),
                                            ADDRESS,
                                            token,
                                            false,
                                            icon = icon,
                                            theme = THEME,
                                            timer = 1
                                        )
                                    )
                                }
                                val fxmlLoader = createFxmlLoader("window.fxml")
                                val stage = showWindow("RIC", Modality.APPLICATION_MODAL, fxmlLoader, true)
                                stage.show()
                            } else errorLabel.text = "Пароли не совпадают."
                        } else errorLabel.text = "Не верные имя или логин."
                    }
                }
            }
        }
    }

    private fun errorMessage(message: String): Boolean {
        return if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "600 No connection") {
            val fxmlLoader = createFxmlLoader("alarmOrInfo.fxml")
            val stage = showWindow("Ошибка", Modality.WINDOW_MODAL, fxmlLoader, false)
            val controller: AlarmOrInfoController = fxmlLoader.getController()
            controller.load(message)
            stage.showAndWait()
            true
        } else false
    }

    @FXML
    private fun onLoginButtonClick() {
        val fxmlLoader = createFxmlLoader("loginWindow.fxml")
        val stage = showWindow("Вход", Modality.APPLICATION_MODAL, fxmlLoader, false)
        stage.show()
    }

    private fun showWindow(title: String, modality: Modality, fxmlLoader: FXMLLoader, isResizable: Boolean): Stage {
        val stage: Stage = loginButton.scene.window as Stage
        if (modality == Modality.APPLICATION_MODAL) {
            stage.close()
        }
        return createStage(fxmlLoader, modality, title, isResizable)
    }

    private fun infoClick(message: String) {
        val fxmlLoader = createFxmlLoader("alarmOrInfo.fxml")
        val stage = showWindow("Справка", Modality.WINDOW_MODAL, fxmlLoader, false)
        val controller: AlarmOrInfoController = fxmlLoader.getController()
        controller.load(message)
        stage.showAndWait()
    }

    @FXML
    private fun nameInfoClick() {
        infoClick("Имя пользователя")
    }

    @FXML
    private fun loginInfoClick() {
        infoClick("Логин")
    }

    @FXML
    private fun tokenInfoClick() {
        infoClick("Токен")
    }

    @FXML
    private fun addressInfoClick() {
        infoClick("Хост")
    }
}