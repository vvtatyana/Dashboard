
var THEME = "light"

const val STYLE_PANEL = "-fx-background-color:  #b3b3b3;"
const val STYLE_FONT = "-fx-font-size: 16px;-fx-font-family: \"Segoe UI Semibold\";"

enum class pref(val prefWidth: Double, val prefHeight: Double){
    INDICATOR (200.0, 200.0),
    CHART (305.0, 305.0)
}

enum class decoration (val prefWidth: Double, val prefHeight: Double, val layoutX: Double, val layoutY: Double, val style: String){
    NAME (0.0, 0.0,14.0, 4.0, "-fx-text-fill: black; -fx-font-size: 14px;-fx-font-family: \"Segoe UI Semibold\";"),
    SETTING (25.0,25.0,25.0,25.0, "./src/main/resources/com/example/controller/images/setting.png"),
    CHARTS (307.0,255.0,0.0,24.0, "")
}

enum class themeSystem (val theme: String, val mainColor: String, val additionalColor: String){
    LIGTH("light", "#ffffff",  "#b3b3b3"),
    DARK("dark", "#000000",  "#333333")
}

fun getMainColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${themeSystem.LIGTH.mainColor}"
        "dark" -> "-fx-background-color: ${themeSystem.DARK.mainColor}"
        else -> "-fx-background-color: ${themeSystem.LIGTH.mainColor}"
    }
}

fun getAdditionalColor(): String{
    return when(THEME){
        "light" -> "-fx-background-color: ${themeSystem.LIGTH.additionalColor}"
        "dark" -> "-fx-background-color: ${themeSystem.DARK.additionalColor}"
        else -> "-fx-background-color: ${themeSystem.LIGTH.additionalColor}"
    }
}