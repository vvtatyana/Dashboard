package com.example.util

import javafx.scene.effect.DropShadow
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

var THEME = "light"

const val DATA_FORMAT = "dd MMM yyy"
const val TIME_FORMAT = "HH:mm"

enum class ChartType(val type: String){
    AREA_CHART ("AreaChart"),
    BAR_CHART ("BarChart"),
    LINE_CHART ("LineChart"),
    SCATTER_CHART ("ScatterChart")
}

enum class Pref(val prefWidth: Double, val prefHeight: Double){
    INDICATOR (200.0, 200.0),
    CHART (305.0, 305.0)
}

enum class Decoration(val pref: Double, val layoutX: Double, val layoutY: Double, val style: String){
    NAME (0.0, 14.0, 4.0, textStyle(16)),
    SETTING (25.0,27.0,27.0, wayToImage("other/settings")),
    CHARTS (307.0,0.0,24.0, "")
}

enum class ThemeSystem(val theme: String, val mainColor: String, val additionalColor: String, val shadowColor: String){
    LIGHT("light", "linear-gradient(to left, #d0daf0, #bac9e8)",  "linear-gradient(to left, #d0daf0, #bac9e8)", "#253554"),
    DARK("dark", "linear-gradient(to left, #2b4b6e, #1d334a)",  "linear-gradient(to left, #2b4b6e, #1d334a)", "#000000")
}

enum class TypeIndicator(val type: String){
    NUMBER ("number"),
    STRING ("string"),
    BOOLEAN ("boolean")
}

fun wayToImage(image: String): String{
    return "./src/main/resources/com/example/controller/images/$image.png"
}

fun getMainColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${ThemeSystem.LIGHT.mainColor}"
        "dark" -> "-fx-background-color: ${ThemeSystem.DARK.mainColor}"
        else -> "-fx-background-color: ${ThemeSystem.LIGHT.mainColor}"
    }
}

fun getAdditionalColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${ThemeSystem.LIGHT.additionalColor};"
        "dark" -> "-fx-background-color: ${ThemeSystem.DARK.additionalColor};"
        else -> "-fx-background-color: ${ThemeSystem.LIGHT.additionalColor};"
    }
}

fun dropShadow(): DropShadow {
    val dropShadow = DropShadow()
    dropShadow.radius = 0.0
    dropShadow.offsetX = 0.0
    dropShadow.offsetY = 0.0
    dropShadow.color = when(THEME){
        "light" -> Color.web(ThemeSystem.LIGHT.shadowColor)
        "dark" -> Color.web(ThemeSystem.DARK.shadowColor)
        else -> Color.web(ThemeSystem.LIGHT.shadowColor)
    }
    return dropShadow
}

fun textStyle(size: Int, color: String = textTheme()): String{
    return "-fx-text-fill: $color; -fx-font-size: ${size}px; -fx-font-family: \"Segoe UI Semibold\";"
}

fun panelTheme(): String{
    return when(THEME){
        "light" -> "-fx-background-color: linear-gradient(to left, #e9edf5, #ced9ed); -fx-background-radius: 2;"
        "dark" -> "-fx-background-color: linear-gradient(to left, #2f649d, #224c77); -fx-background-radius: 2;"
        else -> "-fx-background-color: linear-gradient(to left, #e9edf5, #ced9ed); -fx-background-radius: 2;"
    }
}

fun textTheme(): String{
    return when(THEME){
        "light" -> "#3b507b"
        "dark" -> "#dae5f2"
        else -> "#3b507b"
    }
}

fun themePane(dataPane: AnchorPane, headerPane: AnchorPane? = null){
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