package com.example.controller

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import com.example.util.dropShadow
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

/**
* Класс создает окно для входа
*/
class LoginController {

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var mainPane: AnchorPane

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

    /**
     * Инициализация окна
     */
    fun initialize() {
        themePane(mainPane, dataPane)

        dataPane.effect = dropShadow()
        for(ch in dataPane.children){
            if(ch.layoutY != 203.0)
                ch.effect = dropShadow()
        }
    }

    /**
     * Обработка нажатия кнопки войти
     */
    @FXML
    private fun onEnterButtonClick() {
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

            var stage: Stage = enterButton.scene.window as Stage
            stage.close()
            val fxmlLoader = FXMLLoader(javaClass.getResource("window.fxml"))
            stage = Stage()
            stage.icons.add(Image(FileInputStream(wayToImage("iot"))))
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.title = "Window"
            stage.scene = Scene(fxmlLoader.load())
            stage.show()
        } else {
            errorLabel.text = "Не верный логин"
        }
    }

    /**
     * Обработка нажатия кнопки зарегестрироваться
     */
    @FXML
    private fun onRegistrationButtonClick() {
        var stage: Stage = registrationButton.scene.window as Stage
        stage.close()
        val fxmlLoader = FXMLLoader(javaClass.getResource("registrationWindow.fxml"))
        stage.icons.add(Image(FileInputStream(wayToImage("iot"))))
        stage = Stage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = "Registration"
        stage.scene = Scene(fxmlLoader.load())
        stage.show()
    }
}