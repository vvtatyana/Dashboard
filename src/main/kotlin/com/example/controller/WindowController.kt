package com.example.controller

import com.example.building.*
import com.example.building.Chart
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jfoenix.controls.JFXListView
import eu.hansolo.medusa.Gauge
import eu.hansolo.medusa.Gauge.SkinType
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
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
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
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
    private lateinit var username: Label

    @FXML
    private lateinit var addImageView: ImageView

    @FXML
    private lateinit var indicatorPane: AnchorPane

    @FXML
    private lateinit var chartPane: AnchorPane

    @FXML
    private lateinit var devicesList: JFXListView<String>

    private val request = RequestGeneration()
    private val processingJSON = ProcessingJSON()

    private lateinit var objectData: Object
    private lateinit var user: User
    private var objects = mutableListOf<Object>()

    private lateinit var queriesDB: QueriesDB
    private val database = Database()

    private var mouseFlag: Boolean = false
    private var devicesFlag: Boolean = false

    /**
     * Инициализация окна
     */
    fun initialize() {
        Tooltip.install(addImageView, Tooltip("Добавить виджет"))
        Tooltip.install(chartImageView, Tooltip("Показать изменение"))
        Tooltip.install(indicatorImageView, Tooltip("Показать индикаторы"))
        Tooltip.install(dataImageView, Tooltip("Список объектов"))
        Tooltip.install(movingImageView, Tooltip("Перемещение виджетов"))
        Tooltip.install(accountImageView, Tooltip("Аккаунт"))
        Tooltip.install(reloadImageView, Tooltip("Обновить данные"))

        updateTheme()
        queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        chartPane.isVisible = false
        imageViewVisible(false)

        val userBD = queriesDB.selectUser(usersTable.ID.name, ID_USER.toString())
        if (userBD != null) {
            user = userBD
            username.text = user.getUsername()
        }
        objects()
    }

    private fun imageViewVisible(visible: Boolean) {
        indicatorImageView.isVisible = visible
        chartImageView.isVisible = visible
        movingImageView.isVisible = visible
        addImageView.isVisible = visible
        reloadImageView.isVisible = visible
    }

    /**
     * Обновляет тему окна
     */
    private fun updateTheme() {
        themePane(mainPane, headerPane)
        shadowPane(headerPane)

        indicatorPane.style = getMainColor()
        chartPane.style = getMainColor()
        devicesList.style = getAdditionalColor()
        devicesList.effect = dropShadow()
    }

    /**
     * Показывает/убирает список устройств
     */
    @FXML
    private fun devicesClick() {
        val slider = TranslateTransition()
        slider.duration = Duration.seconds(0.4)
        slider.node = panelSlide
        devicesFlag = if (!devicesFlag) {
            action(slider, 0.0, -195.0)
            true
        } else {
            action(slider,-195.0, 0.0)
            false
        }
    }

    private fun action(slider: TranslateTransition, toX: Double, translateX: Double) {

        slider.toX = toX
        slider.play()
        panelSlide.translateX = translateX
        setMargin(panelSlide, Insets(0.0, 0.0, 0.0, toX))
    }

    /**
     * Считывает все объекты у пользоввателя и загружает в бд если их там нет
     */
    private fun objects() {
        val strObjects = request.addressAssemblyGET(DEFAULT_ADDRESS, OBJECTS)

        val jsonObjects = Gson().fromJson(strObjects, JsonArray::class.java)
        objects = processingJSON.readAllObjects(jsonObjects) as MutableList<Object>

        for (obj in objects) {
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
        for (obj in objects) {
            nameListObjects.add(obj.getNameObject())
            val namesObjects = FXCollections.observableArrayList(nameListObjects)
            devicesList.items = namesObjects

            devicesList.onMouseClicked = EventHandler {

                imageViewVisible(true)

                val newValue = devicesList.selectionModel.selectedItem
                if (newValue != null) {
                    indicatorPane.children.remove(0, indicatorPane.children.size)
                    chartPane.children.remove(0, chartPane.children.size)
                    val objDB = queriesDB.selectObject(objectsTable.NAME_OBJECT.name, newValue)

                    if (objDB != null) {
                        objectData = objDB
                    }

                    val values = valuesObject(newValue, objects, address)
                    val data = objectsData(newValue, objects, address, values)
                    uploadObjects(data)
                    devicesClick()
                }
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
            if (mouseFlag) {
                panel.layoutX = event.sceneX + anchorX
                panel.layoutY = event.sceneY + anchorY
            }
        }

        if (panel.prefHeight == pref.CHART.prefHeight) {
            panel.setOnMouseReleased {
                if (mouseFlag) {
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
                if (mouseFlag) {
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
        panel.style = panelTheme()
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
        nameLabel.alignment = Pos.CENTER
        nameLabel.style = decoration.NAME.style
        nameLabel.effect = dropShadow()
        AnchorPane.setRightAnchor(nameLabel, 5.0)
        AnchorPane.setLeftAnchor(nameLabel, 5.0)
        return nameLabel
    }

    /**
     * Создает индикатор
     * @data - показания с устройства
     * @unitText - единицы измерения
     */
    private fun createGauge(data: String?, unitText: String, name: String): Gauge {
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val strModel = request.addressAssemblyGET(
            address, queriesDB.selectObject(
                objectsTable.ID.name, objectData.getId().toString()
            )!!.getIdModel()
        )
        val jsonModel = Gson().fromJson(strModel, JsonObject::class.java)
        val borderFrom = processingJSON.readBorderFrom(jsonModel, name).toMutableMap()
        val borderTo = processingJSON.readBorderTo(jsonModel, name).toMutableMap()
        val borderColor = processingJSON.readBorderColor(jsonModel, name).toMutableMap()
        val keys = borderColor.keys.toList()
        if (borderFrom[keys[0]]!!.toDouble() < 0.0)
            borderFrom[keys[0]] = (borderFrom[keys[0]]!!.toDouble() + 10.0).toString()


        val gaugeBuilder = GaugeBuilder.create()
            .skinType(SkinType.SIMPLE)
            .sectionsVisible(true)
            .title(unitText)
            .animated(true)
            .tickLabelDecimals(1)
            .decimals(1)
            .minValue(borderFrom[keys[0]]!!.toDouble())
            .maxValue(borderTo[keys[keys.size - 1]]!!.toDouble())

        val sections = mutableListOf<Section>()
        borderFrom[keys[0]] = gaugeBuilder.build().minValue.toString()

        for (i in keys.indices) {
            sections.add(
                Section(
                    borderFrom[keys[i]]!!.toDouble(),
                    borderTo[keys[i]]!!.toDouble(),
                    i.toString(),
                    Color.web(borderColor[keys[i]])
                )
            )
        }

        val gauge = gaugeBuilder.sections(sections).build()
        gauge.ledColor = Color.web(textTheme())
        if (data != null)
            gauge.value = data.toDouble()

        AnchorPane.setTopAnchor(gauge, 27.0)
        AnchorPane.setBottomAnchor(gauge, 19.0)
        AnchorPane.setRightAnchor(gauge, 5.0)
        AnchorPane.setLeftAnchor(gauge, 5.0)

        return gauge
    }

    private fun createString(data: String?, name: String): Label {
        val stringLabel = Label(data)
        stringLabel.alignment = Pos.CENTER
        stringLabel.effect = dropShadow()
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = request.addressAssemblyGET(address, objectData.getIdModel())
        val model = Gson().fromJson(getData, JsonObject::class.java)
        val border = ProcessingJSON().readBorderBoolean(model, name)

        if (border.isNotEmpty()) {
            val borderColor = ProcessingJSON().readBorderColor(model, name)
            if (data != null) {
                if (borderColor.containsKey(data)) {
                    stringLabel.style = textStyle(14) + "-fx-background-color: ${borderColor[data]}; -fx-border-color: ${textTheme()}; -fx-border-width: 3;"
                } else stringLabel.style = textStyle( 14)
            }
        }
        else stringLabel.style = textStyle(14)

        AnchorPane.setTopAnchor(stringLabel, 25.0)
        AnchorPane.setBottomAnchor(stringLabel, 30.0)
        AnchorPane.setRightAnchor(stringLabel, 15.0)
        AnchorPane.setLeftAnchor(stringLabel, 15.0)

        return stringLabel
    }

    private fun createCircle(
        data: String?,
        radius: Double,
        top: Double,
        bottom: Double,
        right: Double,
        left: Double,
        name: String
    ): Circle {
        val circle = Circle()
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val getData = request.addressAssemblyGET(address, objectData.getIdModel())
        val model = Gson().fromJson(getData, JsonObject::class.java)
        val border = ProcessingJSON().readBorderBoolean(model, name)

        if (data != null) {
            if (border.isNotEmpty()) {
                val borderColor = ProcessingJSON().readBorderColor(model, name)

                if (data.toBoolean()) {
                    circle.fill = Paint.valueOf(borderColor["True"])
                } else {
                    circle.fill = Paint.valueOf(borderColor["False"])
                }
            } else {
                if (data.toBoolean()) {
                    circle.fill = Paint.valueOf("#6bdb6b")
                } else {
                    circle.fill = Paint.valueOf("#ff4e33")
                }
            }
        } else {
            circle.fill = Paint.valueOf("#636161")
        }
        circle.radius = radius
        circle.stroke = Paint.valueOf(textTheme())
        circle.strokeWidth = 3.0
        circle.effect = dropShadow()
        AnchorPane.setTopAnchor(circle, top)
        AnchorPane.setBottomAnchor(circle, bottom)
        AnchorPane.setRightAnchor(circle, right)
        AnchorPane.setLeftAnchor(circle, left)
        return circle
    }

    private fun createCircleText(data: String?): Label {
        val circleLabel = Label()
        if (data.toBoolean()) {
            circleLabel.text = "Да"
        } else {
            circleLabel.text = "Нет"
        }
        circleLabel.alignment = Pos.CENTER
        circleLabel.style = textStyle(20)

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
        setting.effect = dropShadow()
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
    private fun settingIndicatorClick(
        panel: AnchorPane,
        pos: List<Double>,
        name: Label,
        nameIndicator: String,
        type: String
    ) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("settingIndicator.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())

        val controller: SettingIndicatorController = fxmlLoader.getController()
        controller.load(objectData.getIdModel(), nameIndicator, type)

        stage.showAndWait()


            if (controller.delete) {
                indicatorPane.children.remove(panel)
                queriesDB.deleteIndicator(pos[0], pos[1])
            } else if (controller.save) {
                val dataWidget = controller.dataWidget
                if (dataWidget != null) {
                    updateObject(pos[0], pos[1], dataWidget.getName(), dataWidget.getUnit(), "")
                    reloadClick()
                    if (dataWidget.getName() != "") name.text = dataWidget.getName()
                }
            }
            reloadClick()
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
        layoutX: Double,
        layoutY: Double,
        name: Label,
        type: String
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
        } else if (controller.save) {
            val dataWidget = controller.dataWidget
            val dataList = mutableListOf<List<Number>>()
            updateObject(layoutX, layoutY, dataWidget.getName(), dataWidget.getUnit(), dataWidget.getType())
            if (dataWidget.getName() != "") name.text = dataWidget.getName() + name.text.substring(
                name.text.indexOf(",", 0), name.text.length
            )
            if (dataWidget.getUnit() != "") name.text =
                name.text.substring(0, name.text.indexOf(",", 0) + 1) + dataWidget.getUnit()

            if (dataWidget.getDate() != "") {

                val simpleDateFormat = SimpleDateFormat(DATA_FORMAT)
                val simpleDate = simpleDateFormat.format(Date(dataWidget.getDate().toLong()))


               // areaChart.data.remove(0, areaChart.data.size)

                for (d in data) {
                    val time = Date(d[0].toLong())

                    if (simpleDateFormat.format(time) == simpleDate) {
                        val simpleDateFormatOther = SimpleDateFormat(TIME_FORMAT)
                        val thisTime = LocalTime.parse(simpleDateFormatOther.format(time))
                        if (dataWidget.getFrom() != "") {
                            val fromTime = LocalTime.parse(dataWidget.getFrom())
                            if (thisTime.isAfter(fromTime)) {
                                if (dataWidget.getTo() != "") {
                                    val toTime = LocalTime.parse(dataWidget.getTo())

                                    if (thisTime.isBefore(toTime)) {
                                        dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
//                                        series.data.add(
//                                            XYChart.Data(
//                                                simpleDateFormatOther.format(time),
//                                                d[1].toDouble()
//                                            )
//                                        )
                                    }

                                } else {
                                    dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
//                                    series.data.add(
//                                        XYChart.Data(
//                                            simpleDateFormatOther.format(time),
//                                            d[1].toDouble()
//                                        )
//                                    )
                                }
                            }
                        } else {
                            dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
//                            series.data.add(
//                                XYChart.Data(
//                                    simpleDateFormatOther.format(time),
//                                    d[1].toDouble()
//                                )
//                            )
                        }
                    }
                }
                panel.children.remove(areaChart)
                panel.children.add(createChart(type, dataList))
            }
        }
    }

    /**
     * @data - показатели
     */
    private fun dataSeries(data: List<List<Number>>): Series<String, Number> {
        val dataChart = Series<String, Number>()
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

    /**
     * Создает виджет график
     * @chartType - тип графика
     * @data - показатели
     */
    private fun createChart(chartType: String, data: List<List<Number>>): XYChart<String, Number> {
        val dataChart = dataSeries(data)
        val xAxis = CategoryAxis()
        xAxis.side = Side.BOTTOM
        xAxis.tickLabelFill = Paint.valueOf(textTheme())
        xAxis.tickLabelFont = Font("Segoe UI Semibold", 12.0)

        val yAxis = NumberAxis()
        yAxis.side = Side.BOTTOM
        yAxis.tickLabelFill = Paint.valueOf(textTheme())

        val areaChart = when (chartType) {
            AREA_CHART -> {
                AreaChart(xAxis, yAxis)
            }
            BAR_CHART -> {
                BarChart(xAxis, yAxis)
            }
            LINE_CHART -> {
                LineChart(xAxis, yAxis)
            }
            SCATTER_CHART -> {
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

            val information = when (addWidget.getType()) {
                typeIndicator.NUMBER.type -> {
                    createGauge(data, addWidget.getUnit(), addWidget.getWidget())
                }
                typeIndicator.STRING.type -> {
                    createString(data, addWidget.getName())
                }
                typeIndicator.BOOLEAN.type -> {
                    createCircle(data, 74.0, 24.0, 19.0, 23.0, 23.0, addWidget.getName())
                }
                else -> {
                    null
                }
            }
            panel.children.add(information)

            if (addWidget.getType() == typeIndicator.BOOLEAN.type) {
                panel.children.add(createCircle(null, 34.0, 65.0, 62.0, 64.0, 64.0, ""))
                panel.children.add(createCircleText(data))
            }

            val setting = createSetting(pref.INDICATOR.prefWidth, pref.INDICATOR.prefHeight)

            setting.onMouseClicked = EventHandler {
                settingIndicatorClick(panel, pos, name, "", addWidget.getWidget())

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
                settingChartClick(panel, areaChart, tp, areaChart.layoutX, areaChart.layoutY, name, addWidget.getType())
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
                            createString(data[indicator.getNameIndicator()], indicator.getNameIndicator())
                        }
                        typeIndicator.BOOLEAN.type -> {
                            createCircle(
                                data[indicator.getNameIndicator()],
                                74.0,
                                24.0,
                                19.0,
                                23.0,
                                23.0,
                                indicator.getNameIndicator()
                            )
                        }
                        else -> {
                            null
                        }
                    }
                    panel.children.add(information)

                    if (indicator.getType() == typeIndicator.BOOLEAN.type) {
                        panel.children.add(createCircle(null, 34.0, 65.0, 62.0, 64.0, 64.0, ""))
                        panel.children.add(createCircleText(data[indicator.getNameIndicator()]))
                    }

                    val name = createName(indicator.getName())
                    panel.children.add(name)

                    val setting = createSetting(pref.INDICATOR.prefWidth, pref.INDICATOR.prefHeight)
                    setting.onMouseClicked = EventHandler {
                        settingIndicatorClick(
                            panel,
                            mutableListOf(indicator.getLayoutX(), indicator.getLayoutY()),
                            name, indicator.getNameIndicator(),
                            indicator.getType()
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
                            areaChart,
                            tp,
                            areaChart.layoutX,
                            areaChart.layoutY,
                            name,
                            chart.getType()
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
            val str = request.addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = processingJSON.read(json, STATE)
            if (state != null) {
                val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)

                val strM = request.addressAssemblyGET(address, obj.getIdModel())
                val jsonM = Gson().fromJson(strM, JsonObject::class.java)
                val modelState = processingJSON.readModelState(jsonM)
                json = Gson().fromJson(state, JsonObject::class.java)
                for (value in modelState) {
                    val pars = processingJSON.read(json, value)?.asString
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
            val str = request.addressAssemblyGET(addressAll, obj.getIdObject())
            var json = Gson().fromJson(str, JsonObject::class.java)
            val state = processingJSON.read(json, STATE)
            if (state != null) {
                json = Gson().fromJson(state, JsonObject::class.java)
                for (dao in dataObject) {
                    val pars = processingJSON.read(json, dao)?.asString

                    if (pars != null) data[dao] = pars
                }
            }
        }
        return data
    }

    //выгружает показатель для определенного данного
    private fun objectData(name: String, objects: String, addressAll: String): String? {
        val str = request.addressAssemblyGET(addressAll, objects)
        var json = Gson().fromJson(str, JsonObject::class.java)

        val state = processingJSON.read(json, STATE)
        if (state != null) {
            json = Gson().fromJson(state, JsonObject::class.java)
            val pars = processingJSON.read(json, name)?.asString
            if (pars != null) return pars
        }
        return null
    }

    private fun chartData(name: String, idObjects: String): List<List<Number>> {
        val address = request.addressGeneration(request.addressGeneration(DEFAULT_ADDRESS, OBJECTS), idObjects)

        val str = request.addressAssemblyGET(address, "packets")
        val json = Gson().fromJson(str, JsonArray::class.java)
        val topic = "$TOPIC$name"
        return processingJSON.readForChart(json, topic)
    }

    //addBlock
    @FXML
    private fun clickIndicators() {
        val fxmlLoader = if (indicatorPane.isVisible)
            FXMLLoader(javaClass.getResource("addWidgetIndicator.fxml"))
        else FXMLLoader(javaClass.getResource("addWidgetChart.fxml"))
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
        if (controller.add) {
            val addWidget = controller.returnData
            addObjects(addWidget)
        }
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
        val values = valuesObject(objectData.getNameObject(), objects, address)
        val data = objectsData(objectData.getNameObject(), objects, address, values)
        uploadObjects(data)
    }

    @FXML
    private fun accountClick() {
        val oldTheme = THEME
        val fxmlLoader = FXMLLoader(javaClass.getResource("account.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.isResizable = false
        stage.scene = Scene(fxmlLoader.load())
        val controller: AccountController = fxmlLoader.getController()
        controller.load(user)
        stage.showAndWait()
        if (THEME != oldTheme) {
            queriesDB.updateUser(ID_USER, usersTable.THEME.name, THEME)
            updateTheme()
        }
        if (controller.save) {
            val saveUser = controller.user
            var token = user.getToken()
            var addressDev = user.getAddress()
            var icon = user.getIcon()
            if (saveUser.getToken() != "") {
                token = controller.user.getToken()
                queriesDB.updateUser(ID_USER, usersTable.TOKEN.name, token)
            }
            if (saveUser.getAddress() != "") {
                addressDev = controller.user.getAddress()
                queriesDB.updateUser(ID_USER, usersTable.ADDRESS.name, addressDev)
            }
            if (saveUser.getIcon() != 0) {
                icon = controller.user.getIcon()
                queriesDB.updateUser(ID_USER, usersTable.ICON.name, icon.toString())
            }

            user = User(
                user.getId(),
                user.getIdUser(),
                user.getUsername(),
                user.getLogin(),
                addressDev,
                token,
                user.getCastle(),
                icon,
                user.getTheme()
            )
        }

        if (controller.exit) {
            queriesDB.updateUser(ID_USER, usersTable.CASTLE.name, false.toString())
            database.closeBD()
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
        if (mouseFlag) {
            mouseFlag = false
            movingImageView.image =
                Image(FileInputStream(wayToImage("other/lock")))
        } else {
            mouseFlag = true
            movingImageView.image =
                Image(FileInputStream(wayToImage("other/unlock")))
        }

    }

}