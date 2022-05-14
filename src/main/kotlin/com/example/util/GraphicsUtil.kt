package com.example.util

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.File
import java.io.FileInputStream
import java.net.URL

var THEME = "light"

const val DATA_FORMAT = "dd MMM yyy"
const val TIME_FORMAT = "HH:mm"
const val ICON = "smart_house"

fun theme(): String = File("./src/main/resources/com/example/controller/css/$THEME.css").toURI().toURL().toExternalForm()
fun fxmlLoader(nameFile: String): URL = File("./src/main/resources/com/example/controller/$nameFile").toURI().toURL()

enum class ChartType(val type: String){
    AREA_CHART ("AreaChart"),
    BAR_CHART ("BarChart"),
    LINE_CHART ("LineChart"),
    SCATTER_CHART ("ScatterChart");
}


enum class Pref(val prefWidth: Double, val prefHeight: Double){
    INDICATOR (200.0, 200.0),
    CHART (305.0, 305.0)
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

fun wayToImage(image: String): String{
    return "./src/main/resources/com/example/controller/images/$image.png"
}

fun createImageView(nameFile: String, fit: Double): ImageView{
    val imageView = ImageView(Image(FileInputStream(wayToImage(nameFile))))
    imageView.fitHeight = fit
    imageView.fitWidth = fit
    imageView.isPickOnBounds = true
    imageView.isPreserveRatio = true
    return imageView
}