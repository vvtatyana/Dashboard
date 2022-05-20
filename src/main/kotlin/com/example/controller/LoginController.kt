package com.example.controller

import com.example.database.QueriesDB
import com.example.util.*
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import org.springframework.security.crypto.factory.PasswordEncoderFactories

class LoginController {

    @FXML
    private lateinit var passwordReset: Button

    @FXML
    private lateinit var passwordText: PasswordField

    @FXML
    private lateinit var registrationButton: Button

    @FXML
    private lateinit var memoryCheck: CheckBox

    @FXML
    private lateinit var loginText: TextField

    @FXML
    private lateinit var errorLabel: Label

    fun initialize() {
        passwordReset.isVisible = false
    }

    @FXML
    private fun onEnterButtonClick() {
        if (loginText.text.isEmpty()) errorLabel.text = "Введите логин"
        else if (passwordText.text.isEmpty()) errorLabel.text = "Введите пароль"
        else if (loginText.text.isNotEmpty() && passwordText.text.isNotEmpty()) {
            val queriesDB = QueriesDB()
            val user = queriesDB.selectUser(UsersTable.LOGIN.name, loginText.text)

            if (user != null && PasswordEncoderFactories.createDelegatingPasswordEncoder()
                    .matches(passwordText.text, user.getPassword())
            ) {
                HEADERS_AUTH = "Bearer ${user.getToken()}"
                if (memoryCheck.isSelected) {
                    queriesDB.updateUser(user.getId(), UsersTable.CASTLE.name, true.toString())
                }

                showWindow(Modality.APPLICATION_MODAL, "window.fxml", "RIC", true)

            } else {
                errorLabel.text = "Не верный логин или пароль."
                passwordReset.isVisible = true
            }
        }
    }

    @FXML
    private fun onRegistrationButtonClick() {
        showWindow(Modality.APPLICATION_MODAL, "registrationWindow.fxml", "Регистрация", false)
    }

    @FXML
    fun onPasswordResetClick() {
        showWindow(Modality.WINDOW_MODAL, "passwordReset.fxml", "Восстановление пароля", false)
    }

    private fun showWindow(modality: Modality, nameFile: String, title: String, isResizable: Boolean) {
        var stage: Stage = registrationButton.scene.window as Stage
        stage.close()
        stage = createStage(createFxmlLoader(nameFile), modality, title, isResizable)
        stage.show()
    }
}