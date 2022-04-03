package com.example.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.system.exitProcess

class Database {
    private var connection: Connection
    private var statement: Statement

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/com/example/front/database/SmartHome.db")
        statement = connection.createStatement()
        connection.autoCommit = false
        openBD()
    }

    fun getConnection(): Connection{
        return connection
    }

    fun getStatement(): Statement{
        return statement
    }

    private fun openBD() {
        try {
            var sql = usersTable()
            statement.executeUpdate(sql)
            sql = objectsTable()
            statement.executeUpdate(sql)
            sql = indicatorsTable()
            statement.executeUpdate(sql)
            sql = chartsTable()
            statement.executeUpdate(sql)
        } catch (e: java.lang.Exception) {
            System.err.println("openBD " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
    }

    fun closeBD(){
        connection.close()
        statement.close()
    }

    private fun usersTable(): String {
        return "CREATE TABLE IF NOT EXISTS USERS" +
                "(ID             INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
                " ID_USER        VARCHAR(50)   NOT NULL," +
                " USERNAME       VARCHAR(50)   NOT NULL," +
                " LOGIN          VARCHAR(50)   NOT NULL," +
                " ADDRESS        VARCHAR(50)   NOT NULL," +
                " TOKEN          VARCHAR(1000) NOT NULL," +
                " CASTLE         VARCHAR(10)   NOT NULL," +
                " THEME          VARCHAR(20)   NOT NULL)"
    }

    private fun objectsTable(): String {
        return "CREATE TABLE IF NOT EXISTS OBJECTS" +
                "(ID             INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
                " ID_OBJECT      VARCHAR(50)   NOT NULL," +
                " ID_USER        VARCHAR(50)   NOT NULL," +
                " ID_MODEL       VARCHAR(50)   NOT NULL," +
                " NAME_OBJECT    VARCHAR(50)   NOT NULL)"
    }

    private fun indicatorsTable(): String {
        return "CREATE TABLE IF NOT EXISTS INDICATORS" +
                "(ID                 INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
                " ID_OBJECT          VARCHAR(50)   NOT NULL," +
                " NAME_INDICATOR     VARCHAR(50)   NOT NULL," +
                " LAYOUT_X           DOUBLE        NOT NULL," +
                " LAYOUT_Y           DOUBLE        NOT NULL," +
                " NAME               VARCHAR(50)   NOT NULL," +
                " UNIT               VARCHAR(10)   NOT NULL)"
    }

    private fun chartsTable(): String {
        return "CREATE TABLE IF NOT EXISTS CHARTS" +
                "(ID                 INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
                " ID_OBJECT          VARCHAR(50)   NOT NULL," +
                " NAME_CHART         VARCHAR(50)   NOT NULL," +
                " LAYOUT_X           DOUBLE        NOT NULL," +
                " LAYOUT_Y           DOUBLE        NOT NULL," +
                " NAME               VARCHAR(50)   NOT NULL," +
                " UNIT               VARCHAR(10)   NOT NULL," +
                " TYPE               VARCHAR(20)   NOT NULL)"
    }
}
