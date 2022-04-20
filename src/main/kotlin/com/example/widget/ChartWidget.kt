package com.example.widget

import com.example.util.*
import javafx.geometry.Side
import javafx.scene.chart.*
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChartWidget(
    indicator: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val data: List<List<Number>>,
    private val chartType: String
) : AbstractWidget(indicator, layoutX, layoutY, pref, name) {

    private var chart: XYChart<String, Number>

    init {
        chart = createChart()
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(chart)
        panel.children.add(setting)
    }

    private fun dataSeries(): XYChart.Series<String, Number> {
        val dataChart = XYChart.Series<String, Number>()
        val df1: DateFormat = SimpleDateFormat(DATA_FORMAT)
        val day: String
        if (data.isNotEmpty()) {
            day = df1.format(Date(data[data.size - 1][0].toLong()))
            dataChart.name = day

            for (t in data) {
                val time = Date(t[0].toLong())
                if (df1.format(time) == day) {
                    val df2 = SimpleDateFormat(TIME_FORMAT)
                    dataChart.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
                }
            }
        }
        return dataChart
    }

    private fun createChart(): XYChart<String, Number> {
        val dataChart = dataSeries()
        val xAxis = CategoryAxis()
        xAxis.side = Side.BOTTOM
        xAxis.tickLabelFill = Paint.valueOf(textTheme())
        xAxis.tickLabelFont = Font("Segoe UI Semibold", 12.0)

        val yAxis = NumberAxis()
        yAxis.side = Side.BOTTOM
        yAxis.tickLabelFill = Paint.valueOf(textTheme())

        val areaChart = when (chartType) {
            ChartType.AREA_CHART.type -> {
                AreaChart(xAxis, yAxis)
            }
            ChartType.BAR_CHART.type -> {
                BarChart(xAxis, yAxis)
            }
            ChartType.LINE_CHART.type -> {
                LineChart(xAxis, yAxis)
            }
            ChartType.SCATTER_CHART.type -> {
                ScatterChart(xAxis, yAxis)
            }
            else -> {
                AreaChart(xAxis, yAxis)
            }
        }
        areaChart.layoutX = Decoration.CHARTS.layoutX
        areaChart.layoutY = Decoration.CHARTS.layoutY
        areaChart.prefWidth = Decoration.CHARTS.pref
        areaChart.prefHeight = Decoration.CHARTS.pref
        areaChart.data.add(dataChart)
        return areaChart
    }

    fun getChart(): XYChart<String, Number> = chart
}