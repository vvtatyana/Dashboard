package com.example.widget

import com.example.database.QueriesDB
import com.example.restAPI.ProcessingJSON
import com.example.restAPI.RequestGeneration
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.hansolo.medusa.Gauge
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

class NumericWidget(
    private val objectData: com.example.building.Object,
    indicator: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val unitText: String,
    private val data: String?,
    private val typeWidget: String,
    private val queriesDB: QueriesDB
) : AbstractWidget(indicator, layoutX, layoutY, pref, name) {

    private val request = RequestGeneration()
    private val processingJSON = ProcessingJSON()
    private var gauge: Gauge

    init {
        gauge = createGauge()
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(gauge)
        panel.children.add(setting)
    }

    private fun createGauge(): Gauge {
        val address = request.addressGeneration(DEFAULT_ADDRESS, MODELS)
        val strModel = request.addressAssemblyGET(
            address, queriesDB.selectObject(
                ObjectsTable.ID.name, objectData.getId().toString()
            )!!.getIdModel()
        )
        val jsonModel = Gson().fromJson(strModel, JsonObject::class.java)
        val borderFrom = processingJSON.readBorderFrom(jsonModel, typeWidget).toMutableMap()
        val borderTo = processingJSON.readBorderTo(jsonModel, typeWidget).toMutableMap()
        val borderColor = processingJSON.readBorderColor(jsonModel, typeWidget).toMutableMap()
        val keys = borderColor.keys.toList()
        if (borderFrom[keys[0]]!!.toDouble() < 0.0)
            borderFrom[keys[0]] = (borderFrom[keys[0]]!!.toDouble() + 10.0).toString()

        val gaugeBuilder = GaugeBuilder.create()
            .skinType(Gauge.SkinType.SIMPLE)
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

    fun setValue(newValue: Double) {
        gauge.value = newValue
    }

    fun setUnit(unit: String) {
        gauge.title = unit
    }
}