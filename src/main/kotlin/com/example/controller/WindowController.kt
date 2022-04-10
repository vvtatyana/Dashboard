package com.example.controller

import STYLE_PANEL
import THEME
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.building.*
import com.example.building.Chart
import com.example.database.*
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jfoenix.controls.JFXListView
import decoration
import eu.hansolo.medusa.Gauge
import eu.hansolo.medusa.Gauge.SkinType
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import getAdditionalColor
import getMainColor
import javafx.animation.TranslateTransition
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.chart.*
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane.setMargin
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import pref
import typeIndicator
import java.io.FileInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

/**
 * Класс создает основное окно приложения
 */
class WindowController {

    @FXML
    private lateinit var mainPane: AnchorPane

    @FXML
    private lateinit var headerPane: AnchorPane

    @FXML
    private lateinit var accountImageView: ImageView

    @FXML
    private lateinit var reloadImageView: ImageView

    @FXML
    private lateinit var movingImageView: ImageView

    @FXML
    private lateinit var panelSlide: AnchorPane

    @FXML
    private lateinit var dataImageView: ImageView

    @FXML
    private lateinit var indicatorImageView: ImageView

    @FXML
    private lateinit var chartImageView: ImageView

    @FXML
    private lateinit var setting: ImageView

    @FXML
    private lateinit var username: Label

    @FXML
    private lateinit var addClick: ImageView

    @FXML
    private lateinit var indicatorPane: AnchorPane

    @FXML
    private lateinit var chartPane: AnchorPane

    @FXML
    private lateinit var dataList: JFXListView<String>

    private val request = RequestGeneration()
    private lateinit var objectData: Object
    private lateinit var user: User
    private var listObjects = mutableListOf<Object>()

    private lateinit var queriesDB: QueriesDB
    private val database = Database()

    private var dragFlag: Boolean = false
    private var dataFlag: Boolean = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        Tooltip.install(addClick, Tooltip("Добавить виджет"))
        Tooltip.install(setting, Tooltip("Настройки приложения"))
        Tooltip.install(chartImageView, Tooltip("Показать изменение"))
        Tooltip.install(indicatorImageView, Tooltip("Показать индикаторы"))
        Tooltip.install(dataImageView, Tooltip("Список объектов"))
        Tooltip.install(movingImageView, Tooltip("Перемещение виджетов"))
        Tooltip.install(accountImageView, Tooltip("Аккаунт"))
        Tooltip.install(reloadImageView, Tooltip("Обновить данные"))

        updateTheme()
        queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        indicatorImageView.isVisible = false
        chartImageView.isVisible = false
        chartPane.isVisible = false
        movingImageView.isVisible = false
        addClick.isVisible = false
        reloadImageView.isVisible = false

        val userBD = queriesDB.selectUser(usersTable.ID.name, ID_USER.toString())
        if (userBD != null) {
            user = userBD
            username.text = user.getUsername()
        }
        objects()
    }

    /**
     * Обновляет тему окна
     */
    private fun updateTheme() {
        mainPane.style = getMainColor()
        headerPane.style = getAdditionalColor()
        indicatorPane.style = getMainColor()
        chartPane.style = getMainColor()
        dataList.style = getAdditionalColor()
    }

    /**
     * Показывает/убирает список устройств
     */
    @FXML
    private fun dataImageClick() {
        val slider = TranslateTransition()
        slider.duration = Duration.seconds(0.4)
        slider.node = panelSlide
        if (!dataFlag) {
            slider.toX = 0.0
            slider.play()
            panelSlide.translateX = -195.0
            dataFlag = true
            setMargin(panelSlide, Insets(0.0, 0.0, 0.0, 0.0))
        } else {
            slider.toX = -195.0
            slider.play()
            panelSlide.translateX = 0.0
            dataFlag = false
            setMargin(panelSlide, Insets(0.0, 0.0, 0.0, -195.0))
        }
    }

    /**
     * Считывает все объекты у пользоввателя и загружает в бд если их там нет
     */
    private fun objects() {
        val strOb = RequestGeneration().addressAssemblyGET(DEFAULT_ADDRESS, OBJECTS)
        val jsonOb = Gson().fromJson(strOb, JsonArray::class.java)
        listObjects = ProcessingJSON().readAllObjects(jsonOb) as MutableList<Object>

        for (obj in listObjects) {
            val objDB = queriesDB.selectObject(objectsTable.ID_OBJECT.name, obj.getIdObject())
            if (objDB == null) {
                queriesDB.insertIntoObject(obj)
            }
        }
        showObject()
    }

    /**
     * Выводит список устройств
     */
    private fun showObject() {
        val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
        val nameListObjects = ArrayList<String>()
        for (obj in listObjects) {
            nameListObjects.add(obj.getNameObject())
            val namesObjects = FXCollections.observableArrayList(nameListObjects)
            dataList.items = namesObjects
            dataList.isVisible = true

            dataList.onMouseClicked = EventHandler {

                indicatorImageView.isVisible = true
                chartImageView.isVisible = true
                movingImageView.isVisible = true
                reloadImageView.isVisible = true
                addClick.isVisible = true
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

    /**
     * Перемещение виджета мышью
     * @panel - пано, которое перемещают
     * @layoutX - позиция по x
     * @layoutY - позиция по y
     */
    private fun mouseDraggedPanel(panel: AnchorPane, layoutX: Double, layoutY: Double) {
        var anchorX = 0.0
        var anchorY = 0.0

        panel.setOnMousePressed { event ->
            anchorX = panel.layoutX - event.sceneX
            anchorY = panel.layoutY - event.sceneY
        }

        panel.setOnMouseDragged { event ->
            if (dragFlag) {
                panel.layoutX = event.sceneX + anchorX
                panel.layoutY = event.sceneY + anchorY
            }
        }

        if (panel.prefHeight == pref.CHART.prefHeight) {
            panel.setOnMouseReleased {
                if (dragFlag) {
                    updatePositionChart(
                        layoutX,
                        layoutY,
                        panel.layoutX,
                        panel.layoutY
                    )
                }
            }
        } else {
            panel.setOnMouseReleased {
                if (dragFlag) {
                    updatePositionIndicator(
                        layoutX,
                        layoutY,
                        panel.layoutX,
                        panel.layoutY
                    )
                }
            }
        }

    }

    /**
     * Создает пано для виджета
     * @layoutX - позиция по x
     * @layoutY - позиция по y
     * @prefWidth - высота пано
     * @prefHeight - ширина пано
     */
    private fun createPanel(layoutX: Double, layoutY: Double, prefWidth: Double, prefHeight: Double): AnchorPane {
        val panel = AnchorPane()
        panel.style = STYLE_PANEL
        panel.layoutX = layoutX
        panel.layoutY = layoutY
        panel.prefWidth = prefWidth
        panel.prefHeight = prefHeight

        mouseDraggedPanel(panel, layoutX, layoutY)
        return panel
    }

    /**
     * Создает имя виджета
     * @name - имя виджета
     */
    private fun createName(name: String): Label {
        val nameLabel = Label(name)
        nameLabel.layoutX = decoration.NAME.layoutX
        nameLabel.layoutY = decoration.NAME.layoutY
        nameLabel.style = decoration.NAME.style
        return nameLabel
    }

    /**
     * Создает индикатор
     * @data - показания с устройства
     * @unitText - единицы измерения
     */
    private fun createGauge(data: String?, unitText: String, name: String): Gauge {
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val str = RequestGeneration().addressAssemblyGET(
            address, queriesDB.selectObject(
                objectsTable.ID.name, objectData.getId().toString()
            )!!.getIdModel()
        )
        val jsonM = Gson().fromJson(str, JsonObject::class.java)
        val border = ProcessingJSON().readBorder(jsonM, name).toMutableMap()
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
        if (data != null)
            gauge.value = data.toDouble()
        AnchorPane.setTopAnchor(gauge, 27.0)
        AnchorPane.setBottomAnchor(gauge, 19.0)
        AnchorPane.setRightAnchor(gauge, 5.0)
        AnchorPane.setLeftAnchor(gauge, 5.0)
        return gauge
    }

    private fun createString(data: String?): Label {
        val stringLabel = Label(data)
        stringLabel.alignment = Pos.CENTER
        stringLabel.style = decoration.NAME.style
        AnchorPane.setTopAnchor(stringLabel, 25.0)
        AnchorPane.setBottomAnchor(stringLabel, 25.0)
        AnchorPane.setRightAnchor(stringLabel, 15.0)
        AnchorPane.setLeftAnchor(stringLabel, 15.0)
        return stringLabel
    }

    private fun createCircleBig(data: String?): Circle {
        val circleBig = Circle()
        if (data.toBoolean()) {
            circleBig.fill = Paint.valueOf("#6bdb6b")
        } else {
            circleBig.fill = Paint.valueOf("#ff4e33")
        }
        circleBig.radius = 75.0
        circleBig.stroke = Paint.valueOf("WHITE")
        circleBig.strokeWidth = 3.0

        AnchorPane.setTopAnchor(circleBig, 24.0)
        AnchorPane.setBottomAnchor(circleBig, 19.0)
        AnchorPane.setRightAnchor(circleBig, 25.0)
        AnchorPane.setLeftAnchor(circleBig, 25.0)
        return circleBig
    }

    private fun createCircleLittle(): Circle {
        val circleLittle = Circle()
        circleLittle.radius = 34.0
        circleLittle.stroke = Paint.valueOf("WHITE")
        circleLittle.strokeWidth = 3.0
        circleLittle.fill = Paint.valueOf("#636161")

        AnchorPane.setTopAnchor(circleLittle, 65.0)
        AnchorPane.setBottomAnchor(circleLittle, 62.0)
        AnchorPane.setRightAnchor(circleLittle, 66.0)
        AnchorPane.setLeftAnchor(circleLittle, 66.0)
        return circleLittle
    }

    private fun createCircleText(data: String?): Label {
        val circleLabel = Label()
        if (data.toBoolean()) {
            circleLabel.text = "Да"
        } else {
            circleLabel.text = "Нет"
        }
        circleLabel.alignment = Pos.CENTER
        circleLabel.style = "-fx-text-fill: white; -fx-font-size: 20px;-fx-font-family: \"Segoe UI Semibold\";"
        AnchorPane.setTopAnchor(circleLabel, 70.0)
        AnchorPane.setBottomAnchor(circleLabel, 70.0)
        AnchorPane.setRightAnchor(circleLabel, 70.0)
        AnchorPane.setLeftAnchor(circleLabel, 70.0)
        return circleLabel
    }

    /**
     * Создает настройки для виджета
     * @prefWidth - высота пано
     * @prefHeight - ширина пано
     */
    private fun createSetting(prefWidth: Double, prefHeight: Double): ImageView {
        val setting = ImageView(Image(FileInputStream(decoration.SETTING.style)))
        setting.fitWidth = decoration.SETTING.prefWidth
        setting.fitHeight = decoration.SETTING.prefHeight
        setting.layoutX = prefWidth - decoration.SETTING.layoutX
        setting.layoutY = prefHeight - decoration.SETTING.layoutY
        return setting
    }

    /**
     * Реакция на нажатие на настройки у индикатора
     * @panel - пано
     * @pos - координаты пано
     * @name - имя пано
     * @gauge - индикатор
     */
    @FXML
    private fun settingIndicatorClick(panel: AnchorPane, pos: List<Double>, name: Label, nameIndicator: String) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("settingIndicator.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())

        val controller: SettingIndicatorController = fxmlLoader.getController()
        controller.load(objectData.getIdModel(), nameIndicator)

        stage.showAndWait()
        reloadClick()
        if (controller.delete) {
            indicatorPane.children.remove(panel)
            queriesDB.deleteIndicator(pos[0], pos[1])
        } else {
            val dataWidget = controller.dataWidget
            if (dataWidget != null) {
                updateObject(pos[0], pos[1], dataWidget.getName(), dataWidget.getUnit(), "")
                if (dataWidget.getName() != "") name.text = dataWidget.getName()
            }
            //if (addList[1] != "") gauge.title = addList[1]
        }
    }

    /**
     * Реакция на нажатие на настройки у графика
     * @panel - пано
     * @areaChart - график
     * @data - показатели
     * @layoutX - координаты x пано
     * @layoutY - координаты y пано
     * @name - имя пано
     */
    @FXML
    private fun settingChartClick(
        panel: AnchorPane,
        areaChart: XYChart<String, Number>,
        data: List<List<Number>>,
        //pos: List<Double>,
        layoutX: Double,
        layoutY: Double,
        name: Label
    ) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("settingChart.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())

        val controller: SettingChartController = fxmlLoader.getController()
        controller.load(layoutX, layoutY)

        stage.showAndWait()
        if (controller.delete) {
            chartPane.children.remove(panel)
            queriesDB.deleteChart(layoutX, layoutY)
        } else {
            val addWidget = controller.dataWidget

            updateObject(layoutX, layoutY, addWidget.getName(), addWidget.getUnit(), addWidget.getType())
            if (addWidget.getName() != "") name.text = addWidget.getName() + name.text.substring(
                name.text.indexOf(",", 0), name.text.length
            )
            if (addWidget.getUnit() != "") name.text =
                name.text.substring(0, name.text.indexOf(",", 0) + 1) + addWidget.getUnit()

            if (addWidget.getDate() != "") {
                val datas = Series<String, Number>()
                val simpleDateFormat = SimpleDateFormat("dd MMM yyy")
                val simpleDate = simpleDateFormat.format(Date(addWidget.getDate().toLong()))

                datas.name = simpleDate
                areaChart.data.remove(0, areaChart.data.size)
                areaChart.data.add(datas)
                for (t in data) {
                    val time = Date(t[0].toLong())
                    if (simpleDateFormat.format(time) == simpleDate) {
                        val simpleDateFormatOther = SimpleDateFormat("HH:mm")

                        val thisTime = LocalTime.parse(simpleDateFormatOther.format(time))
                        if (addWidget.getFrom() != "") {
                            val fromTime = LocalTime.parse(addWidget.getFrom())
                            if (thisTime.isAfter(fromTime)) {
                                if (addWidget.getTo() != "") {
                                    val toTime = LocalTime.parse(addWidget.getTo())

                                    if (thisTime.isBefore(toTime)) {
                                        datas.data.add(
                                            XYChart.Data(
                                                simpleDateFormatOther.format(time),
                                                t[1].toDouble()
                                            )
                                        )
                                    }

                                } else {
                                    datas.data.add(
                                        XYChart.Data(
                                            simpleDateFormatOther.format(time),
                                            t[1].toDouble()
                                        )
                                    )
                                }
                            }
                        } else {
                            datas.data.add(
                                XYChart.Data(
                                    simpleDateFormatOther.format(time),
                                    t[1].toDouble()
                                )
                            )
                        }
                    }
                }
            }
        }

    }

    /**
     * @data - показатели
     */
    private fun dataSeries(data: List<List<Number>>): Series<String, Number> {
        val dataChart = Series<String, Number>()
        val df1: DateFormat = SimpleDateFormat("dd MMM yyy")
        val day = ""
        if (data.isNotEmpty()) {
            df1.format(Date(data[data.size - 1][0].toLong()))
            dataChart.name = day
        }
        for (t in data) {
            val time = Date(t[0].toLong())
            if (df1.format(time) == day) {
                val df2 = SimpleDateFormat("HH:mm")
                dataChart.data.add(XYChart.Data(df2.format(time), t[1].toDouble()))
            }
        }
        return dataChart
    }

    /**
     * Создает виджет график
     * @chartType - тип графика
     * @data - показатели
     */
    private fun createChart(chartType: String, data: List<List<Number>>): XYChart<String, Number> {
        val dataChart = dataSeries(data)
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
        return areaChart
    }

    /**
     * Создает виджет
     * @addObject - тип виджета
     * @nameText - название
     * @unitText - единицы измерения
     * @typeWidget - тип графика
     */
    private fun addObjects(addWidget: Widget) {
        if (indicatorPane.isVisible) {
            val pos = getPositionIndicator()
            val panel = createPanel(pos[0], pos[1], pref.INDICATOR.prefWidth, pref.INDICATOR.prefHeight)
            val name = createName(addWidget.getName())
            panel.children.add(name)

            val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
            val obj = queriesDB.selectObject(objectsTable.ID.name, objectData.getId().toString())
            val data = objectData(addWidget.getWidget(), obj!!.getIdObject(), address)

            val information = when (addWidget.getWidget()) {
                typeIndicator.NUMBER.type -> {
                    createGauge(data, addWidget.getUnit(), addWidget.getWidget())
                }
                typeIndicator.STRING.type -> {
                    createString(data)
                }
                typeIndicator.BOOLEAN.type -> {
                    createCircleBig(data)
                }
                else -> {
                    null
                }
            }
            panel.children.add(information)

            if (addWidget.getType() == typeIndicator.BOOLEAN.type) {
                panel.children.add(createCircleLittle())
                panel.children.add(createCircleText(data))
            }

            val setting = createSetting(pref.INDICATOR.prefWidth, pref.INDICATOR.prefHeight)

            setting.onMouseClicked = EventHandler {
                settingIndicatorClick(panel, pos, name, "")

            }
            panel.children.add(setting)

            indicatorPane.children.add(panel)

            queriesDB.insertIntoIndicator(
                Indicator(
                    null,
                    objectData.getIdObject(),
                    addWidget.getWidget(),
                    pos[0],
                    pos[1],
                    addWidget.getName(),
                    addWidget.getUnit(),
                    addWidget.getType()
                )
            )
        }

        if (chartPane.isVisible) {
            val pos = getPositionChart()

            val panel = createPanel(pos[0], pos[1], pref.CHART.prefWidth, pref.CHART.prefHeight)
            val name = createName("${addWidget.getName()}, ${addWidget.getUnit()}")
            panel.children.add(name)

            val tp = chartData(addWidget.getWidget(), objectData.getIdObject())

            val areaChart = createChart(addWidget.getType(), tp)
            panel.children.add(areaChart)

            val setting = createSetting(pref.CHART.prefWidth, pref.CHART.prefHeight)
            setting.onMouseClicked = EventHandler {
                settingChartClick(panel, areaChart, tp, /*pos,*/ areaChart.layoutX, areaChart.layoutY, name)
            }
            panel.children.add(setting)
            chartPane.children.add(panel)

            queriesDB.insertIntoChart(
                Chart(
                    null,
                    objectData.getIdObject(),
                    addWidget.getWidget(),
                    pos[0],
                    pos[1],
                    addWidget.getName(),
                    addWidget.getUnit(),
                    addWidget.getType()
                )
            )

        }
    }

    /**
     * Загружает виджеты из бд
     * @data - данные
     */
    private fun uploadObjects(data: Map<String, String>) {
        val indicators = queriesDB.selectIndicators()
        if (indicators != null) {
            for (indicator in indicators) {
                if (indicator.getIdObject() == objectData.getIdObject() && !positionFree(
                        indicator.getLayoutX(), indicator.getLayoutY()
                    )
                ) {
                    val panel = createPanel(
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        pref.INDICATOR.prefWidth,
                        pref.INDICATOR.prefHeight
                    )
                    val information = when (indicator.getType()) {
                        typeIndicator.NUMBER.type -> {
                            createGauge(
                                data[indicator.getNameIndicator()],
                                indicator.getUnit(),
                                indicator.getNameIndicator()
                            )
                        }
                        typeIndicator.STRING.type -> {
                            createString(data[indicator.getNameIndicator()])
                        }
                        typeIndicator.BOOLEAN.type -> {
                            createCircleBig(data[indicator.getNameIndicator()])
                        }
                        else -> {
                            null
                        }
                    }
                    panel.children.add(information)

                    if (indicator.getType() == typeIndicator.BOOLEAN.type) {
                        panel.children.add(createCircleLittle())
                        panel.children.add(createCircleText(data[indicator.getNameIndicator()]))
                    }

                    val name = createName(indicator.getName())
                    panel.children.add(name)

                    val setting = createSetting(pref.INDICATOR.prefWidth, pref.INDICATOR.prefHeight)
                    setting.onMouseClicked = EventHandler {
                        settingIndicatorClick(
                            panel,
                            mutableListOf(indicator.getLayoutX(), indicator.getLayoutY()),
                            name, indicator.getNameIndicator()
                        )
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
                    val panel =
                        createPanel(chart.getLayoutX(), chart.getLayoutY(), pref.CHART.prefWidth, pref.CHART.prefHeight)
                    val name = createName(chart.getName() + "," + chart.getUnit())
                    panel.children.add(name)

                    val tp = chartData(chart.getNameChart(), objectData.getIdObject())
                    val areaChart = createChart(chart.getType(), tp)
                    panel.children.add(areaChart)

                    val setting = createSetting(pref.CHART.prefWidth, pref.CHART.prefHeight)
                    setting.onMouseClicked = EventHandler {
                        settingChartClick(
                            panel,
                            areaChart, tp,
                            //mutableListOf(chart.getLayoutX(), chart.getLayoutY()),
                            areaChart.layoutX,
                            areaChart.layoutY,
                            name
                        )
                    }
                    panel.children.add(setting)
                    chartPane.children.add(panel)

                }
            }
//        val thread = WindowController()
//        thread.start()
        }
    }

    /**
     * Изменияе виджет
     * @layoutX - позиция x
     * @layoutY - позиция y
     * @nameText - название
     * @unitText - единицы измерения
     * @type - тип диаграммы
     */
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

    /**
     * Выгружает названия данных у объекта
     * @name - имя объекта
     * @listObjects - список всех объектов
     * @addressAll - адрес
     */
    private fun valuesObject(name: String, listObjects: List<Object>, addressAll: String): List<String> {
        val values = mutableListOf<String>()
        for (obj in listObjects) if (name == obj.getNameObject()) {
            val str = RequestGeneration().addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = ProcessingJSON().read(json, STATE)
            if (state != null) {
                val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)

                val strM = request.addressAssemblyGET(address, obj.getIdModel())
                val jsonM = Gson().fromJson(strM, JsonObject::class.java)
                val modelState = ProcessingJSON().readModelState(jsonM)
                json = Gson().fromJson(state, JsonObject::class.java)
                for (value in modelState) {
                    val pars = ProcessingJSON().read(json, value)?.asString
                    if (pars != null) values.add(value)
                }
            }
        }
        return values
    }

    /**
     * Выгружает все показатели для всех данных
     * @name - имя объекта
     * @listObjects - список всех объектов
     * @addressAll - адрес
     * @dataObject - данные объекта
     */
    private fun objectsData(
        name: String, listObjects: List<Object>, addressAll: String, dataObject: List<String>
    ): Map<String, String> {
        val data: MutableMap<String, String> = mutableMapOf()
        for (obj in listObjects) if (name == obj.getNameObject()) {
            val str = RequestGeneration().addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = ProcessingJSON().read(json, STATE)
            if (state != null) {
                json = Gson().fromJson(state, JsonObject::class.java)
                for (dao in dataObject) {
                    val pars = ProcessingJSON().read(json, dao)?.asString
                    if (pars != null) data[dao] = pars
                }
            }
        }
        return data
    }

    //выгружает показатель для определенного данного
    private fun objectData(name: String, objects: String, addressAll: String): String? {
        val str = RequestGeneration().addressAssemblyGET(addressAll, objects)
        var json = Gson().fromJson(str, JsonObject::class.java)

        val state = ProcessingJSON().read(json, STATE)
        if (state != null) {
            json = Gson().fromJson(state, JsonObject::class.java)
            val pars = ProcessingJSON().read(json, name)?.asString
            if (pars != null) return pars

        }
        return null
    }

    private fun chartData(name: String, idObjects: String): List<List<Number>> {
        var address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
        address = request.addressGeneration(address, idObjects)

        val str = RequestGeneration().addressAssemblyGET(address, "packets")

        val json = Gson().fromJson(str, JsonArray::class.java)
        val topic = "$TOPIC$name"

        return ProcessingJSON().readForChart(json, topic)
    }

    //addBlock
    @FXML
    private fun clickIndicators() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("addWidget.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.title = "addWidget"
        stage.scene = Scene(fxmlLoader.load())
        val controller: AddWidgetController = fxmlLoader.getController()
        if (indicatorPane.isVisible)
            controller.load(objectData.getIdModel(), true)
        if (chartPane.isVisible)
            controller.load(objectData.getIdModel(), false)

        stage.showAndWait()
        val addWidget = controller.returnData

        addObjects(addWidget)
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
            if (length > 0) {
                indicatorPane.children.remove(0, length)
            }
        }
        if (chartPane.isVisible) {
            val length = chartPane.children.size
            if (length > 0) {
                chartPane.children.remove(0, length)
            }
        }
        val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
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

        if (controller.save) {
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

    private fun updatePositionIndicator(
        layoutX: Double,
        layoutY: Double,
        translateX: Double,
        translateY: Double
    ): List<Double> {
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

        val indicator = queriesDB.selectIndicator(layoutX, layoutY, objectData.getIdObject())
        if (positionFree(posX, posY)) {
            val anotherIndicator = queriesDB.selectIndicator(posX, posY, objectData.getIdObject())
            queriesDB.updateIndicatorId(anotherIndicator!!.getId(), indicatorsTable.LAYOUT_X.name, layoutX.toString())
            queriesDB.updateIndicatorId(anotherIndicator.getId(), indicatorsTable.LAYOUT_Y.name, layoutY.toString())
        }
        queriesDB.updateIndicatorId(indicator!!.getId(), indicatorsTable.LAYOUT_X.name, posX.toString())
        queriesDB.updateIndicatorId(indicator.getId(), indicatorsTable.LAYOUT_Y.name, posY.toString())
        reloadClick()
        return mutableListOf(posX, posY)
    }

    private fun updatePositionChart(
        layoutX: Double,
        layoutY: Double,
        translateX: Double,
        translateY: Double
    ): List<Double> {
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

        val chart = queriesDB.selectChart(layoutX, layoutY, objectData.getIdObject())
        if (positionFree(posX, posY)) {
            val anotherChart = queriesDB.selectChart(posX, posY, objectData.getIdObject())
            queriesDB.updateChartId(anotherChart!!.getId(), indicatorsTable.LAYOUT_X.name, layoutX.toString())
            queriesDB.updateChartId(anotherChart.getId(), indicatorsTable.LAYOUT_Y.name, layoutY.toString())
        }

        queriesDB.updateChartId(chart!!.getId(), indicatorsTable.LAYOUT_X.name, posX.toString())
        queriesDB.updateChartId(chart.getId(), indicatorsTable.LAYOUT_Y.name, posY.toString())

        reloadClick()
        return mutableListOf(posX, posY)
    }

    @FXML
    private fun movingClick() {
        if (dragFlag) {
            dragFlag = false
            movingImageView.image =
                Image(FileInputStream("./src/main/resources/com/example/controller/images/shrink.png"))
        } else {
            dragFlag = true
            movingImageView.image =
                Image(FileInputStream("./src/main/resources/com/example/controller/images/expand.png"))
        }

    }
    /* override fun run() {
         while (true) {
             println("run")
             Thread.sleep(600)
             reloadClick()
         }
     }*/
}