package com.example.controller

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import com.example.util.wayToImage
import java.io.FileInputStream

class LoginController {

    lateinit var passwordReset: Button

    @FXML
    private lateinit var passwordText: PasswordField

    @FXML
    private lateinit var registrationButton: Button

    @FXML
    private lateinit var memoryCheck: CheckBox

    @FXML
    private lateinit var loginText: TextField

    @FXML
    private lateinit var enterButton: Button

    @FXML
    private lateinit var errorLabel: Label

    private lateinit var database: Database

    fun initialize() {
        passwordReset.isVisible = false
    }

    @FXML
    private fun onEnterButtonClick() {
        database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())
        val user = queriesDB.selectUser(UsersTable.LOGIN.name, loginText.text)
        if (user != null && passwordText.text == user.getPassword()) {
            ID_USER = user.getId()!!
            HEADERS_AUTH = "Bearer ${user.getToken()}"
            if (memoryCheck.isSelected) {
                queriesDB.updateUser(ID_USER, UsersTable.CASTLE.name, true.toString())
            }

            database.closeBD()

            var stage: Stage = enterButton.scene.window as Stage
            stage.close()
            val fxmlLoader = FXMLLoader(javaClass.getResource("window.fxml"))
            stage = Stage()
            stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.title = "Window"
            val scene = Scene(fxmlLoader.load())
            scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
            stage.scene = scene
            stage.show()
        } else {
            errorLabel.text = "Не верный логин или пароль."
            passwordReset.isVisible = true
        }
    }

    @FXML
    private fun onRegistrationButtonClick() {
        var stage: Stage = registrationButton.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource("registrationWindow.fxml"))
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage = Stage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = "Registration"
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene
        stage.show()
    }

    @FXML
    fun onPasswordResetClick(){
        database.closeBD()
        var stage: Stage = passwordReset.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource("passwordReset.fxml"))
        stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage.isResizable = false
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene
        stage.show()
    }
}