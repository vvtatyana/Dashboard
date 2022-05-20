package com.example.util

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Modality
import javafx.stage.Stage
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.net.URL

var THEME = "light"

const val DATA_FORMAT = "dd MMM yyy"
const val TIME_FORMAT = "HH:mm"
const val ICON = "smart_house"

const val CHART = "chart"
const val INDICATOR = "indicator"

const val filePath = "./src/main/resources/com/example/controller"
//val filePath = "C:/Users/${System.getProperty("user.name")}/AppData/Roaming/Dashboard/controller"
fun theme(): String = File("$filePath/css/$THEME.css").toURI().toURL().toExternalForm()
fun fxmlLoader(nameFile: String): URL = File("$filePath/$nameFile").toURI().toURL()

enum class ChartType(val type: String){
    AREA_CHART ("AreaChart"),
    BAR_CHART ("BarChart"),
    LINE_CHART ("LineChart"),
    SCATTER_CHART ("ScatterChart");
}


enum class Pref(val size: Double){
    INDICATOR (200.0),
    CHART (305.0)
}

enum class Decoration(val pref: Double, val layoutX: Double, val layoutY: Double, val style: String){
    NAME (0.0, 14.0, 4.0,"-fx-font-size: 15px; -fx-font-family: \"Segoe UI Semibold\";"),
    CHARTS (295.0,0.0,7.0, "")
}


enum class TypeIndicator(val type: String){
    NUMBER ("number"),
    STRING ("string"),
    BOOLEAN ("boolean")
}

fun loadImage(image: String): Image{
    return Image(FileInputStream("$filePath/images/$image.png"))
}

fun createImageView(nameFile: String, fit: Double): ImageView{
    val imageView = ImageView(loadImage(nameFile))
    imageView.fitHeight = fit
    imageView.fitWidth = fit
    imageView.isPickOnBounds = true
    imageView.isPreserveRatio = true
    return imageView
}

fun createFxmlLoader(nameFile: String): FXMLLoader {
    return FXMLLoader(fxmlLoader(nameFile))
}

fun createStage(fxmlLoader: FXMLLoader, modal: Modality, title: String, isResizable: Boolean): Stage {
    val stage = Stage()
    stage.icons.add(loadImage("smart_house"))
    stage.initModality(modal)
    stage.title = title
    stage.isResizable = isResizable
    val scene = Scene(fxmlLoader.load())
    scene.stylesheets.add(theme())
    stage.scene = scene
    return stage
}

fun dropShadow(): DropShadow {
    val dropShadow = DropShadow()
    dropShadow.radius = 0.1
    dropShadow.offsetX = 0.1
    dropShadow.offsetY = 0.1
    dropShadow.color = if (THEME == "light")
        javafx.scene.paint.Color.web("#9ba7c5")
    else javafx.scene.paint.Color.web("white")
    return dropShadow
}