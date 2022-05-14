package com.example.controller

import com.example.util.THEME
import com.example.util.wayToImage
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.FileInputStream

class AlarmOrInfoController {
    lateinit var infoImage: ImageView
    lateinit var messageLabel: Label
    lateinit var headerLabel: Label

    fun load (message: String){
        if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "600 No connection") {
            headerLabel.text = message.subSequence(4, message.length).toString()
            infoImage.image = Image(FileInputStream(wayToImage("error")))
            if (message == "401 Unauthorized")
                messageLabel.text = "Токен не указан, или истек его срок действия"
            if (message == "403 Forbidden")
                messageLabel.text = "Запрос недоступен согласно настройкам доступа или ограничениям лицензии"
            if (message == "404 Not Found")
                messageLabel.text = "Запрос несуществующего ресурса"
            if (message == "600 No connection")
                messageLabel.text = "Нет подключения к интернету или указан не верный хост"
        }
        else {
            headerLabel.text = message
            infoImage.image = Image(FileInputStream(wayToImage("info")))
            if (message == "Имя пользователя")
                messageLabel.text = "Имя пользователя — это уникальный идентификатор пользователя в системе RIC. Узнать его можно на платформе RIC, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
            if (message == "Логин")
                messageLabel.text = "Логин — идентификатор пользователя. Логин совпадает с email пользователя платформы RIC. Узнать его можно на платформе RIC, нажав на стрелку возле аккаунта в левом верхнем углу и выбрав \"Управление проектами\". В появившемся окне нажмите \"Профиль\"."
            if (message == "Токен")
                messageLabel.text = "Токен — это средство авторизации для каждого запроса от клиента к серверу. Токен выдается только один раз при регистрации клиента в системе RIC. Его необходимо сохранить для дальнейшего использования."
            if (message == "Хост")
                messageLabel.text = "Хост, на который будет отправлен запрос. Является полным адресом ресурса. По умолчанию dev.rightech.io."
        }
    }
}