package com.example.controller

import com.example.building.User
import com.example.util.*
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.stage.Stage
import javafx.scene.control.Button

class AccountController {
    @FXML
    private lateinit var exitButton: Button

    @FXML
    private lateinit var login: Label

    @FXML
    private lateinit var username: Label

    @FXML
    private lateinit var tokenText: TextField

    @FXML
    private lateinit var hostText: TextField

    @FXML
    private lateinit var saveButton: Button

    lateinit var user: User

    var exit: Boolean = false
    var save: Boolean = false

    fun initialize() {
        saveButton.tooltip = Tooltip("Сохранить изменения")
        exitButton.tooltip = Tooltip("Выйти из аккаунта")
    }

    fun load(user: User) {
        this.user = user
        username.text = user.getUsername()
        login.text = user.getLogin()
    }

    @FXML
    private fun saveClick() {
        save = true
        val token = tokenText.text
        var host = hostText.text

        if (token != "") {
            HEADERS_AUTH = "Bearer $token"
            user.setToken(token)
        }

        if (host != "") {
            ADDRESS = "https://${host}/api/v1"
            host = ADDRESS
            user.setAddress(host)
        }

        val stage: Stage = saveButton.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun exitClick() {
        exit = true
        val stage: Stage = exitButton.scene.window as Stage
        stage.close()
    }
}
