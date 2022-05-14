package com.example.controller

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage


class StartApplication : Application() {

    override fun start(stage: Stage) {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        val user = queriesDB.selectUser(UsersTable.CASTLE.name, true.toString())
        database.closeBD()
        if(user != null) {
            HEADERS_AUTH = "Bearer " + user.getToken()
            THEME = user.getTheme()
            showWindow(stage, "window.fxml", "RIC", true)
        }
        else{
            showWindow(stage, "loginWindow.fxml", "Вход", false)
        }
    }

    /**
     * Открытие нового окна
     * @stage - контейнер для окна
     * @nameFile - название файла окна
     * @title - название окна
     */
    private fun showWindow(stage: Stage, nameFile: String, title: String, isResizable: Boolean){
        val scene = Scene(createFxmlLoader(nameFile).load())
        stage.icons.add(loadImage(ICON))
        stage.isResizable = isResizable
        stage.title = title
        scene.stylesheets.add(theme())
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(StartApplication::class.java)
}