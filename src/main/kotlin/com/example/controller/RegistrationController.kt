package com.example.controller

import com.example.util.THEME
import com.example.restAPI.*
import com.example.building.User
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Modality
import javafx.stage.Stage
import com.example.util.wayToImage
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.io.FileInputStream

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

    private val database = Database()
    private lateinit var queriesDB: QueriesDB
    fun initialize() {
        Tooltip.install(nameInfoOne, Tooltip("Справка"))
        Tooltip.install(nameInfoTwo, Tooltip("Справка"))
        Tooltip.install(nameInfoThree, Tooltip("Справка"))
        Tooltip.install(nameInfoFour, Tooltip("Справка"))

        queriesDB = QueriesDB(database.getConnection(), database.getStatement())
    }

    @FXML
    private fun onRegistrationButtonClick() {
        val username: String = nameText.text
        val login: String = loginText.text
        val token: String = tokenText.text
        if (hostText.text != "") {
            val defAddress = "https://${hostText.text}/api/v1"
            ADDRESS = defAddress
        }
        val request = RequestGeneration()
        HEADERS_AUTH = "Bearer $token"
        val address = request.addressGeneration(ADDRESS, USERS)
        val getData = request.getRequest(address)
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
                                    null,
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
                                    null,
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
                        database.closeBD()
                        val fxmlLoader = createFxmlLoader("window.fxml")
                        val stage = showWindow("RIC", Modality.APPLICATION_MODAL, fxmlLoader)
                        stage.show()
                    } else errorLabel.text = "Пароли не совпадают."
                } else errorLabel.text = "Не верные имя или логин."
            }
        }
    }

    private fun errorMessage(message: String): Boolean {
        return if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "600 No connection") {
            val fxmlLoader = createFxmlLoader("alarmOrInfo.fxml")
            val stage = showWindow("Error", Modality.WINDOW_MODAL, fxmlLoader)
            val controller: AlarmOrInfoController = fxmlLoader.getController()
            controller.load(message)
            stage.showAndWait()
            true
        } else false
    }

    @FXML
    private fun onLoginButtonClick() {
        val fxmlLoader = createFxmlLoader("loginWindow.fxml")
        val stage = showWindow("Login", Modality.APPLICATION_MODAL, fxmlLoader)
        stage.show()
    }

    private fun createFxmlLoader(nameFile: String): FXMLLoader {
        return FXMLLoader(fxmlLoader(nameFile))
    }

    private fun showWindow(title: String, modal: Modality, fxmlLoader: FXMLLoader): Stage {
        println(modal)
        val stage: Stage = loginButton.scene.window as Stage

        if (modal == Modality.APPLICATION_MODAL) {
            stage.close()
        }
        val newStage = Stage()
        newStage.icons.add(Image(FileInputStream(wayToImage("smart_house"))))
        newStage.initModality(modal)
        newStage.title = title
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(theme())
        newStage.scene = scene
        return newStage
    }

    private fun infoClick(message: String) {
        val fxmlLoader = createFxmlLoader("alarmOrInfo.fxml")
        val stage = showWindow("Info", Modality.WINDOW_MODAL, fxmlLoader)
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