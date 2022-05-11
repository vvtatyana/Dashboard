package com.example.restAPI

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.util.*
import org.slf4j.LoggerFactory

class RequestGeneration {
    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    fun addressGeneration(address: String, description: String): String = "$address/$description"

    fun getRequest(address: String?): String? {
        val urlForGetRequest = URL(address)
        var readLine: String?
        val connection = urlForGetRequest.openConnection() as HttpURLConnection
        connection.requestMethod = GET
        connection.setRequestProperty(AUTHORIZATION, HEADERS_AUTH)
        connection.setRequestProperty(CONTENT_TYPE, HEADERS_CONTENT)
        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_OK) {
            val input = BufferedReader(
                InputStreamReader(connection.inputStream)
            )
            val response = StringBuffer()
            while (input.readLine().also { readLine = it } != null) {
                response.append(readLine)
            }
            input.close()
            response.toString()
        } else {
            LOGGER.error("GET Response Code :  $responseCode")
            LOGGER.error("GET Response Message : " + connection.responseMessage)
            null
        }
    }

    @Throws(IOException::class)
    fun patchRequest(address: String?, postParams: String): JsonObject? {
        val url = URL(address)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = POST
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH")
        connection.setRequestProperty(AUTHORIZATION, HEADERS_AUTH)
        connection.setRequestProperty(CONTENT_TYPE, HEADERS_CONTENT)
        connection.doOutput = true
        val os = connection.outputStream
        os.write(postParams.toByteArray())
        os.flush()
        os.close()
        val responseCode = connection.responseCode

        return if (responseCode == HttpURLConnection.HTTP_OK) {
            val isr = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLine: String?
            val response = StringBuffer()
            while (isr.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            isr.close()
            Gson().fromJson(response.toString(), JsonObject::class.java)
        } else {
            LOGGER.error("PATCH Response Code :  $responseCode")
            LOGGER.error("PATCH Response Message : " + connection.responseMessage)
            null
        }
    }
}