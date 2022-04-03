package com.example.front

import STYLE_PANEL
import THEME
import com.example.back.Parsing
import com.example.back.RequestGeneration
import com.example.building.*
import com.example.building.Chart
import com.example.database.*
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jfoenix.controls.JFXListView
import decoration
import eu.hansolo.medusa.Gauge.SkinType
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import getAdditionalColor
import getMainColor
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.chart.*
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import pref
import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class WindowController {
    lateinit var indicatorImageView: ImageView
    lateinit var chartImageView: ImageView
    lateinit var setting: ImageView
    lateinit var username: Label
    lateinit var addClick: ImageView
    lateinit var indicatorPane: AnchorPane
    lateinit var chartPane: AnchorPane
    lateinit var dataList: JFXListView<String>
    private val request = RequestGeneration()
    private lateinit var objectData: Object
    private lateinit var user: User
    private var listObjects = mutableListOf<Object>()

    lateinit var mainPane: AnchorPane
    lateinit var headerPane: AnchorPane

    private lateinit var queriesDB: QueriesDB
    private val database = Database()

    //Иниуиализация при открытии окна
    fun initialize() {
        updateTheme()
        queriesDB = QueriesDB(database.getConnection(), database.getStatement())
        dataList.isVisible = false
        indicatorImageView.isVisible = false
        chartImageView.isVisible = false
        chartPane.isVisible = false
        val userBD = queriesDB.selectUser(usersTable.ID.name, ID_USER.toString())
        if (userBD != null)
            user = userBD
            username.text = user.getUsername()
        objects()
    }

    //Считывает все объекты у пользоввателя и загружает в бд если их там нет
    private fun objects() {
        val strOb = addressAssemblyGET(DEFAULT_ADDRESS, OBJECTS)
        val jsonOb = Gson().fromJson(strOb, JsonArray::class.java)
        listObjects = Parsing().readAllObjects(jsonOb) as MutableList<Object>

        for (obj in listObjects) {
            val objDB = queriesDB.selectObject(objectsTable.ID_OBJECT.name, obj.getIdObject())
            if (objDB == null) {
                queriesDB.insertIntoObject(obj)
            }
        }
        showObject()
    }

    //Выводит пользователю список объектов
    private fun showObject() {
        val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS, requestCharacters.SLESH.code)
        val nameListObjects = ArrayList<String>()
        for (obj in listObjects) {
            nameListObjects.add(obj.getNameObject())
            val namesObjects = FXCollections.observableArrayList(nameListObjects)
            dataList.items = namesObjects
            dataList.isVisible = true

            dataList.onMouseClicked = EventHandler {

                indicatorImageView.isVisible = true
                chartImageView.isVisible = true
                val newValue = dataList.selectionModel.selectedItem

                indicatorPane.children.remove(0, indicatorPane.children.size)


                chartPane.children.remove(0, chartPane.children.size)


                val objDB = queriesDB.selectObject(objectsTable.NAME_OBJECT.name, newValue)

                if (objDB != null) {
                    objectData = objDB
                }

                val values = valuesObject(newValue, listObjects, address)
                val data = objectsData(newValue, listObjects, address, values)
                uploadObjects(data)
            }

        }
    }

    // Добавляет индикатор в дашбоард и бд
    private fun addObjects(
        add_Object: String, nameText: String, unitText: String, chartType: String
    ) {
        var address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS, requestCharacters.SLESH.code)
        val obj = queriesDB.selectObject(objectsTable.ID.name, objectData.getId().toString())
        val data = objectData(add_Object, obj!!.getIdObject(), address)
        if (chartPane.isVisible) {
            val pos = getPositionChart()
            val panel = AnchorPane()
            panel.style = STYLE_PANEL
            panel.layoutX = pos[0]
            panel.layoutY = pos[1]
            panel.prefWidth = pref.CHART.prefWidth
            panel.prefHeight = pref.CHART.prefHeight

            val name = Label("$nameText, $unitText")
            name.layoutX = decoration.NAME.layoutX
            name.layoutY = decoration.NAME.layoutY
            name.style = decoration.NAME.style
            panel.children.add(name)

            val tp = chartData(add_Object, objectData.getIdObject())
            val dataChart = Series<String, Number>()
            val df1: DateFormat = SimpleDateFormat("dd MMM yyy")
            val day = df1.format(Date(tp[tp.size - 1][0].toLong()))

            dataChart.name = day
            for (t in tp) {
                val time = Date(t[0].toLong())
                if (df1.format(time) == day) {
                    val df2 = SimpleDateFormat("HH:mm")
                    dataChart.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
                }
            }

            val xAxis = CategoryAxis()
            val yAxis = NumberAxis()
            val areaChart = when (chartType) {
                "AreaChart" -> {
                    AreaChart(xAxis, yAxis)
                }
                "BarChart" -> {
                    BarChart(xAxis, yAxis)
                }
                "LineChart" -> {
                    LineChart(xAxis, yAxis)
                }
                "ScatterChart" -> {
                    ScatterChart(xAxis, yAxis)
                }
                else -> {
                    AreaChart(xAxis, yAxis)
                }
            }
            areaChart.layoutX = decoration.CHARTS.layoutX
            areaChart.layoutY = decoration.CHARTS.layoutY
            areaChart.prefWidth = decoration.CHARTS.prefWidth
            areaChart.prefHeight = decoration.CHARTS.prefHeight
            areaChart.data.add(dataChart)
            panel.children.add(areaChart)


            val setting = ImageView(Image(FileInputStream(decoration.SETTING.style)))
            setting.fitWidth = decoration.SETTING.prefWidth
            setting.fitHeight = decoration.SETTING.prefHeight
            setting.layoutX = pref.CHART.prefWidth - decoration.SETTING.layoutX
            setting.layoutY = pref.CHART.prefHeight - decoration.SETTING.layoutY
            setting.onMouseClicked = EventHandler {
                val fxmlLoader = FXMLLoader(javaClass.getResource("settingChart.fxml"))
                val stage = Stage()
                stage.initModality(Modality.WINDOW_MODAL)
                stage.isResizable = false
                stage.scene = Scene(fxmlLoader.load())

                val controller: SettingChartController = fxmlLoader.getController()
                controller.load(pos[0], pos[1])

                stage.showAndWait()
                if (controller.delete) {
                    chartPane.children.remove(panel)
                    queriesDB.deleteChart(areaChart.layoutX, areaChart.layoutY)
                } else {
                    val addList = controller.list

                    if (addList.size > 1) {
                        updateObject(areaChart.layoutX, areaChart.layoutY, addList[0], addList[1], addList[3])
                        if (addList[0] != "") name.text = addList[0] + name.text.substring(
                            name.text.indexOf(",", 0), name.text.length
                        )
                        if (addList[1] != "") name.text =
                            name.text.substring(0, name.text.indexOf(",", 0) + 1) + addList[1]
                        if (addList[2] != "") {
                            val datas = Series<String, Number>()
                            val df1 = SimpleDateFormat("dd MMM yyy")
                            val day = df1.format(Date(addList[2].toLong()))

                            datas.name = day
                            areaChart.data.remove(0, areaChart.data.size)
                            areaChart.data.add(datas)
                            for (t in tp) {
                                val time = Date(t[0].toLong())
                                if (df1.format(time) == day) {
                                    val df2 = SimpleDateFormat("HH:mm")
                                    datas.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
                                }
                            }
                        }

                    }
                }
            }
            panel.children.add(setting)
            chartPane.children.add(panel)

            queriesDB.insertIntoChart(
                Chart(
                    null, objectData.getIdObject(), add_Object, pos[0], pos[1], nameText, unitText, chartType
                )
            )

        }

        if (indicatorPane.isVisible) {
            val pos = getPositionIndicator()
            val panel = AnchorPane()
            panel.style = STYLE_PANEL
            panel.layoutX = pos[0]
            panel.layoutY = pos[1]
            panel.prefWidth = pref.INDICATOR.prefWidth
            panel.prefHeight = pref.INDICATOR.prefHeight

            val name = Label(nameText)
            name.layoutX = decoration.NAME.layoutX
            name.layoutY = decoration.NAME.layoutY
            name.style = decoration.NAME.style
            panel.children.add(name)

            address = request.addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)
            val str = addressAssemblyGET(
                address, queriesDB.selectObject(
                    objectsTable.ID.name, objectData.getId().toString()
                )!!.getIdModel()
            )
            val jsonM = Gson().fromJson(str, JsonObject::class.java)
            val border = Parsing().readBorder(jsonM)

            val gauge = GaugeBuilder.create()
                .skinType(SkinType.SIMPLE)
                .minValue(border["Min"]!!.toDouble() - 10)
                .maxValue(border["Max"]!!.toDouble() + 10)
                .sections(
                    Section(border["Min"]!!.toDouble() - 10, border["Min"]!!.toDouble(), "0", Color.web("#ff4e33")),
                    Section(border["Min"]!!.toDouble(), border["Mid"]!!.toDouble(), "1", Color.web("#6bdb6b")),
                    Section(border["Mid"]!!.toDouble(), border["Max"]!!.toDouble(), "2", Color.web("#ffba42")),
                    Section(border["Max"]!!.toDouble(), border["Max"]!!.toDouble() + 10, "3", Color.web("#ff4e33"))
                )
                .sectionsVisible(true)
                .title(unitText)
                .threshold(border["Max"]!!.toDouble())
                .animated(true)
                .build()
            if (data!=null)
                gauge.value = data.toDouble()
            AnchorPane.setTopAnchor(gauge, 27.0)
            AnchorPane.setBottomAnchor(gauge, 19.0)
            AnchorPane.setRightAnchor(gauge, 5.0)
            AnchorPane.setLeftAnchor(gauge, 5.0)
            panel.children.add(gauge)

            val setting = ImageView(Image(FileInputStream(decoration.SETTING.style)))
            setting.fitWidth = decoration.SETTING.prefWidth
            setting.fitHeight = decoration.SETTING.prefHeight
            setting.layoutX = pref.INDICATOR.prefWidth - decoration.SETTING.layoutX
            setting.layoutY = pref.INDICATOR.prefHeight - decoration.SETTING.layoutY
            setting.onMouseClicked = EventHandler {
                val fxmlLoader = FXMLLoader(javaClass.getResource("settingIndicator.fxml"))
                val stage = Stage()
                stage.initModality(Modality.WINDOW_MODAL)
                stage.isResizable = false
                stage.scene = Scene(fxmlLoader.load())

                val controller: SettingIndicatorController = fxmlLoader.getController()
                controller.load(objectData)

                stage.showAndWait()
                reloadClick()
                if (controller.delete) {
                    indicatorPane.children.remove(panel)
                    queriesDB.deleteIndicator(pos[0], pos[1])
                } else {
                    val addList = controller.list

                    if (addList.size > 1) {
                        updateObject(pos[0], pos[1], addList[0], addList[1], "")
                        if (addList[0] != "") name.text = addList[0]
                        if (addList[1] != "") gauge.title = addList[1]
                    }
                }

            }
            panel.children.add(setting)

            indicatorPane.children.add(panel)

            queriesDB.insertIntoIndicator(
                Indicator(
                    null, objectData.getIdObject(), add_Object, pos[0], pos[1], nameText, unitText
                )
            )
        }
    }

    // Загружает индикатор в дашбоард из бд
    private fun uploadObjects(data: Map<String, String>) {
        val indicators = queriesDB.selectIndicators()
        if (indicators != null) {
            for (indicator in indicators) {
                if (indicator.getIdObject() == objectData.getIdObject() && !positionFree(
                        indicator.getLayoutX(), indicator.getLayoutY()
                    )
                ) {
                    var anchorX = 0.0
                    var anchorY = 0.0
                    var startX = indicator.getLayoutX()
                    var startY = indicator.getLayoutY()
                    val panel = AnchorPane()
                    panel.prefWidth = pref.INDICATOR.prefWidth
                    panel.prefHeight = pref.INDICATOR.prefHeight
                    panel.layoutX = startX
                    panel.layoutY = startY
                    panel.style = STYLE_PANEL

                    panel.setOnMousePressed { event ->
                        anchorX = event.sceneX
                        anchorY = event.sceneY
                    }

                    panel.setOnMouseDragged { event ->
                        panel.translateX = event.sceneX - anchorX
                        panel.translateY = event.sceneY - anchorY
                        panel.layoutX = event.sceneX - anchorX
                        panel.layoutY = event.sceneY - anchorY
                    }

                    panel.setOnMouseReleased {
                        val newPos = updatePositionIndicator(
                            startX,
                            startY,
                            panel.translateX,
                            panel.translateY
                        )
                        startX = newPos[0]
                        startY = newPos[1]
                    }

                    val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)
                    val str = addressAssemblyGET(
                        address, queriesDB.selectObject(
                            objectsTable.ID.name, objectData.getId().toString()
                        )!!.getIdModel()
                    )
                    val jsonM = Gson().fromJson(str, JsonObject::class.java)
                    val border = Parsing().readBorder(jsonM)

                    val gauge = GaugeBuilder.create()
                        .skinType(SkinType.SIMPLE)
                        .minValue(border["Min"]!!.toDouble() - 10)
                        .maxValue(border["Max"]!!.toDouble() + 10)
                        .sections(
                            Section(
                                border["Min"]!!.toDouble() - 10,
                                border["Min"]!!.toDouble(),
                                "0",
                                Color.web("#ff4e33")
                            ),
                            Section(border["Min"]!!.toDouble(), border["Mid"]!!.toDouble(), "1", Color.web("#6bdb6b")),
                            Section(border["Mid"]!!.toDouble(), border["Max"]!!.toDouble(), "2", Color.web("#ffba42")),
                            Section(
                                border["Max"]!!.toDouble(),
                                border["Max"]!!.toDouble() + 10,
                                "3",
                                Color.web("#ff4e33")
                            )
                        )
                        .sectionsVisible(true)
                        .title(indicator.getUnit())
                        .threshold(border["Max"]!!.toDouble())
                        .animated(true)
                        .build()
                    val valueData = data[indicator.getNameIndicator()]
                    if (valueData != null) {
                        gauge.value = valueData.toDouble()
                        if (valueData.toDouble() < border["Min"]!!.toDouble() || valueData.toDouble() > border["Max"]!!.toDouble()){
                            val alert = Alert(Alert.AlertType.WARNING)
                            alert.dialogPane.style = getAdditionalColor()
                            alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                            alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                            alert.title = "Внимание"
                            alert.headerText = "Тревога!!!"
                            alert.contentText = "У ${indicator.getName()} критические показания"
                            alert.showAndWait()
                        }
                    }
                    AnchorPane.setTopAnchor(gauge, 27.0)
                    AnchorPane.setBottomAnchor(gauge, 19.0)
                    AnchorPane.setRightAnchor(gauge, 5.0)
                    AnchorPane.setLeftAnchor(gauge, 5.0)
                    panel.children.add(gauge)

                    val name = Label(indicator.getName())
                    name.layoutX = decoration.NAME.layoutX
                    name.layoutY = decoration.NAME.layoutY
                    name.style = decoration.NAME.style
                    panel.children.add(name)

                    val setting = ImageView(Image(FileInputStream(decoration.SETTING.style)))
                    setting.fitWidth = decoration.SETTING.prefWidth
                    setting.fitHeight = decoration.SETTING.prefHeight
                    setting.layoutX = pref.INDICATOR.prefWidth - decoration.SETTING.layoutX
                    setting.layoutY = pref.INDICATOR.prefHeight - decoration.SETTING.layoutY
                    setting.onMouseClicked = EventHandler {
                        val fxmlLoader = FXMLLoader(javaClass.getResource("settingIndicator.fxml"))
                        val stage = Stage()
                        stage.initModality(Modality.WINDOW_MODAL)
                        stage.isResizable = false
                        stage.scene = Scene(fxmlLoader.load())

                        val controller: SettingIndicatorController = fxmlLoader.getController()
                        controller.load(objectData)

                        stage.showAndWait()
                        reloadClick()
                        if (controller.delete) {
                            indicatorPane.children.remove(panel)
                            queriesDB.deleteIndicator(indicator.getLayoutX(), indicator.getLayoutY())
                        } else {
                            val addList = controller.list

                            if (addList.size > 1) {
                                updateObject(indicator.getLayoutX(), indicator.getLayoutY(), addList[0], addList[1], "")
                                if (addList[0] != "") name.text = addList[0]
                                if (addList[1] != "") gauge.title = addList[1]
                            }
                        }

                    }
                    panel.children.add(setting)
                    indicatorPane.children.add(panel)
                }
            }
        }

        val charts = queriesDB.selectCharts()
        if (charts != null) {
            for (chart in charts) {
                if (chart.getIdObject() == objectData.getIdObject() && !positionFree(
                        chart.getLayoutX(), chart.getLayoutY()
                    )
                ) {
                    var anchorX = 0.0
                    var anchorY = 0.0
                    var startX = chart.getLayoutX()
                    var startY = chart.getLayoutY()
                    val panel = AnchorPane()
                    panel.prefWidth = pref.CHART.prefWidth
                    panel.prefHeight = pref.CHART.prefHeight
                    panel.layoutX = chart.getLayoutX()
                    panel.layoutY = chart.getLayoutY()
                    panel.style = STYLE_PANEL

                    panel.setOnMousePressed { event ->
                        anchorX = event.sceneX
                        anchorY = event.sceneY
                    }

                    panel.setOnMouseDragged { event ->
                        panel.translateX = event.sceneX - anchorX
                        panel.translateY = event.sceneY - anchorY
                        panel.layoutX = event.sceneX - anchorX
                        panel.layoutY = event.sceneY - anchorY
                    }

                    panel.setOnMouseReleased {
                        val newPos = updatePositionChart(
                            startX,
                            startY,
                            panel.translateX,
                            panel.translateY
                        )
                        startX = newPos[0]
                        startY = newPos[1]
                    }


                    val name = Label(chart.getName() + "," + chart.getType())
                    name.layoutX = decoration.NAME.layoutX
                    name.layoutY = decoration.NAME.layoutY
                    name.style = decoration.NAME.style
                    panel.children.add(name)

                    val tp = chartData(chart.getNameChart(), objectData.getIdObject())
                    var datas = Series<String, Number>()
                    var df1: DateFormat = SimpleDateFormat("dd MMM yyy")
                    var day = df1.format(Date(tp[tp.size - 1][0].toLong()))
                    datas.name = day
                    for (t in tp) {
                        val time = Date(t[0].toLong())
                        if (df1.format(time) == day) {
                            val df2 = SimpleDateFormat("HH:mm")
                            datas.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
                        }
                    }

                    val xAxis = CategoryAxis()
                    val yAxis = NumberAxis()

                    val areaChart = when (chart.getType()) {
                        "AreaChart" -> {
                            AreaChart(xAxis, yAxis)
                        }
                        "BarChart" -> {
                            BarChart(xAxis, yAxis)
                        }
                        "LineChart" -> {
                            LineChart(xAxis, yAxis)
                        }
                        "ScatterChart" -> {
                            ScatterChart(xAxis, yAxis)
                        }
                        else -> {
                            AreaChart(xAxis, yAxis)
                        }
                    }

                    areaChart.layoutX = decoration.CHARTS.layoutX
                    areaChart.layoutY = decoration.CHARTS.layoutY
                    areaChart.prefWidth = decoration.CHARTS.prefWidth
                    areaChart.prefHeight = decoration.CHARTS.prefHeight
                    areaChart.data.add(datas)
                    panel.children.add(areaChart)

                    val setting = ImageView(Image(FileInputStream(decoration.SETTING.style)))
                    setting.fitWidth = decoration.SETTING.prefWidth
                    setting.fitHeight = decoration.SETTING.prefHeight
                    setting.layoutX = pref.CHART.prefWidth - decoration.SETTING.layoutX
                    setting.layoutY = pref.CHART.prefHeight - decoration.SETTING.layoutY
                    setting.onMouseClicked = EventHandler {
                        val fxmlLoader = FXMLLoader(javaClass.getResource("settingChart.fxml"))
                        val stage = Stage()
                        stage.initModality(Modality.WINDOW_MODAL)
                        stage.isResizable = false
                        stage.scene = Scene(fxmlLoader.load())

                        val controller: SettingChartController = fxmlLoader.getController()
                        controller.load(areaChart.layoutX, areaChart.layoutY)

                        stage.showAndWait()
                        if (controller.delete) {
                            chartPane.children.remove(panel)
                            queriesDB.deleteChart(chart.getLayoutX(), chart.getLayoutY())
                        } else {
                            val addList = controller.list

                            if (addList.size > 1) {
                                updateObject(chart.getLayoutX(), chart.getLayoutY(), addList[0], addList[1], addList[2])
                                if (addList[0] != "") name.text = addList[0] + name.text.substring(
                                    name.text.indexOf(",", 0), name.text.length
                                )
                                if (addList[1] != "") name.text =
                                    name.text.substring(0, name.text.indexOf(",", 0) + 1) + addList[1]
                                if (addList[2] != "") {
                                    datas = Series<String, Number>()
                                    df1 = SimpleDateFormat("dd MMM yyy")
                                    day = df1.format(Date(addList[2].toLong()))

                                    datas.name = day
                                    areaChart.data.remove(0, areaChart.data.size)
                                    areaChart.data.add(datas)
                                    for (t in tp) {
                                        val time = Date(t[0].toLong())
                                        if (df1.format(time) == day) {
                                            val df2 = SimpleDateFormat("HH:mm")
                                            datas.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
                                        }
                                    }
                                }

                            }
                        }
                    }
                    panel.children.add(setting)
                    chartPane.children.add(panel)
                }
            }
        }
    }


    private fun updateObject(layoutX: Double, layoutY: Double, nameText: String, unitText: String, type: String) {
        if (indicatorPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateIndicator(layoutX, layoutY, indicatorsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateIndicator(layoutX, layoutY, indicatorsTable.UNIT.name, unitText)
            }
        }
        if (chartPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateChart(layoutX, layoutY, chartsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateChart(layoutX, layoutY, chartsTable.UNIT.name, unitText)
            }

            if (type != "") {
                queriesDB.updateChart(layoutX, layoutY, chartsTable.TYPE.name, type)
            }
        }
        reloadClick()
    }

    //выгружает названия данных у объекта
    private fun valuesObject(name: String, listObjects: List<Object>, addressAll: String): List<String> {
        val values = mutableListOf<String>()
        for (obj in listObjects) if (name == obj.getNameObject()) {
            val str = addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = Parsing().read(json, STATE)
            if (state != null) {
                val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS, requestCharacters.SLESH.code)

                val strM = request.addressAssemblyGET(address, obj.getIdModel())
                val jsonM = Gson().fromJson(strM, JsonObject::class.java)
                val modelState = Parsing().readModelParams(jsonM)

                val modelST = mutableListOf<String>()
                for (states in modelState)
                    modelST.add(states.key)

                json = Gson().fromJson(state, JsonObject::class.java)
                for (value in modelST) {
                    val pars = Parsing().read(json, value)?.asString
                    if (pars != null) values.add(value)
                }
            }
        }
        return values
    }

    //выгружает все показатели для всех данных в виде мапы
    private fun objectsData(
        name: String, listObjects: List<Object>, addressAll: String, dataObject: List<String>
    ): Map<String, String> {
        val data: MutableMap<String, String> = mutableMapOf()
        for (obj in listObjects) if (name == obj.getNameObject()) {
            val str = addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = Parsing().read(json, STATE)
            if (state != null) {
                json = Gson().fromJson(state, JsonObject::class.java)
                for (dao in dataObject) {
                    val pars = Parsing().read(json, dao)?.asString
                    if (pars != null) data[dao] = pars
                }
            }
        }
        return data
    }

    //выгружает показатель для определенного данного
    private fun objectData(name: String, objects: String, addressAll: String): String? {
        val str = addressAssemblyGET(addressAll, objects)
        var json = Gson().fromJson(str, JsonObject::class.java)

        val state = Parsing().read(json, STATE)
        if (state != null) {
            json = Gson().fromJson(state, JsonObject::class.java)
            val pars = Parsing().read(json, name)?.asString
            if (pars != null) return pars

        }
        return null
    }

    private fun chartData(name: String, idObjects: String): List<List<Number>> {
        var address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS, requestCharacters.SLESH.code)
        address = request.addressGeneration(address, idObjects, requestCharacters.SLESH.code)

        val str = addressAssemblyGET(address, "packets")

        val json = Gson().fromJson(str, JsonArray::class.java)
        val topic = "$TOPIC$name"

        val data = Parsing().readForChart(json, topic)
        return data
    }

    //addBlock
    @FXML
    private fun clickIndicators() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("addBlock.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.title = "add"
        stage.scene = Scene(fxmlLoader.load())
        val controller: AddWidgetController = fxmlLoader.getController()
        if (indicatorPane.isVisible)
            controller.load(objectData, true)
        if (chartPane.isVisible)
            controller.load(objectData, false)

        stage.showAndWait()
        val addList = controller.list

        if (addList.size > 1) addObjects(addList[0], addList[1], addList[2], addList[3])
    }


    private fun addressAssemblyGET(address: String, value: String): String? {
        return request.GETRequest(
            request.addressGeneration(address, value, requestCharacters.SLESH.code))
    }

    private fun getPositionIndicator(): List<Double> {
        var layoutX = 0.0
        var layoutY = 0.0
        for (ch in indicatorPane.children) {
            if (ch.layoutX + ch.prefWidth(0.0) < 1393.0 && ch.prefWidth(0.0) == 200.0) {
                layoutX = ch.layoutX + ch.prefWidth(0.0) + 5.0
            } else if (ch.prefWidth(0.0) == 200.0) {
                layoutX = 0.0
                layoutY = ch.layoutY + ch.prefHeight(0.0) + 5.0
            }
        }
        return mutableListOf(layoutX, layoutY)
    }


    private fun getPositionChart(): List<Double> {
        var layoutX = 0.0
        var layoutY = 0.0
        for (ch in chartPane.children) {
            if (ch.layoutX + ch.prefWidth(0.0) < 1240.0 && ch.prefWidth(0.0) == 305.0) {
                layoutX = ch.layoutX + ch.prefWidth(0.0) + 5.0
            } else if (ch.prefWidth(0.0) == 305.0) {
                layoutX = 0.0
                layoutY = ch.layoutY + ch.prefHeight(0.0) + 5.0
            }
        }
        return mutableListOf(layoutX, layoutY)
    }

    private fun positionFree(layoutX: Double, layoutY: Double): Boolean {
        if (indicatorPane.isVisible) {
            for (ch in indicatorPane.children) {
                if (ch.layoutX == layoutX && ch.layoutY == layoutY) {
                    return true
                }
            }
        }
        if (chartPane.isVisible) {
            for (ch in chartPane.children) {
                if (ch.layoutX == layoutX && ch.layoutY == layoutY) {
                    return true
                }
            }
        }
        return false
    }

    @FXML
    private fun reloadClick() {
        if (indicatorPane.isVisible) {
            val length = indicatorPane.children.size
            if (length > 1) {
                indicatorPane.children.remove(0, length)
            }
        } else if (chartPane.isVisible) {
            val length = chartPane.children.size
            if (length > 1) {
                chartPane.children.remove(0, length)
            }
        }
        val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS, requestCharacters.SLESH.code)
        val values = valuesObject(objectData.getNameObject(), listObjects, address)
        val data = objectsData(objectData.getNameObject(), listObjects, address, values)
        uploadObjects(data)
    }

    @FXML
    private fun accountClick() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("account.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())
        val controller: AccountController = fxmlLoader.getController()
        controller.load(user)
        stage.showAndWait()

        if (controller.save){
            queriesDB.updateUser(ID_USER, usersTable.TOKEN.name, controller.user.getToken())
            queriesDB.updateUser(ID_USER, usersTable.ADDRESS.name, controller.user.getAddress())
        }

        if (controller.exit) {
            queriesDB.updateUser(ID_USER, usersTable.CASTLE.name, false.toString())
            var newStage: Stage = username.scene.window as Stage
            newStage.close()
            val newFxmlLoader = FXMLLoader(javaClass.getResource("loginWindow.fxml"))
            newStage = Stage()
            stage.isResizable = false
            newStage.initModality(Modality.APPLICATION_MODAL)
            newStage.title = "Window"
            newStage.scene = Scene(newFxmlLoader.load())
            newStage.show()
        }
    }

    @FXML
    private fun settingClick() {
        val oldTheme = THEME
        val fxmlLoader = FXMLLoader(javaClass.getResource("setting.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())
        stage.showAndWait()
        if (THEME != oldTheme) {
            queriesDB.updateUser(ID_USER, usersTable.THEME.name, THEME)
            updateTheme()
        }

    }

    private fun updateTheme() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        indicatorPane.style = getMainColor()
        chartPane.style = getMainColor()
        dataList.style = getAdditionalColor()
    }

    @FXML
    private fun indicatorClick() {
        indicatorPane.isVisible = true
        chartPane.isVisible = false
        reloadClick()
    }

    @FXML
    private fun chartClick() {
        chartPane.isVisible = true
        indicatorPane.isVisible = false
        reloadClick()
    }

    private fun updatePositionIndicator(layoutX: Double, layoutY: Double, translateX: Double, translateY: Double): List<Double> {
        var posX = 0.0
        var posY = 0.0
        while (translateX - posX > 200) {
            posX += 205.0
        }
        if (translateX - posX > 102) {
            posX += 205.0
        }
        while (translateY - posY > 200) {
            posY += 205.0
        }
        if (translateY - posY > 102) {
            posY += 205.0
        }

        if(positionFree(posX, posY)){
            val indicator = queriesDB.selectIndicator(layoutX, layoutY)
            val anotherIndicator = queriesDB.selectIndicator(posX, posY)
            if (anotherIndicator != null && indicator != null) {
                queriesDB.updateIndicatorId(indicator.getId(), indicatorsTable.LAYOUT_X.name, posX.toString())
                queriesDB.updateIndicatorId(indicator.getId(), indicatorsTable.LAYOUT_Y.name, posY.toString())
                queriesDB.updateIndicatorId(anotherIndicator.getId(), indicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateIndicatorId(anotherIndicator.getId(), indicatorsTable.LAYOUT_Y.name, layoutY.toString())
            }
            else{
                queriesDB.updateIndicator(layoutX, layoutY, indicatorsTable.LAYOUT_X.name, posX.toString())
                queriesDB.updateIndicator(posX, layoutY, indicatorsTable.LAYOUT_Y.name, posY.toString())
            }
        }
        else{
            queriesDB.updateIndicator(layoutX, layoutY, indicatorsTable.LAYOUT_X.name, posX.toString())
            queriesDB.updateIndicator(posX, layoutY, indicatorsTable.LAYOUT_Y.name, posY.toString())
        }
        reloadClick()
        return mutableListOf(posX, posY)
    }

    private fun updatePositionChart(layoutX: Double, layoutY: Double, translateX: Double, translateY: Double): List<Double> {
        var posX = 0.0
        var posY = 0.0
        while (translateX - posX > 305) {
            posX += 310.0
        }
        if (translateX - posX > 155) {
            posX += 310.0
        }
        while (translateY - posY > 305) {
            posY += 310.0
        }
        if (translateY - posY > 155) {
            posY += 310.0
        }

        if(positionFree(posX, posY)){
            val chart = queriesDB.selectChart(layoutX, layoutY)
            val anotherChart = queriesDB.selectChart(posX, posY)
            if (anotherChart != null && chart != null) {
                queriesDB.updateChartId(chart.getId(), indicatorsTable.LAYOUT_X.name, posX.toString())
                queriesDB.updateChartId(chart.getId(), indicatorsTable.LAYOUT_Y.name, posY.toString())
                queriesDB.updateChartId(anotherChart.getId(), indicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateChartId(anotherChart.getId(), indicatorsTable.LAYOUT_Y.name, layoutY.toString())
            }
            else{
                queriesDB.updateChart(layoutX, layoutY, indicatorsTable.LAYOUT_X.name, posX.toString())
                queriesDB.updateChart(posX, layoutY, indicatorsTable.LAYOUT_Y.name, posY.toString())
            }
        }
        else{
            queriesDB.updateChart(layoutX, layoutY, indicatorsTable.LAYOUT_X.name, posX.toString())
            queriesDB.updateChart(posX, layoutY, indicatorsTable.LAYOUT_Y.name, posY.toString())
        }
        reloadClick()
        return mutableListOf(posX, posY)
    }
}