package com.example.front

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import getAdditionalColor
import getMainColor
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage

class LoginController {
    @FXML
    lateinit var dataPane: AnchorPane
    @FXML
    lateinit var mainPane: AnchorPane
    @FXML
    lateinit var registrationButton: Button
    @FXML
    lateinit var memoryCheck: CheckBox
    @FXML
    private lateinit var loginText: TextField
    @FXML
    private lateinit var inputButton: Button
    @FXML
    private lateinit var errorLabel: Label

    fun initialize() {
        mainPane.style = getMainColor()
        dataPane.style = getAdditionalColor()
    }

    @FXML
    private fun onInputButtonClick() {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())
        val user = queriesDB.selectUser(usersTable.LOGIN.name, loginText.text)
        if (user != null) {
            ID_USER = user.getId()!!
            HEADERS_AUTH += user.getToken()

            if (memoryCheck.isSelected) {
                queriesDB.updateUser(ID_USER, usersTable.CASTLE.name, true.toString())
            }

            database.closeBD()
            var stage: Stage = inputButton.scene.window as Stage
            stage.close()
            val fxmlLoader = FXMLLoader(javaClass.getResource("window.fxml"))
            stage = Stage()
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.title = "Window"
            stage.scene = Scene(fxmlLoader.load())
            stage.show()
        }
        else {
            errorLabel.text = "Не верный логин"
        }
    }

    @FXML
    private fun onRegistrationButtonClick(){
        var stage: Stage = registrationButton.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource("registrationWindow.fxml"))
        stage = Stage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = "Registration"
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }
}