package com.example.controller

import com.example.building.User
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.io.FileInputStream

class PasswordResetController {
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
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())
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
                val fxmlLoader = FXMLLoader(fxmlLoader("loginWindow.fxml"))
                stage = Stage()
                stage.initModality(Modality.WINDOW_MODAL)
                stage.icons.add(Image(FileInputStream(wayToImage(ICON))))
                stage.isResizable = false
                val scene = Scene(fxmlLoader.load())
                scene.stylesheets.add(theme())
                stage.scene = scene
                stage.show()
            } else errorLabel.text = "Пароли не совпадают."
        } else errorLabel.text = "Пользователя с таким токеном нет."
        database.closeBD()
    }
}