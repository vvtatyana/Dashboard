package com.example.controller

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage
import com.example.util.wayToImage
import java.io.FileInputStream

class LoginController {

    @FXML
    private lateinit var dataPane: AnchorPane

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

    fun initialize() {
        themePane(dataPane)
    }

    @FXML
    private fun onEnterButtonClick() {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())
        val user = queriesDB.selectUser(UsersTable.LOGIN.name, loginText.text)
        if (user != null) {
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
            stage.scene = Scene(fxmlLoader.load())
            stage.show()
        } else {
            errorLabel.text = "Не верный логин"
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
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }
}