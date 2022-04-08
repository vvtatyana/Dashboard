package com.example.controller

import com.jfoenix.controls.JFXTimePicker
import getAdditionalColor
import getMainColor
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.time.format.DateTimeFormatter
import java.util.*

/**
* Класс создает окно для настройки графика
*/
class SettingChartController {
    @FXML
    lateinit var toTimePicker: JFXTimePicker

    @FXML
    lateinit var fromTimePicker: JFXTimePicker

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var headerPane: AnchorPane

    @FXML
    private lateinit var dataPane: AnchorPane

    @FXML
    private lateinit var chartsType: ComboBox<String>

    @FXML
    private lateinit var datePicker: DatePicker

    @FXML
    private lateinit var unitChart: TextField

    @FXML
    private lateinit var nameChart: TextField

    @FXML
    private lateinit var saveImageView: ImageView

    @FXML
    private lateinit var deleteImageView: ImageView

    private var layoutX: Double = 0.0
    private var layoutY: Double = 0.0

    var list = mutableListOf<String>()
    var delete = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
        chartsType.items =
            FXCollections.observableArrayList(mutableListOf("AreaChart", "BarChart", "LineChart", "ScatterChart"))
    }

    /**
    * Получение данных из основного окна
    * @layoutX - позиция по оси X
    * @layoutY - позиция по оси Y
    */
    fun load(layoutX: Double, layoutY: Double) {
        this.layoutX = layoutX
        this.layoutY = layoutY
    }

    /**
     * Обработка нажатия кнопки сохранения
     */
    @FXML
    private fun saveClick() {
        var date = ""
        var from = ""
        var to = ""
        if (datePicker.value != null) {
            val cal = Calendar.getInstance()
            cal[Calendar.DAY_OF_MONTH] = datePicker.value.dayOfMonth
            cal[Calendar.MONTH] = datePicker.value.monthValue - 1
            cal[Calendar.YEAR] = datePicker.value.year
            date = cal.timeInMillis.toString()
        }
        if (fromTimePicker.value != null) {
            from = fromTimePicker.value.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        if (toTimePicker.value != null) {
            to = toTimePicker.value.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        val type = if (chartsType.value == null) ""
        else chartsType.value
        list = mutableListOf(nameChart.text, unitChart.text, date, from, to, type)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    /**
     * Обработка нажатия кнопки удалить
     */
    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }
}