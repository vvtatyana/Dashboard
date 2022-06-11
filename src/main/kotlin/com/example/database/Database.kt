package com.example.database

import com.example.util.filePath
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.system.exitProcess

class Database {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var connection: Connection
    private lateinit var statement: Statement

    init {
        open()
        try {
            statement.executeUpdate(createUsersTable())
            connection.commit()
            statement.executeUpdate(createWidgetsTable())
            connection.commit()
        } catch (e: java.lang.Exception) {
            logger.error(e.javaClass.name + ": " + e.message)
            exitProcess(0)
        } finally {
            close()
        }
    }

    fun getConnection(): Connection = connection
    fun getStatement(): Statement = statement


    fun open() {
        Class.forName("org.sqlite.JDBC")
        connection =
            DriverManager.getConnection(
                "jdbc:sqlite:$filePath/database/DashboardRIC.db")
        statement = connection.createStatement()
        connection.autoCommit = false
    }

    fun close() {
        connection.close()
        statement.close()
    }

    private fun createUsersTable(): String = "CREATE TABLE IF NOT EXISTS USERS" +
            "(ID             INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_USER        VARCHAR(50)   NOT NULL," +
            " USERNAME       VARCHAR(50)   NOT NULL," +
            " LOGIN          VARCHAR(50)   NOT NULL," +
            " PASSWORD       VARCHAR(50)   NOT NULL," +
            " ADDRESS        VARCHAR(50)   NOT NULL," +
            " TOKEN          VARCHAR(1000) NOT NULL," +
            " CASTLE         VARCHAR(10)   NOT NULL," +
            " THEME          VARCHAR(20)   NOT NULL," +
            " TIMER          INTEGER       NOT NULL)"

    private fun createWidgetsTable(): String = "CREATE TABLE IF NOT EXISTS WIDGETS" +
            "(ID                 INTEGER       PRIMARY KEY    AUTOINCREMENT   NOT NULL," +
            " ID_USER            INTEGER       NOT NULL," +
            " ID_OBJECT          VARCHAR(50)   NOT NULL," +
            " TYPE_WIDGET        VARCHAR(10)   NOT NULL," +
            " IDENTIFIER         VARCHAR(50)   NOT NULL," +
            " LAYOUT_X           DOUBLE        NOT NULL," +
            " LAYOUT_Y           DOUBLE        NOT NULL," +
            " NAME               VARCHAR(50)   NOT NULL," +
            " UNIT               VARCHAR(10)   NOT NULL," +
            " TYPE               VARCHAR(20)   NOT NULL," +
            " FOREIGN KEY (ID_USER) REFERENCES USERS(ID))"
}

