package com.example.util

import javafx.scene.effect.DropShadow
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

var THEME = "light"

val STYLE_PANEL = panelTheme()
const val DATA_FORMAT = "dd MMM yyy"
const val TIME_FORMAT = "HH:mm"

const val AREA_CHART = "AreaChart"
const val BAR_CHART = "BarChart"
const val LINE_CHART = "LineChart"
const val SCATTER_CHART = "ScatterChart"

enum class pref(val prefWidth: Double, val prefHeight: Double){
    INDICATOR (200.0, 200.0),
    CHART (305.0, 305.0)
}

enum class decoration (val prefWidth: Double, val prefHeight: Double, val layoutX: Double, val layoutY: Double, val style: String){
    NAME (0.0, 0.0,14.0, 4.0, textStyle(16)),
    SETTING (25.0,25.0,27.0,27.0, wayToImage("other/settings")),
    CHARTS (307.0,255.0,0.0,24.0, "")
}

enum class themeSystem (val theme: String, val mainColor: String, val additionalColor: String, val shadowColor: String){
    LIGHT("light", "linear-gradient(to left, #c5d0e6, #a3b4d6)",  "linear-gradient(to left, #c5d0e6, #a3b4d6)", "#253554"),
    DARK("dark", "linear-gradient(to left, #2b4b6e, #1d334a)",  "linear-gradient(to left, #2b4b6e, #1d334a)", "#000000")
}

enum class typeIndicator (val type: String){
    NUMBER ("number"),
    STRING ("string"),
    BOOLEAN ("boolean")
}

fun wayToImage(image: String): String{
    return "./src/main/resources/com/example/controller/images/$image.png"
}

fun getMainColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${themeSystem.LIGHT.mainColor}"
        "dark" -> "-fx-background-color: ${themeSystem.DARK.mainColor}"
        else -> "-fx-background-color: ${themeSystem.LIGHT.mainColor}"
    }
}

fun getAdditionalColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${themeSystem.LIGHT.additionalColor}"
        "dark" -> "-fx-background-color: ${themeSystem.DARK.additionalColor}"
        else -> "-fx-background-color: ${themeSystem.LIGHT.additionalColor}"
    }
}

fun dropShadow(): DropShadow {
    val dropShadow = DropShadow()
    dropShadow.radius = 0.0
    dropShadow.offsetX = 0.0
    dropShadow.offsetY = 0.0
    dropShadow.color = when(THEME){
        "light" -> Color.web(themeSystem.LIGHT.shadowColor)
        "dark" -> Color.web(themeSystem.DARK.shadowColor)
        else -> Color.web(themeSystem.LIGHT.shadowColor)
    }
    return dropShadow
}

fun textStyle(size: Int): String{
    return "-fx-text-fill: ${textTheme()}; -fx-font-size: ${size}px; -fx-font-family: \"Segoe UI Semibold\";"
}

fun panelTheme(): String{
    return when(THEME){
        "light" -> "-fx-background-color: linear-gradient(to left, #e9edf5, #ced9ed);"
        "dark" -> "-fx-background-color: linear-gradient(to left, #6d99c7, #487eb8);"
        else -> "-fx-background-color: linear-gradient(to left, #e9edf5, #ced9ed);"
    }
}

fun textTheme(): String{
    return when(THEME){
        "light" -> "#3b507b"
        "dark" -> "#dae5f2"
        else -> "#3b507b"
    }
}

fun themePane(mainPane: AnchorPane, dataPane: AnchorPane, headerPane: AnchorPane? = null){
    mainPane.style = getAdditionalColor()

    dataPane.style = getAdditionalColor()

    if (headerPane != null) {
        headerPane.style = getAdditionalColor()
    }
}

fun shadowPane(dataPane: AnchorPane, headerPane: AnchorPane? = null){

    dataPane.effect = dropShadow()
    for(ch in dataPane.children){
        ch.effect = dropShadow()
    }

    if (headerPane != null) {
        headerPane.effect = dropShadow()
        for (ch in headerPane.children) {
            ch.effect = dropShadow()
        }
    }
}