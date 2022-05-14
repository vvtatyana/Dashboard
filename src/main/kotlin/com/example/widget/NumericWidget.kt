package com.example.widget

import com.example.restAPI.ProcessingJSON
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.hansolo.medusa.Gauge
import eu.hansolo.medusa.GaugeBuilder
import eu.hansolo.medusa.Section
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color

class NumericWidget(
    layoutX: Double,
    layoutY: Double,
    pref: Double,
    name: String,
    private val unitText: String,
    private val data: String?,
    private val typeWidget: String,
    private val strModel: String
) : AbstractWidget(layoutX, layoutY, pref, name) {

    private val processingJSON = ProcessingJSON()
    private var gauge: Gauge
    private var type: Boolean = false
    private var rang: Double = 0.0

    init {
        gauge = createGauge()
        setting = createSetting()
        panel.children.add(title)
        panel.children.add(gauge)
        panel.children.add(setting)
    }

    private fun createGauge(): Gauge {
        val jsonModel = Gson().fromJson(strModel, JsonObject::class.java)
        type = processingJSON.typeNumeric(jsonModel, typeWidget)

        val borderFrom = processingJSON.readBorderFrom(jsonModel, typeWidget).toMutableMap()
        val borderColor = processingJSON.readBorderColor(jsonModel, typeWidget).toMutableMap()
        val keys = borderColor.keys.toList()
        if (borderFrom.isNotEmpty() && borderFrom[keys[0]]!!.toDouble() < 0.0)
            borderFrom[keys[0]] = (borderFrom[keys[0]]!!.toDouble() + 10.0).toString()
        val gaugeBuilder = GaugeBuilder.create()
            .skinType(Gauge.SkinType.SIMPLE)
            .sectionsVisible(true)
            .title(unitText)
            .animated(true)
            .minValue(borderFrom[keys[0]]!!.toDouble())
        val sections = mutableListOf<Section>()
        borderFrom[keys[0]] = gaugeBuilder.build().minValue.toInt().toString()
        if (type) {
            val borderTo = processingJSON.readBorderTo(jsonModel, typeWidget).toMutableMap()

            gaugeBuilder.tickLabelDecimals(1)
                .decimals(1)
                .maxValue(borderTo[keys[keys.size - 1]]!!.toDouble())

            keys.indices.forEach {
                sections.add(
                    Section(borderFrom[keys[it]]!!.toDouble(),
                        borderTo[keys[it]]!!.toDouble(),
                        it.toString(),
                        Color.web(borderColor[keys[it]])))
            }
        } else {
            gaugeBuilder.maxValue(borderFrom[keys[keys.size - 1]]!!.toDouble()).sectionTextVisible(true)
            rang = (borderFrom[keys[keys.size - 1]]!!.toDouble() - borderFrom[keys[0]]!!.toDouble()) / keys.size
            val c = borderColor.values.toList()
            var start = borderFrom[keys[0]]!!.toDouble()
            for (y in keys.indices) {
                val stop: Double = start + rang
                sections.add(Section(start,stop,borderFrom[keys[y]]!!.toString(),Color.web(c[y])))
                start = stop
            }
        }

        val gauge = gaugeBuilder.sections(sections).build()
        if (data != null) {
            gauge.value = if (type) data.toDouble()
            else data.toDouble()
        }
        AnchorPane.setTopAnchor(gauge, 27.0)
        AnchorPane.setBottomAnchor(gauge, 19.0)
        AnchorPane.setRightAnchor(gauge, 5.0)
        AnchorPane.setLeftAnchor(gauge, 5.0)
        return gauge
    }

    fun setValue(newValue: Double) {
        gauge.value = if (type) newValue
        else newValue
    }

    fun setUnit(unit: String) {
        gauge.title = unit
    }
}