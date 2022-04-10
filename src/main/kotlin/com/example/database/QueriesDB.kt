package com.example.database

import com.example.building.*
import com.example.util.tableBD
import java.sql.Connection
import java.sql.Statement
import kotlin.system.exitProcess

/**
* Класс для работы с бд
*/
class QueriesDB(private val connection: Connection, private val statement: Statement) {

    /**
    * Возвращает выборку данных из бд
    * @sql - запрос
    * @column - количество столбцов в таблице
    */
    private fun select(sql: String, column: Int): List<List<String>> {
        val result = mutableListOf<List<String>>()

        try {
            val rs = statement.executeQuery(sql)
            while (rs.next()) {
                val res = mutableListOf<String>()
                for (i in 1..column) {
                    res.add(rs.getString(i))
                }
                result.add(res)
            }
            rs.close()
        } catch (e: Exception) {
            System.err.println(sql + " " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
       return result
    }

    /**
    * Выполняет запросы для операторов вставить, изменить, удалить данные из таблицы
    * @sql - запрос
    */
    private fun insertUpdateDelete(sql: String) {
        try {
            statement.executeUpdate(sql)
            connection.commit()
        } catch (e: java.lang.Exception) {
            System.err.println(sql + " " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
    }

    /*USERS*/
    fun selectUser(field: String, value: String): User?{
        val result = select("SELECT * from USERS where $field='$value'", tableBD.USERS.column)
        return if (result.isNotEmpty())
            User(result[0][0].toInt(), result[0][1], result[0][2], result[0][3], result[0][4], result[0][5], result[0][6].toBoolean(), result[0][7].toInt(), result[0][8])
        else null
    }

    fun selectUsers(): List<User>? {
        val resultList = select("SELECT * from USERS", 7)
        return if (resultList.isNotEmpty()) {
            val users = mutableListOf<User>()
            for (result in resultList) {
                users.add(
                    User(
                        result[0].toInt(),
                        result[1],
                        result[2],
                        result[3],
                        result[4],
                        result[5],
                        result[6].toBoolean(),
                        result[7].toInt(),
                        result[8]
                    )
                )
            }
            users
        }
        else null
    }

    fun insertIntoUser(user: User){
        val sql = "INSERT INTO USERS (ID_USER,USERNAME,LOGIN,ADDRESS,TOKEN,CASTLE,ICON,THEME)" +
                "VALUES ('${user.getIdUser()}' , '${user.getUsername()}', '${user.getLogin()}', '${user.getAddress()}', '${user.getToken()}', '${user.getCastle()}', '${user.getIcon()}', '${user.getTheme()}');"
        insertUpdateDelete(sql)
    }

    fun updateUser(id: Int, field: String, value: String){
        val sql = "UPDATE USERS set $field='$value' where ID=$id"
        insertUpdateDelete(sql)
    }

    fun deleteUser(id: Int){
        val sql = "DELETE from USERS where ID=$id"
        insertUpdateDelete(sql)
    }

    /*OBJECTS*/
    fun selectObject(field: String, value: String): Object?{
        val result = select("SELECT * from OBJECTS where $field='$value'", tableBD.OBJECTS.column)
        return if (result.isNotEmpty())
            Object(result[0][0].toInt(), result[0][1], result[0][2].toInt(), result[0][3], result[0][4])
        else null
    }

    fun selectObjects(): List<Object>? {
        val resultList = select("SELECT * from OBJECTS", 5)
        return if (resultList.isNotEmpty()) {
            val objects = mutableListOf<Object>()
            for (result in resultList) {
                objects.add(Object(result[0].toInt(), result[1], result[2].toInt(), result[3], result[4]))
            }
            objects
        }
        else null
    }

    fun insertIntoObject(obj: Object){
        val sql = "INSERT INTO OBJECTS (ID_OBJECT,ID_USER,ID_MODEL,NAME_OBJECT)" +
                "VALUES ('${obj.getIdObject()}' , '${obj.getIdUser()}', '${obj.getIdModel()}', '${obj.getNameObject()}');"
        insertUpdateDelete(sql)
    }

    fun updateObject(idObj: String, field: String, value: String){
        val sql = "UPDATE OBJECTS set $field = '$value' where ID_OBJECT=$idObj"
        insertUpdateDelete(sql)
    }

    fun deleteObject(idObj: String){
        val sql = "DELETE from OBJECTS where ID_OBJECT='$idObj'"
        insertUpdateDelete(sql)
    }

    /*INDICATORS*/
    fun selectIndicator(layoutX: Double, layoutY: Double, idObj: String): Indicator?{
        val result = select("SELECT * from INDICATORS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY and ID_OBJECT='$idObj'", tableBD.INDICATORS.column)
        return if (result.isNotEmpty())
            Indicator(result[0][0].toInt(), result[0][1], result[0][2], result[0][3].toDouble(), result[0][4].toDouble(), result[0][5], result[0][6], result[0][7])
        else null
    }

    fun selectIndicators(): List<Indicator>? {
        val resultList = select("SELECT * from INDICATORS", tableBD.INDICATORS.column)
        return if (resultList.isNotEmpty()) {
            val indicators = mutableListOf<Indicator>()
            for (result in resultList) {
                indicators.add(
                    Indicator(
                        result[0].toInt(),
                        result[1],
                        result[2],
                        result[3].toDouble(),
                        result[4].toDouble(),
                        result[5],
                        result[6],
                        result[7]
                    )
                )
            }
            indicators
        }
        else null
    }

    fun insertIntoIndicator(indicator: Indicator){
        val sql = "INSERT INTO INDICATORS (ID_OBJECT,NAME_INDICATOR,LAYOUT_X,LAYOUT_Y,NAME,UNIT,TYPE)" +
                "VALUES ('${indicator.getIdObject()}' , '${indicator.getNameIndicator()}', '${indicator.getLayoutX()}', '${indicator.getLayoutY()}', '${indicator.getName()}', '${indicator.getUnit()}', '${indicator.getType()}');"
        insertUpdateDelete(sql)
    }

    fun updateIndicator(layoutX: Double, layoutY: Double, field: String, value: String){
        val sql = "UPDATE INDICATORS set $field = '$value' where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY"
        insertUpdateDelete(sql)
    }

    fun updateIndicatorId(id: Int, field: String, value: String){
        val sql = "UPDATE INDICATORS set $field = '$value' where ID=$id"
        insertUpdateDelete(sql)
    }

    fun deleteIndicator(layoutX: Double, layoutY: Double){
        val sql = "DELETE from INDICATORS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY"
        insertUpdateDelete(sql)
    }

    /*CHARTS*/
    fun selectChart(layoutX: Double, layoutY: Double, idObj: String): Chart?{
        val result = select("SELECT * from CHARTS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY and ID_OBJECT='$idObj'", tableBD.CHARTS.column)
        return if (result.isNotEmpty())
            Chart(result[0][0].toInt(), result[0][1], result[0][2], result[0][3].toDouble(), result[0][4].toDouble(), result[0][5], result[0][6], result[0][6])
        else null
    }

    fun selectCharts(): List<Chart>? {
        val resultList = select("SELECT * from CHARTS", tableBD.CHARTS.column)
        return if (resultList.isNotEmpty()) {
            val charts = mutableListOf<Chart>()
            for (result in resultList) {
                charts.add(
                    Chart(
                        result[0].toInt(),
                        result[1],
                        result[2],
                        result[3].toDouble(),
                        result[4].toDouble(),
                        result[5],
                        result[6],
                        result[7]
                    )
                )
            }
            charts
        }
        else null
    }

    fun insertIntoChart(chart: Chart){
        val sql = "INSERT INTO CHARTS (ID_OBJECT,NAME_CHART,LAYOUT_X,LAYOUT_Y,NAME,UNIT,TYPE)" +
                "VALUES ('${chart.getIdObject()}' , '${chart.getNameChart()}', '${chart.getLayoutX()}', '${chart.getLayoutY()}', '${chart.getName()}', '${chart.getUnit()}', '${chart.getType()}');"
        insertUpdateDelete(sql)
    }

    fun updateChart(layoutX: Double, layoutY: Double, field: String, value: String){
        val sql = "UPDATE CHARTS set $field = '$value' where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY"
        insertUpdateDelete(sql)
    }

    fun updateChartId(id: Int, field: String, value: String){
        val sql = "UPDATE CHARTS set $field = '$value' where ID=$id"
        insertUpdateDelete(sql)
    }

    fun deleteChart(layoutX: Double, layoutY: Double){
        val sql = "DELETE from CHARTS where LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY"
        insertUpdateDelete(sql)
    }
}