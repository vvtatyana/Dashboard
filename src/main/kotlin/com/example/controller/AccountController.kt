package com.example.controller

import com.example.building.User
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.stage.Stage
import javafx.scene.control.Button

class AccountController {
    @FXML
    private lateinit var errorLabel: Label

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
        val host = hostText.text

        var goodFlag = true

        if (token != "") {
            if (check(token, user.getAddress())){
                user.setToken(token)
            }
            else {
                goodFlag = false
                HEADERS_AUTH = "Bearer ${user.getToken()}"
                errorLabel.text = "Не верный токен"
            }
        }

        if (host != "") {
            if (check(user.getToken(), host)){
                user.setAddress(ADDRESS)
            }
            else {
                if (goodFlag)
                    errorLabel.text = "Не верный хост"
                else errorLabel.text = "Не верные токен и хост"
                goodFlag = false
                ADDRESS = user.getAddress()
            }
        }

        if (goodFlag) {
            val stage: Stage = saveButton.scene.window as Stage
            stage.close()
        }
    }

    @FXML
    private fun exitClick() {
        exit = true
        val stage: Stage = exitButton.scene.window as Stage
        stage.close()
    }

    private fun errorMessage(message: String): Boolean =
        message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "No connection"

    private fun check(token: String, host: String): Boolean {
        HEADERS_AUTH = "Bearer $token"
        ADDRESS = "https://${host}/api/v1"
        val request = RequestGeneration()
        val address = request.addressGeneration(ADDRESS, USERS)
        val getData = request.getRequest(address)
        if (!errorMessage(getData)) {
            val json: JsonArray = Gson().fromJson(getData, JsonArray::class.java)
            val users = ProcessingJSON().readAllUsers(json)
            for (user in users) {
                if (user.getLogin() == this.user.getLogin()) {
                    return true
                }
            }
        }
        return false
    }

}
