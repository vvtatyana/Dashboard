package com.example.front

import THEME
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class StartApplication : Application() {
    override fun start(stage: Stage) {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        val user = queriesDB.selectUser(usersTable.CASTLE.name, true.toString())
        database.closeBD()

        if(user != null) {
            ID_USER = user.getId()!!
            HEADERS_AUTH += user.getToken()
            THEME = user.getTheme()

            showWindow(stage, "window.fxml", "Window")
        }
        else{
            showWindow(stage, "loginWindow.fxml", "Login")
        }
    }

    private fun showWindow(stage: Stage, nameFile: String, title: String){
        val fxmlLoader = FXMLLoader(javaClass.getResource(nameFile))
        val scene = Scene(fxmlLoader.load())
        stage.title = title
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(StartApplication::class.java)
}