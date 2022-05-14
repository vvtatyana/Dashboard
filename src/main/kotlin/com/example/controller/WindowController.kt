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
import javafx.animation.TranslateTransition
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane.setMargin
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Duration
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WindowController : Initializable {

    @FXML
    private lateinit var accountButton: Button

    @FXML
    private lateinit var reloadButton: Button

    @FXML
    private lateinit var movingButton: Button

    @FXML
    private lateinit var panelSlide: AnchorPane

    @FXML
    private lateinit var dataButton: Button

    @FXML
    private lateinit var indicatorButton: Button

    @FXML
    private lateinit var chartButton: Button

    @FXML
    private lateinit var addButton: Button

    @FXML
    private lateinit var indicatorPane: AnchorPane

    @FXML
    private lateinit var chartPane: AnchorPane

    @FXML
    private lateinit var devicesList: ListView<String>

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
        addButton.tooltip = Tooltip("Добавить виджет")
        chartButton.tooltip = Tooltip("Показать изменение")
        dataButton.tooltip = Tooltip("Список объектов")
        movingButton.tooltip = Tooltip("Перемещение виджетов")
        accountButton.tooltip = Tooltip("Аккаунт")
        reloadButton.tooltip = Tooltip("Обновить данные")

        queriesDB = QueriesDB(database.getConnection(), database.getStatement())

        chartPane.isVisible = false
        imageViewVisible(false)

        val userBD = queriesDB.selectUser(UsersTable.CASTLE.name, true.toString())
        if (userBD != null) {
            user = userBD
        }
        objects()
    }

    private fun initializeScheduler() {
        executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.scheduleAtFixedRate(this::schedule, 0, user.getTimer().toLong(), TimeUnit.MINUTES)
    }

    private fun schedule() {
        /*Plform.runLater {
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
        }*/
    }

    private fun imageViewVisible(visible: Boolean) {
        if (!visible)
            indicatorButton.isVisible = false
        chartButton.isVisible = visible
        movingButton.isVisible = visible
        addButton.isVisible = visible
        reloadButton.isVisible = visible
    }

    private fun updateTheme() {
        val stage: Stage = dataButton.scene.window as Stage
        stage.scene.stylesheets.remove(0, stage.scene.stylesheets.size - 1)
        stage.scene.stylesheets.add(theme())
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

    private fun errorMessage(message: String): Boolean {
        return if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "600 No connection") {
            indicatorPane.children.remove(0, indicatorPane.children.size)
            chartPane.children.remove(0, chartPane.children.size)
            indicatorPane.isVisible = true
            chartPane.isVisible = false
            indicatorButton.isVisible = false
            chartButton.isVisible = false

            val fxmlLoader = FXMLLoader(fxmlLoader("alarmOrInfo.fxml"))
            val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Error", false)

            val controller: AlarmOrInfoController = fxmlLoader.getController()
            controller.load(message)

            stage.showAndWait()
            true
        } else false
    }

    /**
     * Считывает все объекты у пользоввателя и загружает в бд если их там нет
     */
    private fun objects() {
        devicesList.items.remove(0, devicesList.items.size)

        val strObjects = request.getRequest(request.addressGeneration(ADDRESS, OBJECTS))

        if (!errorMessage(strObjects)) {
            val jsonObjects = Gson().fromJson(strObjects, JsonArray::class.java)
            objects = processingJSON.readAllObjects(jsonObjects, user.getId()) as MutableList<Object>

            objects.forEach {
                val objDB = queriesDB.selectObject(ObjectsTable.ID_OBJECT.name, it.getIdObject())
                if (objDB == null)
                    queriesDB.insertIntoObject(it)
            }
            showObject()
        }

    }

    /**
     * Выводит список устройств
     */
    private fun showObject() {
        val address = request.addressGeneration(ADDRESS, OBJECTS)
        val nameListObjects = ArrayList<String>()
        println(objects)
        objects.forEach {
            nameListObjects.add(it.getNameObject())
        }
        val namesObjects = FXCollections.observableArrayList(nameListObjects)
        println(namesObjects)
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
                if (values != null) {
                    val data = objectsData(newValue, objects, address, values)
                    if (data != null) {
                        uploadObjects(data)
                        devicesClick()
                    }
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
        val fxmlLoader = FXMLLoader(fxmlLoader("settingChart.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Setting", false)

        val controller: SettingChartController = fxmlLoader.getController()
        val chart = queriesDB.selectChart(layoutX, layoutY, objectData.getId())!!
        controller.load(layoutX, layoutY, chart)

        stage.showAndWait()
        if (controller.delete) {
            chartPane.children.remove(panel.getPanel())
            queriesDB.deleteChart(layoutX, layoutY)
        } else if (controller.save) {
            val dataWidget = controller.dataWidget

            if (chart.getType() != dataWidget.getType())
                panel.updateType(dataWidget.getType())

            updateObject(chart.getId(), dataWidget.getName(), dataWidget.getUnit(), dataWidget.getType())
            if (dataWidget.getName() != "" && dataWidget.getName() != chart.getName()) panel.setTitle(
                dataWidget.getName() + name.text.substring(
                    name.text.indexOf(",", 0), name.text.length
                )
            )
            if (dataWidget.getUnit() != "" && dataWidget.getName() != chart.getUnit()) panel.setTitle(
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
                                        series.data.add(
                                            XYChart.Data(
                                                simpleDateFormatOther.format(time),
                                                d[1].toDouble()
                                            )
                                        )
                                    }

                                } else {
                                    series.data.add(
                                        XYChart.Data(
                                            simpleDateFormatOther.format(time),
                                            d[1].toDouble()
                                        )
                                    )
                                }
                            }
                        } else {
                            series.data.add(
                                XYChart.Data(
                                    simpleDateFormatOther.format(time),
                                    d[1].toDouble()
                                )
                            )
                        }
                    }
                }
                panel.updateChart(series)
            }
        }
    }

    @FXML
    private fun settingIndicatorClick(
        id: Int,
        panel: AbstractWidget,
        pos: List<Double>,
        name: String,
        type: String
    ) {
        val fxmlLoader = FXMLLoader(fxmlLoader("settingIndicator.fxml"))

        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Setting", false)

        val controller: SettingIndicatorController = fxmlLoader.getController()

        if (controller.load(objectData.getIdModel(), name, type)) {

            stage.showAndWait()

            if (controller.delete) {
                indicatorPane.children.remove(panel.getPanel())
                queriesDB.deleteIndicator(pos[0], pos[1])
            } else if (controller.save) {
                val dataWidget = controller.dataWidget
                if (dataWidget != null) {
                    updateObject(id, dataWidget.getName(), dataWidget.getUnit(), "")
                    if (dataWidget.getName() != "") panel.setTitle(dataWidget.getName())
                    if (panel is NumericWidget && dataWidget.getUnit() != "") panel.setUnit(dataWidget.getUnit())
                }
            }
        } else {
            stage.close()
            errorMessage(controller.message)
        }
    }

    private fun updateObject(id: Int, nameText: String, unitText: String, type: String) {
        if (indicatorPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateIndicator(id, IndicatorsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateIndicator(id, IndicatorsTable.UNIT.name, unitText)
            }
        }
        if (chartPane.isVisible) {
            if (nameText != "") {
                queriesDB.updateChart(id, ChartsTable.NAME.name, nameText)
            }

            if (unitText != "") {
                queriesDB.updateChart(id, ChartsTable.UNIT.name, unitText)
            }

            if (type != "") {
                queriesDB.updateChart(id, ChartsTable.TYPE.name, type)
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
            val address = request.addressGeneration(ADDRESS, OBJECTS)
            val obj = queriesDB.selectObject(ObjectsTable.ID.name, objectData.getId().toString())

            val data = objectData(addWidget.getWidget(), obj!!.getIdObject(), address)

            queriesDB.insertIntoIndicator(
                Indicator(
                    null,
                    objectData.getId(),
                    addWidget.getWidget(),
                    pos[0],
                    pos[1],
                    addWidget.getName(),
                    addWidget.getUnit(),
                    addWidget.getType()
                )
            )
            val indicator = queriesDB.selectIndicator(pos[0], pos[1], objectData.getId())

            if (indicator != null) {
                addIndicatorToPanel(indicator, data.toString())
            }
        }
        if (chartPane.isVisible) {
            val pos = getPositionChart()
            queriesDB.insertIntoChart(
                Chart(
                    null,
                    objectData.getId(),
                    addWidget.getWidget(),
                    pos[0],
                    pos[1],
                    addWidget.getName(),
                    addWidget.getUnit(),
                    addWidget.getType()
                )
            )

            val chart = queriesDB.selectChart(pos[0], pos[1], objectData.getId())

            if (chart != null) {
                addChartToPanel(chart)
            }
        }
    }

    private fun addIndicatorToPanel(indicator: Indicator, data: String): Boolean {
        val address = request.addressGeneration(ADDRESS, MODELS)
        val strModel = request.getRequest(
            request.addressGeneration(
                address,
                queriesDB.selectObject(ObjectsTable.ID.name, objectData.getId().toString())!!.getIdModel()
            )
        )
        if (!errorMessage(strModel)) {
            val panel = when (indicator.getType()) {
                TypeIndicator.NUMBER.type -> {
                    NumericWidget(
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.prefWidth,
                        indicator.getName(),
                        indicator.getUnit(),
                        data,
                        indicator.getNameIndicator(),
                        strModel
                    )
                }
                TypeIndicator.STRING.type -> {
                    StringWidget(
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.prefWidth,
                        indicator.getName(),
                        data,
                        indicator.getNameIndicator(),
                        strModel
                    )
                }
                TypeIndicator.BOOLEAN.type -> {
                    BooleanWidget(
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.prefWidth,
                        indicator.getName(),
                        data,
                        indicator.getNameIndicator(),
                        strModel
                    )
                }
                else -> {
                    StringWidget(
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.prefWidth,
                        indicator.getName(),
                        data,
                        indicator.getNameIndicator(),
                        strModel
                    )
                }
            }

            mouseDraggedPanel(panel.getPanel(), indicator.getLayoutX(), indicator.getLayoutY())
            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                settingIndicatorClick(
                    indicator.getId(),
                    panel,
                    listOf(indicator.getLayoutX(), indicator.getLayoutY()), indicator.getNameIndicator(),
                    indicator.getType()
                )
            }
            widgets.add(panel)
            indicatorPane.children.add(panel.getPanel())
            return true
        }
        return false
    }

    private fun addChartToPanel(chart: Chart): Boolean {
        val tp = chartData(chart.getNameChart(), objectData.getIdObject())
        if (tp != null) {
            val panel = ChartWidget(
                chart.getLayoutX(),
                chart.getLayoutY(),
                Pref.CHART.prefHeight,
                "${chart.getName()}, ${chart.getUnit()}",
                tp,
                chart.getType()
            )
            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                val areaChart = panel.getPanel()
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
            return true
        } else return false
    }

    /**
     * Загружает виджеты из бд
     * @data - данные
     */
    private fun uploadObjects(data: Map<String, String>) {
        val indicators = queriesDB.selectIndicators(objectData.getId().toString())
        if (indicators != null) {
            var flag = true
            for (indicator in indicators) {
                if (indicator.getIdObject() == objectData.getId() && !positionFree(
                        indicator.getLayoutX(), indicator.getLayoutY()
                    )
                ) {
                    flag = addIndicatorToPanel(indicator, data[indicator.getNameIndicator()].toString())
                }
                if (!flag) break
            }
        }

        val charts = queriesDB.selectCharts(objectData.getId().toString())

        if (charts != null) {
            var flag = true
            for (chart in charts) {
                if (chart.getIdObject() == objectData.getId() && !positionFree(
                        chart.getLayoutX(), chart.getLayoutY()
                    )
                ) {
                    flag = addChartToPanel(chart)
                }
                if (!flag) break
            }
        }
    }

    /**
     * Выгружает названия данных у объекта
     * @name - имя объекта
     * @listObjects - список всех объектов
     * @addressAll - адрес
     */
    private fun valuesObject(name: String, listObjects: List<Object>, addressAll: String): List<String>? {
        val values = mutableListOf<String>()
        listObjects.forEach {
            if (name == it.getNameObject()) {
                val str = request.getRequest(request.addressGeneration(addressAll, it.getIdObject()))
                if (!errorMessage(str)) {
                    var json = Gson().fromJson(str, JsonObject::class.java)

                    val state = processingJSON.read(json, STATE)
                    if (state != null) {
                        val address = request.addressGeneration(ADDRESS, MODELS)

                        val strM = request.getRequest(request.addressGeneration(address, it.getIdModel()))
                        if (!errorMessage(strM)) {
                            val jsonM = Gson().fromJson(strM, JsonObject::class.java)
                            val modelState = processingJSON.readModelState(jsonM)
                            json = Gson().fromJson(state, JsonObject::class.java)
                            for (value in modelState) {
                                val pars = processingJSON.read(json, value)?.asString
                                if (pars != null) values.add(value)
                            }
                        }
                    }
                } else return null
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
    ): Map<String, String>? {
        val data: MutableMap<String, String> = mutableMapOf()
        listObjects.forEach {
            if (name == it.getNameObject()) {
                val str = request.getRequest(request.addressGeneration(addressAll, it.getIdObject()))
                if (!errorMessage(str)) {
                    var json = Gson().fromJson(str, JsonObject::class.java)
                    val state = processingJSON.read(json, STATE)
                    if (state != null) {
                        json = Gson().fromJson(state, JsonObject::class.java)
                        for (dao in dataObject) {
                            val pars = processingJSON.read(json, dao)?.asString

                            if (pars != null) data[dao] = pars
                        }
                    }
                } else return null
            }
        }
        return data
    }

    //выгружает показатель для определенного данного
    private fun objectData(name: String, objects: String, addressAll: String): String? {
        val str = request.getRequest(request.addressGeneration(addressAll, objects))
        if (!errorMessage(str)) {
            var json = Gson().fromJson(str, JsonObject::class.java)

            val state = processingJSON.read(json, STATE)
            if (state != null) {
                json = Gson().fromJson(state, JsonObject::class.java)
                val pars = processingJSON.read(json, name)?.asString
                if (pars != null) return pars
            }
        }
        return null
    }

    private fun chartData(name: String, idObjects: String): List<List<Number>>? {
        val address = request.addressGeneration(request.addressGeneration(ADDRESS, OBJECTS), idObjects)

        val str = request.getRequest(request.addressGeneration(address, "packets"))

        if (!errorMessage(str)) {
            val json = Gson().fromJson(str, JsonArray::class.java)
            val topic = "$TOPIC_WAY$name"
            return processingJSON.readForChart(json, topic)
        }
        return null
    }

    @FXML
    private fun clickIndicators() {
        val fxmlLoader = if (indicatorPane.isVisible)
            FXMLLoader(fxmlLoader("addWidgetIndicator.fxml"))
        else FXMLLoader(fxmlLoader("addWidgetChart.fxml"))

        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Add", false)
        val controller: AddWidgetController = fxmlLoader.getController()
        val error = if (indicatorPane.isVisible)
            controller.load(objectData.getIdModel(), true)
        else
            controller.load(objectData.getIdModel(), false)

        if (!error) {
            stage.showAndWait()
            if (controller.add) {
                val addWidget = controller.widget
                addObjects(addWidget)
            }
        } else {
            stage.close()
            errorMessage(controller.message)
        }
    }

    private fun getPositionIndicator(): List<Double> {
        var layoutX = 0.0
        var layoutY = 0.0
        indicatorPane.children.forEach {
            if (it.layoutX + it.prefWidth(0.0) < 1393.0 && it.prefWidth(0.0) == 200.0) {
                layoutX = it.layoutX + it.prefWidth(0.0) + 5.0
            } else if (it.prefWidth(0.0) == 200.0) {
                layoutX = 0.0
                layoutY = it.layoutY + it.prefHeight(0.0) + 5.0
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
            indicatorPane.children.forEach {
                if (it.layoutX == layoutX && it.layoutY == layoutY) {
                    return true
                }
            }
        }
        if (chartPane.isVisible) {
            chartPane.children.forEach {
                if (it.layoutX == layoutX && it.layoutY == layoutY) {
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
        val address = request.addressGeneration(ADDRESS, OBJECTS)
        val values = valuesObject(objectData.getNameObject(), objects, address)
        if (values != null) {
            val data = objectsData(objectData.getNameObject(), objects, address, values)
            if (data != null) uploadObjects(data)
        }
    }

    @FXML
    private fun accountClick() {
        val fxmlLoader = FXMLLoader(fxmlLoader("account.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Account", false)

        val token = user.getToken()
        val addressDev = user.getAddress()
        val icon = user.getIcon()

        val controller: AccountController = fxmlLoader.getController()
        controller.load(user)
        stage.showAndWait()

        if (controller.save) {
            if (user.getToken() != token) {
                queriesDB.updateUser(user.getId(), UsersTable.TOKEN.name, user.getToken())
                objects()
            }
            if (user.getAddress() != addressDev) {
                queriesDB.updateUser(user.getId(), UsersTable.ADDRESS.name, user.getAddress())
                objects()
            }
            if (user.getIcon() != icon) {
                queriesDB.updateUser(user.getId(), UsersTable.ICON.name, user.getIcon().toString())
            }
        }

        if (controller.exit) {
            queriesDB.updateUser(user.getId(), UsersTable.CASTLE.name, false.toString())
            database.closeBD()
            var newStage: Stage = dataButton.scene.window as Stage
            newStage.close()
            val newFxmlLoader = FXMLLoader(fxmlLoader("loginWindow.fxml"))
            newStage = createStage(newFxmlLoader, Modality.APPLICATION_MODAL, "login", false)
            newStage.show()
        }
    }

    @FXML
    private fun indicatorClick() {
        indicatorButton.isVisible = false
        chartButton.isVisible = true
        indicatorPane.isVisible = true
        chartPane.isVisible = false
        reloadClick()
    }

    @FXML
    private fun chartClick() {
        indicatorButton.isVisible = true
        chartButton.isVisible = false
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
        val newPos = shift(translateX, translateY, 200.0)

        val indicator = queriesDB.selectIndicator(layoutX, layoutY, objectData.getId())
        if (positionFree(newPos[0], newPos[1])) {
            val anotherIndicator = queriesDB.selectIndicator(newPos[0], newPos[1], objectData.getId())
            if (anotherIndicator != null) {
                widgets.forEach {
                    if (it !is ChartWidget && it.getPanel().layoutX == newPos[0] && it.getPanel().layoutY == newPos[1]) {
                        it.getPanel().layoutX = layoutX
                        it.getPanel().layoutY = layoutY
                    }
                }
                queriesDB.updateIndicator(anotherIndicator.getId(), IndicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateIndicator(anotherIndicator.getId(), IndicatorsTable.LAYOUT_Y.name, layoutY.toString())

            }
        }
        if (indicator != null) {
            queriesDB.updateIndicator(indicator.getId(), IndicatorsTable.LAYOUT_X.name, newPos[0].toString())
            queriesDB.updateIndicator(indicator.getId(), IndicatorsTable.LAYOUT_Y.name, newPos[1].toString())
        }
        return mutableListOf(newPos[0], newPos[1])
    }

    private fun updatePositionChart(
        layoutX: Double,
        layoutY: Double,
        translateX: Double,
        translateY: Double
    ): List<Double> {
        val newPos = shift(translateX, translateY, 305.0)

        val chart = queriesDB.selectChart(layoutX, layoutY, objectData.getId())
        if (positionFree(newPos[0], newPos[1])) {
            val anotherChart = queriesDB.selectChart(newPos[0], newPos[1], objectData.getId())
            if (anotherChart != null) {
                widgets.forEach {
                    if (it is ChartWidget && it.getPanel().layoutX == newPos[0] && it.getPanel().layoutY == newPos[1]) {
                        it.getPanel().layoutX = layoutX
                        it.getPanel().layoutY = layoutY
                    }
                }
                queriesDB.updateChart(anotherChart.getId(), IndicatorsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateChart(anotherChart.getId(), IndicatorsTable.LAYOUT_Y.name, layoutY.toString())
            }
        }
        if (chart != null) {
            queriesDB.updateChart(chart.getId(), IndicatorsTable.LAYOUT_X.name, newPos[0].toString())
            queriesDB.updateChart(chart.getId(), IndicatorsTable.LAYOUT_Y.name, newPos[1].toString())
        }
        return mutableListOf(newPos[0], newPos[1])
    }

    private fun shift(
        translateX: Double,
        translateY: Double,
        step: Double
    ): List<Double> {
        var posX = 0.0
        var posY = 0.0
        while (translateX - posX > step) {
            posX += step + 5
        }
        if (translateX - posX > step / 2) {
            posX += step + 5
        }
        while (translateY - posY > step) {
            posY += step + 5
        }
        if (translateY - posY > step / 2) {
            posY += step + 5
        }
        return listOf(posX, posY)
    }

    @FXML
    private fun clickSetting() {

        val oldTheme = THEME
        val fxmlLoader = FXMLLoader(fxmlLoader("setting.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "Setting", false)

        val controller: SettingController = fxmlLoader.getController()
        controller.loader(user)
        stage.showAndWait()

        if (THEME != oldTheme) {
            queriesDB.updateUser(user.getId(), UsersTable.THEME.name, THEME)
            updateTheme()
            for (widget in widgets) {
                if (widget is StringWidget) {
                    widget.updateString()
                }
            }
        }
        if (controller.save) {
            queriesDB.updateUser(user.getId(), UsersTable.TIMER.name, controller.user.getTimer().toString())
        }
    }

    @FXML
    private fun movingClick() {
        val imageView = if (mouseFlag) {
            mouseFlag = false
            createImageView("lock", 30.0)
        } else {
            mouseFlag = true
            createImageView("unlock", 30.0)
        }
        movingButton.graphic = imageView
    }
}