package com.example.database

import com.example.building.*
import com.example.util.TableBD
import java.sql.Connection
import java.sql.Statement
import kotlin.system.exitProcess

class QueriesDB(private val connection: Connection, private val statement: Statement) {

    private fun select(sql: String, column: Int): List<List<String>> {
        val result = mutableListOf<List<String>>()
        try {
            val rs = statement.executeQuery(sql)
            while (rs.next()) {
                val res = mutableListOf<String>()
                for (i in 1..column)
                    res.add(rs.getString(i))
                result.add(res)
            }
            rs.close()
        } catch (e: Exception) {
            System.err.println(sql + " " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
        return result
    }

    private fun insertUpdateDelete(sql: String) {
        try {
            statement.executeUpdate(sql)
            connection.commit()
        } catch (e: java.lang.Exception) {
            System.err.println(sql + " " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
    }

    fun selectUser(field: String, value: String): User? {
        val result = select("SELECT * from USERS where $field='$value'", TableBD.USERS.column)
        return if (result.isNotEmpty())
            User(
                result[0][0].toInt(), result[0][1], result[0][2], result[0][3], result[0][4], result[0][5],
                result[0][6], result[0][7].toBoolean(), result[0][8].toInt(), result[0][9], result[0][10].toInt()
            )
        else null
    }

    fun insertIntoUser(user: User) {
        insertUpdateDelete(
            "INSERT INTO USERS (ID_USER,USERNAME,LOGIN,PASSWORD,ADDRESS,TOKEN,CASTLE,ICON,THEME,TIMER)" +
                    "VALUES ('${user.getIdUser()}' , '${user.getUsername()}', '${user.getLogin()}', " +
                    "'${user.getPassword()}', '${user.getAddress()}', '${user.getToken()}', '${user.getCastle()}'," +
                    "'${user.getIcon()}', '${user.getTheme()}', '${user.getTimer()}');"
        )
    }

    fun updateUser(id: Int, field: String, value: String) {
        insertUpdateDelete("UPDATE USERS set $field='$value' where ID=$id")
    }

    fun selectObject(field: String, value: String): Object? {
        val result = select("SELECT * from OBJECTS where $field='$value'", TableBD.OBJECTS.column)
        return if (result.isNotEmpty())
            Object(result[0][0].toInt(), result[0][1], result[0][2].toInt(), result[0][3], result[0][4])
        else null
    }

    fun selectObjects(): List<Object>? {
        val resultList = select("SELECT * from OBJECTS", 5)
        return if (resultList.isNotEmpty()) {
            val objects = mutableListOf<Object>()
            resultList.forEach {
                objects.add(Object(it[0].toInt(), it[1], it[2].toInt(), it[3], it[4]))
            }
            objects
        } else null
    }

    fun insertIntoObject(obj: Object) {
        insertUpdateDelete(
            "INSERT INTO OBJECTS (ID_OBJECT,ID_USER,ID_MODEL,NAME_OBJECT)" +
                    "VALUES ('${obj.getIdObject()}' , '${obj.getIdUser()}', '${obj.getIdModel()}', '${obj.getNameObject()}');"
        )
    }

    fun selectIndicator(layoutX: Double, layoutY: Double, idObj: Int): Indicator? {
        val result = select("SELECT * from INDICATORS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY and ID_OBJECT=$idObj",
            TableBD.INDICATORS.column)
        return if (result.isNotEmpty())
            Indicator(result[0][0].toInt(), result[0][1].toInt(), result[0][2], result[0][3].toDouble(),
                result[0][4].toDouble(), result[0][5], result[0][6], result[0][7])
        else null
    }

    fun selectIndicators(idObj: String): List<Indicator>? {
        val resultList = select("SELECT * from INDICATORS where ID_OBJECT='$idObj'", TableBD.INDICATORS.column)
        return if (resultList.isNotEmpty()) {
            val indicators = mutableListOf<Indicator>()
            resultList.forEach {
                indicators.add(Indicator(it[0].toInt(),it[1].toInt(),it[2],it[3].toDouble(),it[4].toDouble(),it[5],it[6],it[7]))
            }
            indicators
        } else null
    }

    fun insertIntoIndicator(indicator: Indicator) {
        insertUpdateDelete("INSERT INTO INDICATORS (ID_OBJECT,NAME_INDICATOR,LAYOUT_X,LAYOUT_Y,NAME,UNIT,TYPE)" +
                    "VALUES ('${indicator.getIdObject()}' , '${indicator.getNameIndicator()}', '${indicator.getLayoutX()}', " +
                    "'${indicator.getLayoutY()}', '${indicator.getName()}', '${indicator.getUnit()}', '${indicator.getType()}');")
    }

    fun updateIndicator(id: Int, field: String, value: String) {
        insertUpdateDelete("UPDATE INDICATORS set $field = '$value' where ID=$id")
    }

    fun deleteIndicator(layoutX: Double, layoutY: Double) {
        insertUpdateDelete("DELETE from INDICATORS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY")
    }

    fun selectChart(layoutX: Double, layoutY: Double, idObj: Int): Chart? {
        val result = select("SELECT * from CHARTS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY and ID_OBJECT=$idObj", TableBD.CHARTS.column)
        return if (result.isNotEmpty())
            Chart(result[0][0].toInt(), result[0][1].toInt(), result[0][2], result[0][3].toDouble(),
                result[0][4].toDouble(), result[0][5], result[0][6], result[0][7])
        else null
    }

    fun selectCharts(idObj: String): List<Chart>? {
        val resultList = select("SELECT * from CHARTS where ID_OBJECT='$idObj'", TableBD.CHARTS.column)
        return if (resultList.isNotEmpty()) {
            val charts = mutableListOf<Chart>()
            resultList.forEach {
                charts.add(Chart(it[0].toInt(),it[1].toInt(),it[2],it[3].toDouble(),it[4].toDouble(),it[5],it[6],it[7]))
            }
            charts
        } else null
    }

    fun insertIntoChart(chart: Chart) {
        insertUpdateDelete("INSERT INTO CHARTS (ID_OBJECT,NAME_CHART,LAYOUT_X,LAYOUT_Y,NAME,UNIT,TYPE) VALUES ('${chart.getIdObject()}' , " +
              "'${chart.getNameChart()}','${chart.getLayoutX()}','${chart.getLayoutY()}','${chart.getName()}','${chart.getUnit()}','${chart.getType()}');")
    }

    fun updateChart(id: Int, field: String, value: String) {
        insertUpdateDelete("UPDATE CHARTS set $field = '$value' where ID=$id")
    }

    fun deleteChart(layoutX: Double, layoutY: Double) {
        insertUpdateDelete("DELETE from CHARTS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY")
    }
}