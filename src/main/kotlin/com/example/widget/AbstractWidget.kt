package com.example.widget

import com.example.util.*
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import java.io.FileInputStream

abstract class AbstractWidget(
    private val indicator: Int,
    private val layoutX: Double,
    private val layoutY: Double,
    private val size: Double,
    private val name: String
) {
    protected var panel: AnchorPane = createPanel()
    protected var title: Label = createTitle()
    protected var setting: ImageView = createSetting()

    private fun createPanel(): AnchorPane {
        val panel = AnchorPane()
        panel.style = panelTheme()
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
        nameLabel.effect = dropShadow()
        AnchorPane.setRightAnchor(nameLabel, 5.0)
        AnchorPane.setLeftAnchor(nameLabel, 5.0)
        return nameLabel
    }

    protected fun createSetting(): ImageView {
        val setting = ImageView(Image(FileInputStream(Decoration.SETTING.style)))
        setting.fitWidth = Decoration.SETTING.pref
        setting.fitHeight = Decoration.SETTING.pref

        setting.layoutX = size - Decoration.SETTING.layoutX
        setting.layoutY = size - Decoration.SETTING.layoutY
        setting.effect = dropShadow()

        return setting
    }

    @JvmName("getPanel1")
    fun getPanel(): AnchorPane = panel

    @JvmName("getTitle1")
    fun getTitle(): Label = title

    fun setTitle(newTitle: String) {
        title.text = newTitle
    }

    @JvmName("getSetting1")
    fun getSetting(): ImageView = setting

    fun getIndicator(): Int = indicator

    fun updateColor() {
        panel.style = panelTheme()
        title.style = textStyle(16)
    }
}