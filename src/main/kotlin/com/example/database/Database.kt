package com.example.database

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.system.exitProcess

class Database {
    private var connection: Connection
    private var statement: Statement

    init {
        val urlDirectory = "C:\\Users\\${System.getProperty("user.name")}\\AppData\\Roaming\\Dashboard"
        if (!Files.exists(Paths.get(urlDirectory))) File(urlDirectory).mkdirs()

        Class.forName("org.sqlite.JDBC")

        connection = DriverManager.getConnection("jdbc:sqlite:$urlDirectory\\SmartHome.db")
        statement = connection.createStatement()
        connection.autoCommit = false
        try {
            statement.executeUpdate(createUsersTable())
            statement.executeUpdate(createObjectsTable())
            statement.executeUpdate(createIndicatorsTable())
            statement.executeUpdate(createChartsTable())
        } catch (e: java.lang.Exception) {
            System.err.println("Open BD " + e.javaClass.name + ": " + e.message)
            exitProcess(0)
        }
    }

    fun closeBD() {
        connection.close()
        statement.close()
    }

    fun getConnection(): Connection = connection
    fun getStatement(): Statement = statement

    private fun createUsersTable(): String = "CREATE TABLE IF NOT EXISTS USERS" +
            "(ID             INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_USER        VARCHAR(50)   NOT NULL," +
            " USERNAME       VARCHAR(50)   NOT NULL," +
            " LOGIN          VARCHAR(50)   NOT NULL," +
            " PASSWORD       VARCHAR(50)   NOT NULL," +
            " ADDRESS        VARCHAR(50)   NOT NULL," +
            " TOKEN          VARCHAR(1000) NOT NULL," +
            " CASTLE         VARCHAR(10)   NOT NULL," +
            " ICON           VARCHAR(10)   NOT NULL," +
            " THEME          VARCHAR(20)   NOT NULL," +
            " TIMER          INTEGER       NOT NULL)"

    private fun createObjectsTable(): String = "CREATE TABLE IF NOT EXISTS OBJECTS" +
            "(ID             INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_OBJECT      VARCHAR(50)  NOT NULL," +
            " ID_USER        INTEGER       NOT NULL," +
            " ID_MODEL       VARCHAR(50)   NOT NULL," +
            " NAME_OBJECT    VARCHAR(50)   NOT NULL," +
            " FOREIGN KEY (ID_USER) REFERENCES USERS(ID))"

    private fun createIndicatorsTable(): String = "CREATE TABLE IF NOT EXISTS INDICATORS" +
            "(ID                 INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_OBJECT          INTEGER       NOT NULL," +
            " NAME_INDICATOR     VARCHAR(50)   NOT NULL," +
            " LAYOUT_X           DOUBLE        NOT NULL," +
            " LAYOUT_Y           DOUBLE        NOT NULL," +
            " NAME               VARCHAR(50)   NOT NULL," +
            " UNIT               VARCHAR(10)   NOT NULL," +
            " TYPE               VARCHAR(10)   NOT NULL," +
            " FOREIGN KEY (ID_OBJECT) REFERENCES OBJECTS(ID))"

    private fun createChartsTable(): String = "CREATE TABLE IF NOT EXISTS CHARTS" +
            "(ID                 INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_OBJECT          INTEGER       NOT NULL," +
            " NAME_CHART         VARCHAR(50)   NOT NULL," +
            " LAYOUT_X           DOUBLE        NOT NULL," +
            " LAYOUT_Y           DOUBLE        NOT NULL," +
            " NAME               VARCHAR(50)   NOT NULL," +
            " UNIT               VARCHAR(10)   NOT NULL," +
            " TYPE               VARCHAR(20)   NOT NULL," +
            " FOREIGN KEY (ID_OBJECT) REFERENCES OBJECTS(ID))"
}

