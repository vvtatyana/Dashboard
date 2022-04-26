package com.example.controller

import com.example.database.Database
import com.example.database.QueriesDB
import com.example.util.*
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.FileInputStream

class StartApplication : Application() {

    override fun start(stage: Stage) {
        val database = Database()
        val queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        val user = queriesDB.selectUser(UsersTable.CASTLE.name, true.toString())
        database.closeBD()
        if(user != null) {
            ID_USER = user.getId()!!
            HEADERS_AUTH += user.getToken()
            THEME = user.getTheme()

            showWindow(stage, "window.fxml", "RIC")
        }
        else{
            showWindow(stage, "loginWindow.fxml", "Вход")
        }
    }

    /**
     * Открытие нового окна
     * @stage - контейнер для окна
     * @nameFile - название файла окна
     * @title - название окна
     */
    private fun showWindow(stage: Stage, nameFile: String, title: String){
        val fxmlLoader = FXMLLoader(javaClass.getResource(nameFile))
        val scene = Scene(fxmlLoader.load())
        stage.initStyle(StageStyle.DECORATED)

        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        if (nameFile == "loginWindow.fxml"){
            stage.isResizable = false
        }
        stage.title = title
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(StartApplication::class.java)
}