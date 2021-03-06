package com.example.controller

import com.example.building.*
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
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
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
    private lateinit var settingButton: Button

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
    private lateinit var devicesList: JFXListView<String>

    @FXML
    private lateinit var refreshMenu: Button

    private val request = RequestGeneration()
    private val processingJSON = ProcessingJSON()
    private lateinit var objectData: Object
    private lateinit var user: User
    private var objects = mutableListOf<Object>()
    private lateinit var queriesDB: QueriesDB
    private var devicesFlag: Boolean = false
    private var isMovingAllowed: Boolean = false
    private lateinit var executorService: ScheduledExecutorService
    private val widgets = mutableListOf<AbstractWidget>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        addButton.tooltip = Tooltip("???????????????? ????????????")
        dataButton.tooltip = Tooltip("???????????? ??????????????????")
        movingButton.tooltip = Tooltip("?????????????????????? ????????????????")
        accountButton.tooltip = Tooltip("??????????????")
        reloadButton.tooltip = Tooltip("???????????????? ????????????")
        settingButton.tooltip = Tooltip("??????????????????")

        queriesDB = QueriesDB()

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
        executorService.scheduleAtFixedRate(
            this::schedule,
            1,
            user.getTimer().toLong(),
            TimeUnit.MINUTES
        )
    }

    private fun schedule() {
        Platform.runLater {
            val address = request.addressGeneration(ADDRESS, OBJECTS)
            val values = valuesObject(objectData.getNameObject(), objects, address)
            if (values != null && indicatorPane.isVisible) {
                val data = objectsData(objectData.getNameObject(), objects, address, values)
                widgets.forEach {
                    if (it !is ChartWidget) {
                        val indicator = queriesDB.selectWidget(
                            user.getId(),
                            objectData.getIdObject(),
                            INDICATOR,
                            it.getLayoutX(),
                            it.getLayoutY()
                        )
                        if (indicator != null && data != null) {
                            it.setValue(data[indicator.getIdentifier()].toString())
                        }
                    }
                }
            }
        }
    }


    private fun imageViewVisible(visible: Boolean) {
        if (!visible) {
            indicatorButton.isVisible = false
            chartButton.isVisible = false
        } else {
            indicatorButton.isVisible = false
            chartButton.isVisible = true
        }
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
     * ????????????????????/?????????????? ???????????? ??????????????????
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

    @FXML
    private fun refreshMenuClick() {
        objects()
    }

    private fun errorMessage(message: String): Boolean =
        if (message == "401 Unauthorized" || message == "403 Forbidden" || message == "404 Not Found" || message == "No connection") {
            indicatorPane.children.remove(0, indicatorPane.children.size)
            chartPane.children.remove(0, chartPane.children.size)
            indicatorPane.isVisible = true
            chartPane.isVisible = false
            indicatorButton.isVisible = false
            chartButton.isVisible = false
            devicesList.items = FXCollections.observableArrayList(listOf<String>())
            imageViewVisible(false)
            alarmOrInfo("????????????", message)
            true
        } else false

    /**
     * ?????????????????? ?????? ?????????????? ?? ?????????????????????????? ?? ?????????????????? ?? ???? ???????? ???? ?????? ??????
     */
    private fun objects() {
        devicesList.items.remove(0, devicesList.items.size)
        val strObjects = request.getRequest(request.addressGeneration(ADDRESS, OBJECTS))
        if (!errorMessage(strObjects)) {
            val jsonObjects = Gson().fromJson(strObjects, JsonArray::class.java)
            objects = processingJSON.readAllObjects(jsonObjects) as MutableList<Object>
            showObject()
        }
    }

    /**
     * ?????????????? ???????????? ??????????????????
     */
    private fun showObject() {
        val address = request.addressGeneration(ADDRESS, OBJECTS)
        val nameListObjects = ArrayList<String>()
        objects.forEach { nameListObjects.add(it.getNameObject()) }
        val namesObjects = FXCollections.observableArrayList(nameListObjects)
        devicesList.items = namesObjects

        devicesList.onMouseClicked = EventHandler {
            imageViewVisible(true)
            val newValue = devicesList.selectionModel.selectedItem
            if (newValue != null) {
                indicatorPane.children.remove(0, indicatorPane.children.size)
                chartPane.children.remove(0, chartPane.children.size)
                indicatorPane.isVisible = true
                chartPane.isVisible = false
                var objDB: Object? = null
                objects.forEach { if (it.getNameObject() == newValue) objDB = it }

                if (objDB != null) {
                    objectData = objDB as Object
                }

                widgets.removeAll(widgets)

                val values = valuesObject(newValue, objects, address)
                if (values != null) {
                    val data = objectsData(newValue, objects, address, values)
                    if (data != null) {
                        uploadObjects(data)
                        initializeScheduler()
                        devicesClick()
                    }
                }
            }
        }
    }

    @FXML
    private fun settingChartClick(
        chartWidget: ChartWidget,
        data: List<List<Number>>,
        chart: Widget
    ) {
        val fxmlLoader = FXMLLoader(fxmlLoader("settingChart.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "?????????????????? ??????????????????", false)

        val controller: SettingChartController = fxmlLoader.getController()

        val panel = chartWidget.getPanel()
        controller.load(panel.layoutX, panel.layoutY, chart)

        stage.showAndWait()
        if (controller.delete) {
            chartPane.children.remove(panel)
            widgets.remove(chartWidget)
            queriesDB.deleteWidget(user.getId(), objectData.getIdObject(), CHART, panel.layoutX, panel.layoutY)
        } else if (controller.save) {
            val dataWidget = controller.dataWidget
            if (chart.getType() != dataWidget.getType()) {
                widgets.forEach {
                    if (it.getId() == chartWidget.getId() && it is ChartWidget) {
                        it.updateType(dataWidget.getType())
                        chart.setType(dataWidget.getType())
                    }
                }
            }
            updateObject(chart.getId(), dataWidget.getName(), dataWidget.getUnit(), dataWidget.getType())

            var title = ""
            title += dataWidget.getName()
            if (title.isNotEmpty() && dataWidget.getUnit().isNotEmpty()) title += ", "
            title += dataWidget.getUnit()
            chart.setName(dataWidget.getName())
            chart.setUnit(dataWidget.getUnit())
            chartWidget.setTitle(title)
            if (dataWidget.getDate().isNotEmpty()) {
                val series = XYChart.Series<String, Number>()
                val simpleDateFormat = SimpleDateFormat(DATA_FORMAT)
                val simpleDate = simpleDateFormat.format(Date(dataWidget.getDate().toLong()))
                series.name = simpleDate
                chartWidget.getChart().data.remove(0, chartWidget.getChart().data.size)
                chartWidget.getChart().data.add(series)
                val cData = chartData(chart.getIdentifier(), objectData.getIdObject())
                if (cData != null) {
                    cData.forEach {
                        val time = Date(it[0].toLong())

                        if (simpleDateFormat.format(time) == simpleDate) {
                            val simpleDateFormatOther = SimpleDateFormat(TIME_FORMAT)
                            val thisTime = LocalTime.parse(simpleDateFormatOther.format(time))
                            if (dataWidget.getFrom().isNotEmpty()) {
                                val fromTime = LocalTime.parse(dataWidget.getFrom())
                                if (thisTime.isAfter(fromTime)) {
                                    if (dataWidget.getTo().isNotEmpty()) {
                                        val toTime = LocalTime.parse(dataWidget.getTo())

                                        if (thisTime.isBefore(toTime)) {
                                            series.data.add(
                                                XYChart.Data(
                                                    simpleDateFormatOther.format(time),
                                                    it[1].toDouble()
                                                )
                                            )
                                        }

                                    } else {
                                        series.data.add(
                                            XYChart.Data(
                                                simpleDateFormatOther.format(time),
                                                it[1].toDouble()
                                            )
                                        )
                                    }
                                }
                            } else if (dataWidget.getTo().isNotEmpty()) {
                                val toTime = LocalTime.parse(dataWidget.getTo())

                                if (thisTime.isBefore(toTime)) {
                                    series.data.add(
                                        XYChart.Data(
                                            simpleDateFormatOther.format(time),
                                            it[1].toDouble()
                                        )
                                    )
                                }

                            } else {
                                series.data.add(
                                    XYChart.Data(
                                        simpleDateFormatOther.format(time),
                                        it[1].toDouble()
                                    )
                                )
                            }
                        }
                    }
                    chartWidget.updateChart(series)
                }
            }
        }
    }

    @FXML
    private fun settingIndicatorClick(
        indicator: Widget,
        widget: AbstractWidget,
    ): AbstractWidget {
        val fxmlLoader = FXMLLoader(fxmlLoader("settingIndicator.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "?????????????????? ????????????????????", false)
        val controller: SettingIndicatorController = fxmlLoader.getController()
        if (controller.load(objectData.getIdModel(), indicator)) {
            stage.showAndWait()
            if (!errorMessage(controller.message)) {
                if (controller.delete) {
                    indicatorPane.children.remove(widget.getPanel())
                    queriesDB.deleteWidget(
                        user.getId(),
                        objectData.getIdObject(),
                        INDICATOR,
                        indicator.getLayoutX(),
                        indicator.getLayoutY()
                    )
                } else if (controller.save) {
                    val dataWidget = controller.dataWidget
                    if (dataWidget != null) {
                        updateObject(indicator.getId(), dataWidget.getName(), dataWidget.getUnit(), "")
                        widget.setTitle(dataWidget.getName())
                        indicator.setName(dataWidget.getName())
                        if (widget is NumericWidget) {
                            widget.setUnit(dataWidget.getUnit())
                            indicator.setUnit(dataWidget.getUnit())
                        }

                        val address = request.addressGeneration(ADDRESS, MODELS)
                        var obj: Object? = null
                        objects.forEach {
                            if (it.getIdObject() == objectData.getIdObject()) obj = it
                        }
                        val strModel = request.getRequest(
                            request.addressGeneration(
                                address,
                                obj!!.getIdModel()
                            )
                        )
                        widget.setColor(strModel)
                    }
                }
            }
        } else {
            stage.close()
            errorMessage(controller.message)
        }
        return widget
    }

    private fun updateObject(id: Int, nameText: String, unitText: String, type: String) {
        if (indicatorPane.isVisible) {
            queriesDB.updateWidget(id, INDICATOR, WidgetsTable.NAME.name, nameText)
            queriesDB.updateWidget(id, INDICATOR, WidgetsTable.UNIT.name, unitText)
        }
        if (chartPane.isVisible) {
            queriesDB.updateWidget(id, CHART, WidgetsTable.NAME.name, nameText)
            queriesDB.updateWidget(id, CHART, WidgetsTable.UNIT.name, unitText)
            if (type.isNotEmpty()) {
                queriesDB.updateWidget(id, CHART, WidgetsTable.TYPE.name, type)
            }
        }
    }

    private fun mouseDraggedPanel(panel: AnchorPane) {
        var layoutX: Double = panel.layoutX
        var layoutY: Double = panel.layoutY
        var anchorX = 0.0
        var anchorY = 0.0
        panel.setOnMousePressed { event ->
            layoutX = panel.layoutX
            layoutY = panel.layoutY
            anchorX = panel.layoutX - event.sceneX
            anchorY = panel.layoutY - event.sceneY
            panel.toFront()
        }
        panel.setOnMouseDragged { event ->
            if (isMovingAllowed) {
                panel.layoutX = event.sceneX + anchorX
                panel.layoutY = event.sceneY + anchorY
            }
        }
        panel.setOnMouseReleased {
            if (isMovingAllowed) {
                if (panel.prefHeight == Pref.CHART.size) updatePositionChart(layoutX, layoutY, panel)
                else updatePositionIndicator(layoutX, layoutY, panel)
            }
        }
    }

    /**
     * ?????????????? ????????????
     * @addObject - ?????? ??????????????
     * @nameText - ????????????????
     * @unitText - ?????????????? ??????????????????
     * @typeWidget - ?????? ??????????????
     */
    private fun addObjects(addWidget: WidgetDesigner) {
        if (indicatorPane.isVisible) {
            val pos = getPositionIndicator()
            if (pos[0] != -1.0 || pos[1] != -1.0) {
                val address = request.addressGeneration(ADDRESS, OBJECTS)
                var obj: Object? = null
                objects.forEach {
                    if (it.getIdObject() == objectData.getIdObject()) obj = it
                }
                val data = objectData(addWidget.getWidget(), obj!!.getIdObject(), address)
                queriesDB.insertIntoWidget(
                    Widget(
                        null,
                        user.getId(),
                        objectData.getIdObject(),
                        INDICATOR,
                        addWidget.getWidget(),
                        pos[0],
                        pos[1],
                        addWidget.getName(),
                        addWidget.getUnit(),
                        addWidget.getType()
                    )
                )
                val indicator =
                    queriesDB.selectWidget(user.getId(), objectData.getIdObject(), INDICATOR, pos[0], pos[1])
                if (indicator != null) {
                    addIndicatorToPanel(indicator, data.toString())
                }
            }
        }
        if (chartPane.isVisible) {
            val pos = getPositionChart()
            if (pos[0] != -1.0 || pos[1] != -1.0) {
                queriesDB.insertIntoWidget(
                    Widget(
                        null,
                        user.getId(),
                        objectData.getIdObject(),
                        CHART,
                        addWidget.getWidget(),
                        pos[0],
                        pos[1],
                        addWidget.getName(),
                        addWidget.getUnit(),
                        addWidget.getType()
                    )
                )
                val chart = queriesDB.selectWidget(user.getId(), objectData.getIdObject(), CHART, pos[0], pos[1])
                if (chart != null) {
                    addChartToPanel(chart)
                }
            }
        }
    }

    private fun addIndicatorToPanel(indicator: Widget, data: String): Boolean {
        val address = request.addressGeneration(ADDRESS, MODELS)
        var obj: Object? = null
        objects.forEach {
            if (it.getIdObject() == objectData.getIdObject()) obj = it
        }
        val strModel = request.getRequest(
            request.addressGeneration(
                address,
                obj!!.getIdModel()
            )
        )
        if (!errorMessage(strModel)) {
            val panel = when (indicator.getType()) {
                TypeIndicator.NUMBER.type -> {
                    NumericWidget(
                        indicator.getId(),
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.size,
                        indicator.getName(),
                        indicator.getUnit(),
                        data,
                        indicator.getIdentifier(),
                        indicator.getType(),
                        strModel
                    )
                }
                TypeIndicator.STRING.type -> {
                    StringWidget(
                        indicator.getId(),
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.size,
                        indicator.getName(),
                        data,
                        indicator.getIdentifier(),
                        strModel
                    )
                }
                TypeIndicator.BOOLEAN.type -> {
                    BooleanWidget(
                        indicator.getId(),
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.size,
                        indicator.getName(),
                        data,
                        indicator.getIdentifier(),
                        strModel
                    )
                }
                else -> {
                    StringWidget(
                        indicator.getId(),
                        indicator.getLayoutX(),
                        indicator.getLayoutY(),
                        Pref.INDICATOR.size,
                        indicator.getName(),
                        data,
                        indicator.getIdentifier(),
                        strModel
                    )
                }
            }
            mouseDraggedPanel(panel.getPanel())
            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                settingIndicatorClick(
                    indicator,
                    panel
                )
            }
            indicatorPane.children.add(panel.getPanel())
            widgets.add(panel)
            return true
        }
        return false
    }

    private fun addChartToPanel(chart: Widget): Boolean {
        val cData = chartData(chart.getIdentifier(), objectData.getIdObject())
        return if (cData != null) {
            val title = if (chart.getUnit().isNotEmpty())
                "${chart.getName()}, ${chart.getUnit()}"
            else chart.getName()
            val panel = ChartWidget(
                chart.getId(),
                chart.getLayoutX(),
                chart.getLayoutY(),
                Pref.CHART.size,
                title,
                cData,
                chart.getType()
            )
            mouseDraggedPanel(panel.getPanel())
            val setting = panel.getSetting()
            setting.onMouseClicked = EventHandler {
                settingChartClick(
                    panel,
                    cData,
                    chart
                )
            }
            widgets.add(panel)
            chartPane.children.add(panel.getPanel())
            true
        } else false
    }

    /**
     * ?????????????????? ?????????????? ???? ????
     * @data - ????????????
     */
    private fun uploadObjects(data: Map<String, String>) {
        val indicators = queriesDB.selectWidgets(user.getId(), objectData.getIdObject(), INDICATOR)
        if (indicators != null) {
            var flag = true
            for (indicator in indicators) {
                if (indicator.getIdObject() == objectData.getIdObject() && !positionFree(
                        indicator.getLayoutX(), indicator.getLayoutY()
                    )
                ) flag = addIndicatorToPanel(indicator, data[indicator.getIdentifier()].toString())
                if (!flag) break
            }
        }
        val charts = queriesDB.selectWidgets(user.getId(), objectData.getIdObject(), CHART)
        if (charts != null) {
            var flag = true
            for (chart in charts) {
                if (chart.getIdObject() == objectData.getIdObject() && !positionFree(
                        chart.getLayoutX(), chart.getLayoutY()
                    )
                ) flag = addChartToPanel(chart)
                if (!flag) break
            }
        }
    }

    /**
     * ?????????????????? ???????????????? ???????????? ?? ??????????????
     * @name - ?????? ??????????????
     * @listObjects - ???????????? ???????? ????????????????
     * @addressAll - ??????????
     */
    private fun valuesObject(name: String, listObjects: List<Object>, addressAll: String): List<String>? {
        val values = mutableListOf<String>()
        listObjects.forEach { it ->
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
                            modelState.forEach {
                                val pars = processingJSON.read(json, it)?.asString
                                if (pars != null) values.add(it)
                            }
                        }
                    }
                } else return null
            }
        }
        return values
    }

    /**
     * ?????????????????? ?????? ???????????????????? ?????? ???????? ????????????
     * @name - ?????? ??????????????
     * @listObjects - ???????????? ???????? ????????????????
     * @addressAll - ??????????
     * @dataObject - ???????????? ??????????????
     */
    private fun objectsData(
        name: String, listObjects: List<Object>, addressAll: String, dataObject: List<String>
    ): Map<String, String>? {
        val data: MutableMap<String, String> = mutableMapOf()
        listObjects.forEach { it ->
            if (name == it.getNameObject()) {
                val str = request.getRequest(request.addressGeneration(addressAll, it.getIdObject()))
                if (!errorMessage(str)) {
                    var json = Gson().fromJson(str, JsonObject::class.java)
                    val state = processingJSON.read(json, STATE)
                    if (state != null) {
                        json = Gson().fromJson(state, JsonObject::class.java)
                        dataObject.forEach {
                            val pars = processingJSON.read(json, it)?.asString
                            if (pars != null) data[it] = pars
                        }
                    }
                } else return null
            }
        }
        return data
    }

    //?????????????????? ???????????????????? ?????? ?????????????????????????? ??????????????
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
        val title: String
        val fxmlLoader = if (indicatorPane.isVisible) {
            title = "???????????????? ??????????????????"
            FXMLLoader(fxmlLoader("addWidgetIndicator.fxml"))
        } else {
            title = "???????????????? ??????????????????"
            FXMLLoader(fxmlLoader("addWidgetChart.fxml"))
        }
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, title, false)
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
            if (it.layoutX + it.prefWidth(0.0) < 1235.0 && it.prefWidth(0.0) == Pref.INDICATOR.size)
                layoutX = it.layoutX + it.prefWidth(0.0) + 5.0
            else if (it.layoutY + it.prefHeight(0.0) < 416.0 && it.prefWidth(0.0) == Pref.INDICATOR.size) {
                layoutX = 0.0
                layoutY = it.layoutY + it.prefHeight(0.0) + 5.0
            } else {
                alarmOrInfo("????????????????", "???? ???????????????? ?????????????????????? ??????????")
                layoutX = -1.0
                layoutY = -1.0
            }
        }
        return mutableListOf(layoutX, layoutY)
    }

    private fun alarmOrInfo(title: String, message: String) {
        val fxmlLoader = FXMLLoader(fxmlLoader("alarmOrInfo.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, title, false)
        val controller: AlarmOrInfoController = fxmlLoader.getController()
        controller.load(message)
        stage.showAndWait()
    }

    private fun getPositionChart(): List<Double> {
        var layoutX = 0.0
        var layoutY = 0.0
        chartPane.children.forEach {
            if (it.layoutX + it.prefWidth(0.0) < 1226.0 && it.prefWidth(0.0) == Pref.CHART.size)
                layoutX = it.layoutX + it.prefWidth(0.0) + 5.0
            else if (it.layoutY + it.prefHeight(0.0) < 311.0 && it.prefWidth(0.0) == Pref.CHART.size) {
                layoutX = 0.0
                layoutY = it.layoutY + it.prefHeight(0.0) + 5.0
            } else {
                alarmOrInfo("????????????????", "???? ???????????????? ?????????????????????? ??????????")
                layoutX = -1.0
                layoutY = -1.0
            }
        }
        return mutableListOf(layoutX, layoutY)
    }

    private fun positionFree(layoutX: Double, layoutY: Double): Boolean {
        if (indicatorPane.isVisible) {
            indicatorPane.children.forEach {
                if (it.layoutX == layoutX && it.layoutY == layoutY)
                    return true
            }
        }
        if (chartPane.isVisible) {
            chartPane.children.forEach {
                if (it.layoutX == layoutX && it.layoutY == layoutY)
                    return true
            }
        }
        return false
    }

    @FXML
    private fun reloadClick() {
        if (indicatorPane.isVisible) {
            val length = indicatorPane.children.size
            if (length > 0)
                indicatorPane.children.remove(0, length)
        }
        if (chartPane.isVisible) {
            val length = chartPane.children.size
            if (length > 0)
                chartPane.children.remove(0, length)
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
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "??????????????", false)

        val token = user.getToken()
        val addressDev = user.getAddress()

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
        }
        if (controller.exit) {
            queriesDB.updateUser(user.getId(), UsersTable.CASTLE.name, false.toString())
            var newStage: Stage = dataButton.scene.window as Stage
            newStage.close()
            val newFxmlLoader = FXMLLoader(fxmlLoader("loginWindow.fxml"))
            newStage = createStage(newFxmlLoader, Modality.APPLICATION_MODAL, "????????", false)
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

    private fun updatePositionIndicator(layoutX: Double, layoutY: Double, panel: AnchorPane) {
        val newPos = shift(panel.layoutX, panel.layoutY, Pref.INDICATOR.size)
        val indicator = queriesDB.selectWidget(user.getId(), objectData.getIdObject(), INDICATOR, layoutX, layoutY)
        if (positionFree(newPos[0], newPos[1])) {
            val anotherIndicator = queriesDB.selectWidget(user.getId(), objectData.getIdObject(), INDICATOR, newPos[0], newPos[1])
            if (anotherIndicator != null) {
                widgets.forEach {
                    if (it.getId() == anotherIndicator.getId()) {
                        it.setLayoutX(layoutX)
                        it.setLayoutY(layoutY)
                    }
                }
                queriesDB.updateWidget(anotherIndicator.getId(), INDICATOR, WidgetsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateWidget(anotherIndicator.getId(), INDICATOR, WidgetsTable.LAYOUT_Y.name, layoutY.toString())
            }
        }
        if (indicator != null) {
            widgets.forEach {
                if (it.getId() == indicator.getId()) {
                    it.setLayoutX(newPos[0])
                    it.setLayoutY(newPos[1])
                }
            }
            panel.layoutX = newPos[0]
            panel.layoutY = newPos[1]
            queriesDB.updateWidget(indicator.getId(), INDICATOR, WidgetsTable.LAYOUT_X.name, newPos[0].toString())
            queriesDB.updateWidget(indicator.getId(), INDICATOR, WidgetsTable.LAYOUT_Y.name, newPos[1].toString())
        }
    }

    private fun updatePositionChart(layoutX: Double, layoutY: Double, panel: AnchorPane) {
        val newPos = shift(panel.layoutX, panel.layoutY, Pref.CHART.size)
        val chart = queriesDB.selectWidget(user.getId(), objectData.getIdObject(), CHART, layoutX, layoutY)

        if (positionFree(newPos[0], newPos[1])) {
            val anotherChart =
                queriesDB.selectWidget(user.getId(), objectData.getIdObject(), CHART, newPos[0], newPos[1])
            if (anotherChart != null) {
                widgets.forEach {
                    if (it.getId() == anotherChart.getId()) {
                        it.setLayoutX(layoutX)
                        it.setLayoutY(layoutY)
                    }
                }
                queriesDB.updateWidget(anotherChart.getId(), CHART, WidgetsTable.LAYOUT_X.name, layoutX.toString())
                queriesDB.updateWidget(anotherChart.getId(), CHART, WidgetsTable.LAYOUT_Y.name, layoutY.toString())
            }
        }
        if (chart != null) {
            widgets.forEach {
                if (it.getId() == chart.getId()) {
                    it.setLayoutX(newPos[0])
                    it.setLayoutY(newPos[1])
                }
            }
            panel.layoutX = newPos[0]
            panel.layoutY = newPos[1]
            queriesDB.updateWidget(chart.getId(), CHART, WidgetsTable.LAYOUT_X.name, newPos[0].toString())
            queriesDB.updateWidget(chart.getId(), CHART, WidgetsTable.LAYOUT_Y.name, newPos[1].toString())
        }
    }

    private fun shift(translateX: Double, translateY: Double, step: Double): List<Double> {
        var posX = 0.0
        var posY = 0.0
        while (translateX - posX > step)
            posX += step + 5
        if (translateX - posX > step / 2)
            posX += step + 5
        while (translateY - posY > step)
            posY += step + 5
        if (translateY - posY > step / 2)
            posY += step + 5
        return listOf(posX, posY)
    }

    @FXML
    private fun clickSetting() {
        val oldTheme = THEME
        val fxmlLoader = FXMLLoader(fxmlLoader("setting.fxml"))
        val stage = createStage(fxmlLoader, Modality.WINDOW_MODAL, "??????????????????", false)

        val controller: SettingController = fxmlLoader.getController()
        controller.loader(user)
        stage.showAndWait()

        if (THEME != oldTheme) {
            queriesDB.updateUser(user.getId(), UsersTable.THEME.name, THEME)
            updateTheme()
        }
        if (controller.save)
            queriesDB.updateUser(user.getId(), UsersTable.TIMER.name, controller.user.getTimer().toString())
    }

    @FXML
    private fun movingClick() {
        val imageView = if (isMovingAllowed) {
            initializeScheduler()
            isMovingAllowed = false
            createImageView("lock", 30.0)
        } else {
            executorService.shutdown()
            isMovingAllowed = true
            createImageView("unlock", 30.0)
        }
        movingButton.graphic = imageView
    }
}