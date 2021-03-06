package com.example.widget

import com.example.util.*
import javafx.geometry.Side
import javafx.scene.chart.*
import javafx.scene.text.Font
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChartWidget(
    id: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val data: List<List<Number>>,
    private var chartType: String
) : AbstractWidget(id, layoutX, layoutY, pref, name) {

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

            val start = if(data.size < 11) 0
            else data.size - 11
            for (i in start until data.size) {
                val time = Date(data[i][0].toLong())
                if (df1.format(time) == day) {
                    val df2 = SimpleDateFormat(TIME_FORMAT)
                    dataChart.data.add(XYChart.Data(df2.format(time), data[i][1].toDouble()))
                }
            }
        }
        return dataChart
    }

    private fun createChart(series: XYChart.Series<String, Number>? = null): XYChart<String, Number> {
        val dataChart = series ?: dataSeries()
        val xAxis = CategoryAxis()
        xAxis.side = Side.BOTTOM
        xAxis.tickLabelFont = Font(FONT_FAMILY, 12.0)

        val yAxis = NumberAxis()
        yAxis.side = Side.BOTTOM

        val areaChart = when (chartType) {
            ChartType.AREA_CHART.type -> AreaChart(xAxis, yAxis)
            ChartType.BAR_CHART.type -> BarChart(xAxis, yAxis)
            ChartType.LINE_CHART.type -> LineChart(xAxis, yAxis)
            ChartType.SCATTER_CHART.type -> ScatterChart(xAxis, yAxis)
            else -> AreaChart(xAxis, yAxis)
        }
        areaChart.layoutX = Decoration.CHARTS.layoutX
        areaChart.layoutY = Decoration.CHARTS.layoutY
        areaChart.prefWidth = Decoration.CHARTS.pref
        areaChart.prefHeight = Decoration.CHARTS.pref - 20
        areaChart.data.add(dataChart)
        return areaChart
    }

    fun getChart(): XYChart<String, Number> = chart

    fun updateChart(series: XYChart.Series<String, Number>){
        panel.children.remove(chart)
        chart = createChart(series)
        panel.children.add(chart)
    }

    fun updateType(chartType: String){
        this.chartType = chartType
        panel.children.remove(chart)
        chart = createChart()
        panel.children.add(chart)
    }
}