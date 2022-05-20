package com.example.controller

import com.example.building.User
import com.example.database.QueriesDB
import com.example.util.*
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import org.springframework.security.crypto.factory.PasswordEncoderFactories

class PasswordResetController {

    @FXML
    private lateinit var backButton: Button

    @FXML
    private lateinit var passwordTextTwo: PasswordField

    @FXML
    private lateinit var passwordTextOne: PasswordField

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var enterButton: Button

    @FXML
    private lateinit var tokenText: TextField

    lateinit var user: User

    @FXML
    fun onEnterButtonClick() {
        if (tokenText.text.isEmpty()) errorLabel.text = "Введите токен"
        else if (passwordTextOne.text.isEmpty()) errorLabel.text = "Введите пароль"
        else if (passwordTextTwo.text.isEmpty()) errorLabel.text = "Введите второй пароль"
        else if (tokenText.text.isNotEmpty() && passwordTextOne.text.isNotEmpty() && passwordTextTwo.text.isNotEmpty()) {
            val queriesDB = QueriesDB()
            val user = queriesDB.selectUser(UsersTable.TOKEN.name, tokenText.text)
            if (user != null) {
                if (passwordTextOne.text == passwordTextTwo.text) {
                    queriesDB.updateUser(
                        user.getId(),
                        UsersTable.PASSWORD.name,
                        PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(passwordTextOne.text)
                    )
                    var stage: Stage = enterButton.scene.window as Stage
                    stage.close()
                    stage = createStage(createFxmlLoader("loginWindow.fxml"), Modality.WINDOW_MODAL, "Вход", false)
                    stage.show()
                } else errorLabel.text = "Пароли не совпадают."
            } else errorLabel.text = "Пользователя с таким токеном нет."
        }
    }

    @FXML
    fun onBackButtonClick(){
        var stage: Stage = backButton.scene.window as Stage
        stage.close()
        stage = createStage(createFxmlLoader("loginWindow.fxml"), Modality.WINDOW_MODAL, "Вход", false)
        stage.show()
    }
}