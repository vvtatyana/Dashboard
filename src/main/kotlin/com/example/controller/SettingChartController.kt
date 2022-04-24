package com.example.controller

import com.example.building.Widget
import com.example.util.*
import com.jfoenix.controls.JFXTimePicker
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

class SettingChartController {
    @FXML
    lateinit var toTimePicker: JFXTimePicker

    @FXML
    lateinit var fromTimePicker: JFXTimePicker

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

    lateinit var dataWidget: Widget
    var delete = false
    var save = true

    fun initialize() {
        Tooltip.install(saveImageView, Tooltip("Сохранить изменения"))
        chartsType.items =
            FXCollections.observableArrayList(mutableListOf(ChartType.AREA_CHART.type, ChartType.BAR_CHART.type, ChartType.LINE_CHART.type, ChartType.SCATTER_CHART.type))
    }

    fun load(layoutX: Double, layoutY: Double) {
        this.layoutX = layoutX
        this.layoutY = layoutY
    }

    @FXML
    private fun saveClick() {
        save = true
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
            from = fromTimePicker.value.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
        }
        if (toTimePicker.value != null) {
            to = toTimePicker.value.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
        }
        val type = if (chartsType.value == null) ""
        else chartsType.value
        dataWidget = Widget(nameChart.text, unitChart.text,type, date, from, to)
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }
}