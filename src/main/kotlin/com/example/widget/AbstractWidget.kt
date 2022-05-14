package com.example.widget

import com.example.util.*
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import java.io.FileInputStream

abstract class AbstractWidget(
    private val layoutX: Double,
    private val layoutY: Double,
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
}