package com.example.widget

import com.example.restAPI.ProcessingJSON
import com.example.util.TypeIndicator
import com.example.util.dropShadow
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.hansolo.medusa.Gauge
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

class NumericWidget(
    id: Int,
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val unitText: String,
    private val data: String?,
    private val typeWidget: String,
    private val typeData: String,
    private val strModel: String
) : AbstractWidget(id, layoutX, layoutY, pref, name) {

    private val processingJSON = ProcessingJSON()
    private var gauge: Gauge
    private var rang: Double = 0.0

    init {
        gauge = createGauge()
        AnchorPane.setTopAnchor(gauge, 27.0)
        AnchorPane.setBottomAnchor(gauge, 19.0)
        AnchorPane.setRightAnchor(gauge, 5.0)
        AnchorPane.setLeftAnchor(gauge, 5.0)
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(gauge)
        panel.children.add(setting)
    }

    private fun createGauge(): Gauge {
        val jsonModel = Gson().fromJson(strModel, JsonObject::class.java)
        val borderFrom = processingJSON.readBorderFrom(jsonModel, typeWidget).toMutableMap()
        val borderColor = processingJSON.readBorderColor(jsonModel, typeWidget).toMutableMap()
        val keys = borderFrom.keys.toList()
        if (borderFrom.isNotEmpty() && borderFrom[keys[0]]!!.toDouble() < 0.0)
            borderFrom[keys[0]] = (borderFrom[keys[0]]!!.toDouble() + 10.0).toString()
        val gaugeBuilder = gaugeBuilder().minValue(borderFrom[keys[0]]!!.toDouble())
        val sections = mutableListOf<Section>()
        borderFrom[keys[0]] = gaugeBuilder.build().minValue.toInt().toString()
        if (typeData == TypeIndicator.NUMBER.type) {
            val borderTo = processingJSON.readBorderTo(jsonModel, typeWidget).toMutableMap()
            gaugeBuilder.tickLabelDecimals(1).decimals(1).maxValue(borderTo[keys[keys.size - 1]]!!.toDouble())
            keys.indices.forEach { sections.add(Section(borderFrom[keys[it]]!!.toDouble(),
                borderTo[keys[it]]!!.toDouble(), it.toString(), Color.web(borderColor[keys[it]])))}
        } else {
            gaugeBuilder.maxValue(borderFrom[keys[keys.size - 1]]!!.toDouble()).sectionTextVisible(true)
            rang = (borderFrom[keys[keys.size - 1]]!!.toDouble() - borderFrom[keys[0]]!!.toDouble()) / keys.size
            val colorValue = borderColor.values.toList()
            var start = borderFrom[keys[0]]!!.toDouble()
            keys.indices.forEach {
                val stop: Double = start + rang
                sections.add(Section(start,stop,borderFrom[keys[it]]!!.toString(),Color.web(colorValue[it])))
                start = stop
            } }
        val gauge = gaugeBuilder.sections(sections).build()
        gauge.effect = dropShadow()
        gauge.tickLabelColor = Color.web("#9ba7c5")
        if (data != null)
            gauge.value = data.toDouble()
        return gauge
    }

    private fun gaugeBuilder(): GaugeBuilder<*> = GaugeBuilder.create()
        .skinType(Gauge.SkinType.SIMPLE)
        .sectionsVisible(true)
        .title(unitText)
        .animated(true)

    fun number(){}

    fun setValue(newValue: Double) {
        gauge.value = if (typeData == TypeIndicator.NUMBER.type) newValue
        else newValue
    }

    fun setUnit(unit: String) {
        gauge.title = unit
    }
}