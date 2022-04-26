package com.example.util

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
    NAME (0.0, 14.0, 4.0,""),
    SETTING (25.0,27.0,27.0, wayToImage("other/settings")),
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