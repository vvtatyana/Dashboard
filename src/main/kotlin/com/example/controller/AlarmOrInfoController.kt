package com.example.controller

import com.example.util.loadImage
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.stage.Stage

class AlarmOrInfoController {
    @FXML
    private lateinit var okButton: Button

    @FXML
    private lateinit var infoImage: ImageView

    @FXML
    private lateinit var messageLabel: Label

    @FXML
    private lateinit var headerLabel: Label

    fun load(message: String) {
        if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "No connection") {
            headerLabel.text = message
            infoImage.image = loadImage("error")
            if (message == "401 Unauthorized")
                messageLabel.text = "Токен не указан, или истек его срок действия"
            if (message == "403 Forbidden")
                messageLabel.text = "Запрос недоступен согласно настройкам доступа или ограничениям лицензии"
            if (message == "404 Not Found")
                messageLabel.text = "Запрос несуществующего ресурса"
            if (message == "No connection")
                messageLabel.text = "Нет подключения к интернету или указан не верный хост"
        } else if (message == "На дашборде закончилось место") {
            headerLabel.text = "Внимание"
            infoImage.image = loadImage("warn")
            messageLabel.text = message
        } else {
            headerLabel.text = message
            infoImage.image = loadImage("info")
            if (message == "Имя пользователя")
                messageLabel.text =
                    "Имя пользователя можно узнать на платформе RIC, нажав в правом верхнем углу на Личный кабинет и на профиль."
            if (message == "Логин")
                messageLabel.text =
                    "Логин совпадает с E-mail пользователя платформы RIC, узнать который можно, нажав в правом верхнем углу на Личный кабинет и на профиль."
            if (message == "Токен")
                messageLabel.text =
                    "Токен — это средство авторизации для каждого запроса от клиента к серверу. Токен выдается только один раз при регистрации клиента в системе RIC. Его необходимо сохранить для дальнейшего использования."
            if (message == "Хост")
                messageLabel.text =
                    "Хост, на который будет отправлен запрос. Является полным адресом ресурса. По умолчанию dev.rightech.io."
        }
    }

    @FXML
    private fun okClick(){
        val stage: Stage = okButton.scene.window as Stage
        stage.close()
    }
}