package com.example.restAPI

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.util.*

/* *
* Класс для обмена данными с платформой
*/
class RequestGeneration {

    /* *
     * Собирает url-адресс для запросов
     * address - протокол и хост
     * description - адрес ресурса
     */
    fun addressGeneration(address: String, description: String): String {
        return "$address/$description"
    }

    /* *
     * Собирает url-адресс для запросов и отправляет GET-запрос
     * address - протокол и хост
     * description - адрес ресурса
     */
    fun addressAssemblyGET(address: String, description: String): String? {
        return getRequest(
            addressGeneration(address, description)
        )
    }

    /* *
     * Совершает GET-запрос по указанному адресу
     * address - адрес для запроса
     */
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
            null
        }
    }

    /* *
     * Совершает PATCH-запрос по указанному адресу
     * address - адрес для запроса
     * postParams - измененные данные для отправки
     */
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
        println("PATCH Response Code :  $responseCode")
        println("PATCH Response Message : " + connection.responseMessage)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val isr = BufferedReader(
                InputStreamReader(
                    connection.inputStream
                )
            )
            var inputLine: String?
            val response = StringBuffer()
            while (isr.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            isr.close()
            return Gson().fromJson(response.toString(), JsonObject::class.java)
        } else {
            return null
        }
    }
}