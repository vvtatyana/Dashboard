package com.example.util

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.File
import java.io.FileInputStream
import java.net.URL

var THEME = "light"

const val DATA_FORMAT = "dd MMM yyy"
const val TIME_FORMAT = "HH:mm"
const val ICON = "ric"

const val CHART = "chart"
const val INDICATOR = "indicator"
const val FONT_FAMILY = "Segoe UI Semibold"

//const val filePath = "./src/main/resources/com/example/controller"

val filePath = "C:/Users/${System.getProperty("user.name")}/AppData/Roaming/DashboardRIC/controller"
fun theme(): String = File("$filePath/css/$THEME.css").toURI().toURL().toExternalForm()
fun fxmlLoader(nameFile: String): URL = File("$filePath/$nameFile").toURI().toURL()

enum class ChartType(val translation: String, val type: String) {
    AREA_CHART("Диаграмма с областями", "AreaChart"),
    BAR_CHART("Гистограмма", "BarChart"),
    LINE_CHART("График", "LineChart"),
    SCATTER_CHART("Точечная диаграмма", "ScatterChart");

    companion object {
        fun getTranslation(type: String): ChartType {
            return values().find { it.type == type }!!
        }

        fun getType(translation: String): ChartType {
            return values().find { it.translation == translation }!!
        }
    }
}

enum class Pref(val size: Double) {
    INDICATOR(200.0),
    CHART(300.0)
}

enum class Decoration(val pref: Double, val layoutX: Double, val layoutY: Double, val style: String) {
    NAME(0.0, 14.0, 4.0, "-fx-font-size: 15px; -fx-font-family: \"Segoe UI Semibold\";"),
    CHARTS(290.0, 0.0, 7.0, "")
}


enum class TypeIndicator(val type: String) {
    NUMBER("number"),
    STRING("string"),
    BOOLEAN("boolean")
}

fun loadImage(image: String): Image {
    return Image(FileInputStream("$filePath/images/$image.png"))
}

fun createImageView(nameFile: String, fit: Double): ImageView {
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
    stage.icons.add(loadImage(ICON))
    stage.initModality(modal)
    stage.title = title
    stage.isResizable = isResizable
    val scene = Scene(fxmlLoader.load())
    scene.stylesheets.add(theme())
    stage.scene = scene
    return stage
}

fun dropShadow(): String =
    if (THEME == "light")
        "-fx-effect: dropshadow(one-pass-box, #cacdd5, 1.0, 1.0, 1.0, 1.0);"
    else "-fx-effect: dropshadow(one-pass-box, #23242f, 1.0, 1.0, 1.0, 1.0);"
