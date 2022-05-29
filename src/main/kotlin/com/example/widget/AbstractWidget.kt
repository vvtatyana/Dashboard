package com.example.widget

import com.example.util.*
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane

abstract class AbstractWidget(
    private val id: Int,
    private var layoutX: Double,
    private var layoutY: Double,
    private val size: Double,
    private val name: String
) {
    protected var panel: AnchorPane = createPanel()
    protected var title: Label = createTitle()
    protected var setting: Button = createSetting()

    private fun createPanel(): AnchorPane {
        val panel = AnchorPane()
        panel.id = "pane"
        panel.layoutX = layoutX
        panel.layoutY = layoutY
        panel.prefWidth = size
        panel.prefHeight = size
        panel.effect = dropShadow()
        return panel
    }

    private fun createTitle(): Label {
        val nameLabel = Label(name)
        nameLabel.alignment = Pos.CENTER
        nameLabel.style = Decoration.NAME.style
        AnchorPane.setRightAnchor(nameLabel, 5.0)
        AnchorPane.setLeftAnchor(nameLabel, 5.0)
        return nameLabel
    }

    protected fun createSetting(): Button {
        val settingButton = Button()
        settingButton.graphic = createImageView("setting", 25.0)
        settingButton.prefHeight = 25.0
        settingButton.prefWidth = 25.0
        AnchorPane.setRightAnchor(settingButton, -5.0)
        AnchorPane.setBottomAnchor(settingButton, 0.0)
        return settingButton
    }

    @JvmName("panel")
    fun getPanel(): AnchorPane = panel

    @JvmName("title")
    fun getTitle(): Label = title

    fun setTitle(newTitle: String) { title.text = newTitle }

    @JvmName("setting")
    fun getSetting(): Button = setting

    fun getLayoutX(): Double = layoutX

    fun getLayoutY(): Double = layoutY

    fun getId(): Int = id

    fun setLayoutX(newValue: Double){
        layoutX = newValue
        panel.layoutX = newValue
    }

    fun setLayoutY(newValue: Double){
        layoutY = newValue
        panel.layoutY = newValue
    }

    open fun setColor(strModel: String){}
}