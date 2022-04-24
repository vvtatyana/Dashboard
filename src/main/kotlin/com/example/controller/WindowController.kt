package com.example.controller

import com.example.building.*
import com.example.database.Database
import com.example.database.QueriesDB
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.example.widget.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jfoenix.controls.JFXListView
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.chart.XYChart
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane.setMargin
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import java.io.FileInputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WindowController : Initializable {

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

    private var devicesFlag: Boolean = false

    private var mouseFlag: Boolean = false
    private lateinit var executorService: ScheduledExecutorService

    private val widgets = mutableListOf<AbstractWidget>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Tooltip.install(addImageView, Tooltip("Добавить виджет"))
        Tooltip.install(chartImageView, Tooltip("Показать изменение"))
        Tooltip.install(indicatorImageView, Tooltip("Показать индикаторы"))
        Tooltip.install(dataImageView, Tooltip("Список объектов"))
        Tooltip.install(movingImageView, Tooltip("Перемещение виджетов"))
        Tooltip.install(accountImageView, Tooltip("Аккаунт"))
        Tooltip.install(reloadImageView, Tooltip("Обновить данные"))

        queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        chartPane.isVisible = false
        imageViewVisible(false)

        val userBD = queriesDB.selectUser(UsersTable.ID.name, ID_USER.toString())
        if (userBD != null) {
            user = userBD
            username.text = user.getUsername()
        }
        objects()
    }

    private fun initializeScheduler() {
        executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.scheduleAtFixedRate(this::schedule, 0, 120, TimeUnit.SECONDS)
    }

    private fun schedule() {
        Platform.runLater {
            val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
            val values = valuesObject(objectData.getNameObject(), objects, address)
            val data = objectsData(objectData.getNameObject(), objects, address, values)

            for (widget in widgets) {
                val indicator = queriesDB.selectIndicatorId(widget.getIndicator())
                if (indicator != null) {
                    if (widget is BooleanWidget) {
                        widget.setValue(data[indicator.getNameIndicator()].toBoolean())
                    }
                    if (widget is NumericWidget) {
                        widget.setValue(data[indicator.getNameIndicator()]!!.toDouble())
                    }
                    if (widget is StringWidget) {
                        widget.setValue(data[indicator.getNameIndicator()].toString())
                    }
                }
            }
        }
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
        val stage: Stage = username.scene.window as Stage
        stage.scene.stylesheets.remove(0, stage.scene.stylesheets.size-1)
        stage.scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
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
            action(slider, -195.0, 0.0)
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
            val objDB = queriesDB.selectObject(ObjectsTable.ID_OBJECT.name, obj.getIdObject())
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
                    val objDB = queriesDB.selectObject(ObjectsTable.NAME_OBJECT.name, newValue)

                    if (objDB != null) {
                        objectData = objDB
                        initializeScheduler()
                    }

                    widgets.removeAll(widgets)

                    val values = valuesObject(newValue, objects, address)
                    val data = objectsData(newValue, objects, address, values)
                    uploadObjects(data)
                    devicesClick()
                }
            }
        }
    }

    @FXML
    private fun settingChartClick(
        panel: ChartWidget,
        data: List<List<Number>>,
        layoutX: Double,
        layoutY: Double,
        name: Label
    ) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("settingChart.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage.isResizable = false
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene

        val controller: SettingChartController = fxmlLoader.getController()
        controller.load(layoutX, layoutY)

        stage.showAndWait()
        if (controller.delete) {
            chartPane.children.remove(panel.getPanel())
            queriesDB.deleteChart(layoutX, layoutY)
        } else if (controller.save) {
            val dataWidget = controller.dataWidget

            updateObject(layoutX, layoutY, dataWidget.getName(), dataWidget.getUnit(), dataWidget.getType())
            if (dataWidget.getName() != "") panel.setTitle(
                dataWidget.getName() + name.text.substring(
                    name.text.indexOf(",", 0), name.text.length
                )
            )
            if (dataWidget.getUnit() != "") panel.setTitle(
                name.text.substring(
                    0,
                    name.text.indexOf(",", 0) + 1
                ) + dataWidget.getUnit()
            )

            if (dataWidget.getDate() != "") {
                val series = XYChart.Series<String, Number>()
                val simpleDateFormat = SimpleDateFormat(DATA_FORMAT)
                val simpleDate = simpleDateFormat.format(Date(dataWidget.getDate().toLong()))
                series.name = simpleDate
                panel.getChart().data.remove(0, panel.getChart().data.size)
                panel.getChart().data.add(series)
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
                                        //     dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
                                        series.data.add(
                                            XYChart.Data(
                                                simpleDateFormatOther.format(time),
                                                d[1].toDouble()
                                            )
                                        )
                                    }

                                } else {
                                    //   dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
                                    series.data.add(
                                        XYChart.Data(
                                            simpleDateFormatOther.format(time),
                                            d[1].toDouble()
                                        )
                                    )
                                }
                            }
                        } else {
                            //  dataList.add(mutableListOf(d[0].toLong(), d[1].toDouble()))
                            series.data.add(
                                XYChart.Data(
                                    simpleDateFormatOther.format(time),
                                    d[1].toDouble()
                                )
                            )
                        }
                    }
                }

            }
        }
    }

    @FXML
    private fun settingIndicatorClick(
        panel: AbstractWidget,
        pos: List<Double>,
        name: String,
        type: String
    ) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("settingIndicator.fxml"))
        val stage = Stage()
        stage.initModality(Modality.WINDOW_MODAL)
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage.isResizable = false
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene

        val controller: SettingIndicatorController = fxmlLoader.getController()

        controller.load(objectData.getIdModel(), name, type)

        stage.showAndWait()

        if (controller.delete) {
            indicatorPane.children.remove(panel.getPanel())
            queriesDB.deleteIndicator(pos[0], pos[1])
        } else if (controller.save) {
            val dataWidget = controller.dataWidget
            if (dataWidget != null) {
                updateObject(pos[0], pos[1], dataWidget.getName(), dataWidget.getUnit(), "")
                if (dataWidget.getName() != "") panel.setTitle(dataWidget.getName())
                if (panel is NumericWidget && dataWidget.getUnit() != "") panel.setUnit(dataWidget.getUnit())
            }
        }
    }

    private fun updateObject(layoutX: Double, layoutY: Double, nameText: String, unitText: String, type: String) {
        if (indicatorPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateIndicator(layoutX, layoutY, IndicatorsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateIndicator(layoutX, layoutY, IndicatorsTable.UNIT.name, unitText)
            }
        }
        if (chartPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateChart(layoutX, layoutY, ChartsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateChart(layoutX, layoutY, ChartsTable.UNIT.name, unitText)
            }

            if (type != "") {
                queriesDB.updateChart(layoutX, layoutY, ChartsTable.TYPE.name, type)
            }
        }
    }

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

        if (panel.prefHeight == Pref.CHART.prefHeight) {
            panel.setOnMouseReleased {
                if (mouseFlag) {
                    val newPos = updatePositionChart(
                        layoutX,
                        layoutY,
                        panel.layoutX,
                        panel.layoutY
                    )
                    panel.layoutX = newPos[0]
                    panel.layoutY = newPos[1]
                }
            }
        } else {
            panel.setOnMouseReleased {
                if (mouseFlag) {
                    val newPos = updatePositionIndicator(
                        layoutX,
                        layoutY,
                        panel.layoutX,
                        panel.layoutY
                    )
                    panel.layoutX = newPos[0]
                    panel.layoutY = newPos[1]
                }
            }
        }
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
            val address = request.addressGeneration(DEFAULT_ADDRESS, OBJECTS)
            val obj = queriesDB.selectObject(ObjectsTable.ID.name, objectData.getId().toString())

            val data = objectData(addWidget.getWidget(), obj!!.getIdObject(), address)
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
            val indicator = queriesDB.selectIndicator(pos[0], pos[1], objectData.getIdObject())

            val panel = when (addWidget.getType()) {
                TypeIndicator.NUMBER.type -> {
                    NumericWidget(
                        objectData,
                        indicator!!.getId(),
                        pos[0],
                        pos[1],
                        Pref.INDICATOR.prefHeight,
                        indicator.getName(),
                        indicator.getUnit(),
                        data,
                        addWidget.getWidget(),
                        queriesDB
                    )
                }
                TypeIndicator.STRING.type -> {
                    StringWidget(
                        objectData,
                        indicator!!.getId(),
                        pos[0],
                        pos[1],
                        Pref.INDICATOR.prefHeight,
                        addWidget.getName(),
                        data,
                        addWidget.getWidget()
                    )
                }
                TypeIndicator.BOOLEAN.type -> {
                    BooleanWidget(
                        objectData,
                        indicator!!.getId(),
                        pos[0],
                        pos[1],
                        Pref.INDICATOR.prefHeight,
                        addWidget.getName(),
                        data,
                        addWidget.getWidget()
                    )
                }
                else -> {
                    StringWidget(
                        objectData,
                        indicator!!.getId(),
                        pos[0],
                        pos[1],
                        Pref.INDICATOR.prefHeight,
                        addWidget.getName(),
                        data,
                        addWidget.getWidget()
                    )
                }
            }
            mouseDraggedPanel(panel.getPanel(), pos[0], pos[1])

            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                settingIndicatorClick(panel, pos, addWidget.getWidget(), addWidget.getType())
            }

            indicatorPane.children.add(panel.getPanel())


        }

        if (chartPane.isVisible) {
            val pos = getPositionChart()
            val tp = chartData(addWidget.getWidget(), objectData.getIdObject())
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

            val chart = queriesDB.selectChart(pos[0], pos[1], objectData.getIdObject())

            val panel = ChartWidget(
                chart!!.getId(),
                pos[0],
                pos[1],
                Pref.CHART.prefHeight,
                "${addWidget.getName()}, ${addWidget.getUnit()}",
                tp,
                addWidget.getType()
            )
            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                val areaChart = panel.getChart()
                settingChartClick(
                    panel,
                    tp,
                    areaChart.layoutX,
                    areaChart.layoutY,
                    panel.getTitle()
                )
            }
            mouseDraggedPanel(panel.getPanel(), pos[0], pos[1])
            chartPane.children.add(panel.getPanel())
        }
    }

    /**
     * Загружает виджеты из бд
     * @data - данные
     */
    private fun uploadObjects(data: Map<String, String>) {
        val indicators = queriesDB.selectIndicators(objectData.getIdObject())
        if (indicators != null) {
            for (indicator in indicators) {
                if (indicator.getIdObject() == objectData.getIdObject() && !positionFree(
                        indicator.getLayoutX(), indicator.getLayoutY()
                    )
                ) {
                    val panel = when (indicator.getType()) {
                        TypeIndicator.NUMBER.type -> {
                            NumericWidget(
                                objectData,
                                indicator.getId(),
                                indicator.getLayoutX(),
                                indicator.getLayoutY(),
                                Pref.INDICATOR.prefWidth,
                                indicator.getName(),
                                indicator.getUnit(),
                                data[indicator.getNameIndicator()],
                                indicator.getNameIndicator(),
                                queriesDB
                            )
                        }
                        TypeIndicator.STRING.type -> {
                            StringWidget(
                                objectData,
                                indicator.getId(),
                                indicator.getLayoutX(),
                                indicator.getLayoutY(),
                                Pref.INDICATOR.prefWidth,
                                indicator.getName(),
                                data[indicator.getNameIndicator()],
                                indicator.getNameIndicator()
                            )
                        }
                        TypeIndicator.BOOLEAN.type -> {
                            BooleanWidget(
                                objectData,
                                indicator.getId(),
                                indicator.getLayoutX(),
                                indicator.getLayoutY(),
                                Pref.INDICATOR.prefWidth,
                                indicator.getName(),
                                data[indicator.getNameIndicator()],
                                indicator.getNameIndicator()
                            )
                        }
                        else -> {
                            StringWidget(
                                objectData,
                                indicator.getId(),
                                indicator.getLayoutX(),
                                indicator.getLayoutY(),
                                Pref.INDICATOR.prefWidth,
                                indicator.getName(),
                                data[indicator.getNameIndicator()],
                                indicator.getNameIndicator()
                            )
                        }
                    }
                    mouseDraggedPanel(panel.getPanel(), indicator.getLayoutX(), indicator.getLayoutY())
                    val setting = panel.getSetting()
                    setting.onMouseClicked = EventHandler {
                        settingIndicatorClick(
                            panel,
                            listOf(indicator.getLayoutX(), indicator.getLayoutY())
                            , indicator.getNameIndicator(),
                            indicator.getType()
                        )
                    }
                    widgets.add(panel)
                    indicatorPane.children.add(panel.getPanel())
                }
            }
        }

        val charts = queriesDB.selectCharts(objectData.getIdObject())

        if (charts != null) {
            for (chart in charts) {
                if (chart.getIdObject() == objectData.getIdObject() && !positionFree(
                        chart.getLayoutX(), chart.getLayoutY()
                    )
                ) {
                    val tp = chartData(chart.getNameChart(), objectData.getIdObject())
                    val panel = ChartWidget(
                        chart.getId(),
                        chart.getLayoutX(),
                        chart.getLayoutY(),
                        Pref.CHART.prefHeight,
                        "${chart.getName()}, ${chart.getUnit()}",
                        tp,
                        chart.getType()
                    )
                    val setting = panel.getSetting()
                    setting.onMouseClicked = EventHandler {
                        val areaChart = panel.getChart()
                        settingChartClick(
                            panel,
                            tp,
                            areaChart.layoutX,
                            areaChart.layoutY,
                            panel.getTitle()
                        )
                    }
                    mouseDraggedPanel(panel.getPanel(), chart.getLayoutX(), chart.getLayoutY())
                    chartPane.children.add(panel.getPanel())

                }
            }
//        val thread = WindowController()
//        thread.start()
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
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage.isResizable = false
        stage.title = "addWidget"
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene
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
        stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
        stage.isResizable = false
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
        stage.scene = scene
        val controller: AccountController = fxmlLoader.getController()
        controller.load(user)
        stage.showAndWait()
        if (THEME != oldTheme) {
            queriesDB.updateUser(ID_USER, UsersTable.THEME.name, THEME)
            updateTheme()
            for (widget in widgets) {
                widget.updateColor()
                if (widget is StringWidget) {
                    widget.updateString()
                }
            }
        }
        if (controller.save) {
            val saveUser = controller.user
            var token = user.getToken()
            var addressDev = user.getAddress()
            var icon = user.getIcon()
            if (saveUser.getToken() != "") {
                token = controller.user.getToken()
                queriesDB.updateUser(ID_USER, UsersTable.TOKEN.name, token)
            }
            if (saveUser.getAddress() != "") {
                addressDev = controller.user.getAddress()
                queriesDB.updateUser(ID_USER, UsersTable.ADDRESS.name, addressDev)
            }
            if (saveUser.getIcon() != 0) {
                icon = controller.user.getIcon()
                queriesDB.updateUser(ID_USER, UsersTable.ICON.name, icon.toString())
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
            queriesDB.updateUser(ID_USER, UsersTable.CASTLE.name, false.toString())
            database.closeBD()
            var newStage: Stage = username.scene.window as Stage
            newStage.close()
            val newFxmlLoader = FXMLLoader(javaClass.getResource("loginWindow.fxml"))
            newStage = Stage()
            stage.isResizable = false
            newStage.initModality(Modality.APPLICATION_MODAL)
            stage.icons.add(Image(FileInputStream(wayToImage("other/smart_house"))))
            newStage.title = "login"
            val newScene = Scene(newFxmlLoader.load())
            newScene.stylesheets.add(this.javaClass.getResource("\\css\\$THEME.css")!!.toExternalForm())
            newStage.scene = newScene
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
            if (anotherIndicator != null) {
                queriesDB.updateIndicatorId(anotherIndicator.getId(), IndicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateIndicatorId(anotherIndicator.getId(), IndicatorsTable.LAYOUT_Y.name, layoutY.toString())
            }
        }
        if (indicator != null) {
            queriesDB.updateIndicatorId(indicator.getId(), IndicatorsTable.LAYOUT_X.name, posX.toString())
            queriesDB.updateIndicatorId(indicator.getId(), IndicatorsTable.LAYOUT_Y.name, posY.toString())
        }

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
            if (anotherChart != null) {
                queriesDB.updateChartId(anotherChart.getId(), IndicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateChartId(anotherChart.getId(), IndicatorsTable.LAYOUT_Y.name, layoutY.toString())
            }
        }
        if (chart != null) {
            queriesDB.updateChartId(chart.getId(), IndicatorsTable.LAYOUT_X.name, posX.toString())
            queriesDB.updateChartId(chart.getId(), IndicatorsTable.LAYOUT_Y.name, posY.toString())
        }
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