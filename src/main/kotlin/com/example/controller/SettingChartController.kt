package com.example.controller

import com.example.building.Widget
import com.example.building.WidgetDesigner
import com.example.util.*
import com.jfoenix.controls.JFXTimePicker
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.time.format.DateTimeFormatter
import java.util.*

class SettingChartController {

    @FXML
    private lateinit var toTimePicker: JFXTimePicker

    @FXML
    private lateinit var fromTimePicker: JFXTimePicker

    @FXML
    private lateinit var chartsType: ComboBox<String>

    @FXML
    private lateinit var datePicker: DatePicker

    @FXML
    private lateinit var unitChart: TextField

    @FXML
    private lateinit var nameChart: TextField

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var deleteButton: Button

    private var layoutX: Double = 0.0
    private var layoutY: Double = 0.0

    lateinit var dataWidget: WidgetDesigner
    var delete = false
    var save = false

    fun initialize() {
        saveButton.tooltip = Tooltip("Сохранить изменения")
        chartsType.items =
            FXCollections.observableArrayList(
                mutableListOf(
                    ChartType.AREA_CHART.translation,
                    ChartType.BAR_CHART.translation,
                    ChartType.LINE_CHART.translation,
                    ChartType.SCATTER_CHART.translation
                )
            )
    }
    fun load(layoutX: Double, layoutY: Double, chart: Widget) {
        this.layoutX = layoutX
        this.layoutY = layoutY
        chartsType.value = ChartType.getTranslation(chart.getType()).translation
        nameChart.text = chart.getName()
        unitChart.text = chart.getUnit()
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
        if (fromTimePicker.value != null)
            from = fromTimePicker.value.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
        if (toTimePicker.value != null)
            to = toTimePicker.value.format(DateTimeFormatter.ofPattern(TIME_FORMAT))

        val type = if (chartsType.value == null) ""
        else ChartType.getType(chartsType.value).type
        dataWidget = WidgetDesigner(nameChart.text, unitChart.text, type, date, from, to)
        val stage: Stage = saveButton.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun deleteClick() {
        delete = true
        val stage: Stage = deleteButton.scene.window as Stage
        stage.close()
    }
}