package com.example.database

import com.example.building.*
import com.example.util.TableBD
import org.slf4j.LoggerFactory

class QueriesDB {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val database: Database = Database()

    private fun select(sql: String, column: Int): List<List<String>> {
        val result = mutableListOf<List<String>>()
        try {
            database.open()
            val rs = database.getStatement().executeQuery(sql)
            while (rs.next()) {
                val res = mutableListOf<String>()
                for (i in 1..column)
                    res.add(rs.getString(i))
                result.add(res)
            }
            rs.close()
        } catch (e: Exception) {
            logger.error(sql + " " + e.javaClass.name + ": " + e.message)
        } finally {
            database.close()
        }
        return result
    }

    private fun insertUpdateDelete(sql: String) {
        try {
            database.open()
            database.getStatement().executeUpdate(sql)
            database.getConnection().commit()
        } catch (e: java.lang.Exception) {
            logger.error(sql + " " + e.javaClass.name + ": " + e.message)
        } finally {
            database.close()
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

    fun selectWidget(idUser: Int, idObj: String, typeWidget: String, layoutX: Double, layoutY: Double): Widget? {
        val result = select("SELECT * from WIDGETS where ID_USER='$idUser' and ID_OBJECT='$idObj' and TYPE_WIDGET='$typeWidget' " +
                "and LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY", TableBD.WIDGETS.column)
        return if (result.isNotEmpty())
            Widget(result[0][0].toInt(), result[0][1].toInt(), result[0][2], result[0][3], result[0][4], result[0][5].toDouble(),
                result[0][6].toDouble(), result[0][7], result[0][8], result[0][9])
        else null
    }
    fun selectWidgets(idUser: Int, idObj: String, typeWidget: String): List<Widget>? {
        val resultList = select("SELECT * from WIDGETS where ID_USER='$idUser' and ID_OBJECT='$idObj' and TYPE_WIDGET='$typeWidget'", TableBD.WIDGETS.column)
        return if (resultList.isNotEmpty()) {
            val indicators = mutableListOf<Widget>()
            resultList.forEach {
                indicators.add(Widget(it[0].toInt(),it[1].toInt(),it[2],it[3],it[4],it[5].toDouble(),it[6].toDouble(),it[7],it[8],it[9]))
            }
            indicators
        } else null
    }
    fun insertIntoWidget(indicator: Widget) {
        insertUpdateDelete("INSERT INTO WIDGETS (ID_USER,ID_OBJECT,TYPE_WIDGET,IDENTIFIER,LAYOUT_X,LAYOUT_Y,NAME,UNIT,TYPE)" +
                    "VALUES ('${indicator.getIdUser()}','${indicator.getIdObject()}','${indicator.getTypeWidgets()}','${indicator.getIdentifier()}'," +
                "'${indicator.getLayoutX()}','${indicator.getLayoutY()}','${indicator.getName()}','${indicator.getUnit()}','${indicator.getType()}');")
    }
    fun updateWidget(id: Int, typeWidget: String, field: String, value: String) {
        insertUpdateDelete("UPDATE WIDGETS set $field = '$value' where ID=$id and TYPE_WIDGET='$typeWidget'")
    }
    fun deleteWidget(idUser: Int, idObj: String, typeWidget: String, layoutX: Double, layoutY: Double) {
        insertUpdateDelete("DELETE from WIDGETS where ID_USER='$idUser' and ID_OBJECT='$idObj' " +
                "and TYPE_WIDGET='$typeWidget' and LAYOUT_X=$layoutX and LAYOUT_Y=$layoutY")
    }
}