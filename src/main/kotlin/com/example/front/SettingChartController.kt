package com.example.front

import getAdditionalColor
import getMainColor
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.DatePicker
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.util.*

class SettingChartController {
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

    fun initialize() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        dataPane.style = getAdditionalColor()
        chartsType.items =
            FXCollections.observableArrayList(mutableListOf("AreaChart", "BarChart", "LineChart", "ScatterChart"))
    }

    fun load(layoutX: Double, layoutY: Double){
        this.layoutX = layoutX
        this.layoutY = layoutY
    }

    @FXML
    private fun saveClick(){
        var date = ""
        if (datePicker.value!=null) {
            val cal = Calendar.getInstance()
            cal[Calendar.DAY_OF_MONTH] = datePicker.value.dayOfMonth
            cal[Calendar.MONTH] = datePicker.value.monthValue - 1
            cal[Calendar.YEAR] = datePicker.value.year
            date = cal.timeInMillis.toString()
        }
        val type = if (chartsType.value == null) ""
        else chartsType.value
        list = mutableListOf(nameChart.text, unitChart.text, date, type )
        val stage: Stage = saveImageView.scene.window as Stage
        stage.close()
    }

    @FXML
    private fun deleteClick(){
        delete = true
        val stage: Stage = deleteImageView.scene.window as Stage
        stage.close()
    }
}