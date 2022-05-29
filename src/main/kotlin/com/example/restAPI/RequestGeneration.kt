package com.example.restAPI

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.util.*
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.nio.charset.Charset

class RequestGeneration {
    fun addressGeneration(address: String, description: String): String = "$address/$description"

    fun getRequest(address: String): String {
        val urlForGetRequest = URL(address)
        val connection = urlForGetRequest.openConnection() as HttpURLConnection
        connection.requestMethod = GET
        connection.setRequestProperty(AUTHORIZATION, HEADERS_AUTH)
        connection.setRequestProperty(CONTENT_TYPE, HEADERS_CONTENT)
        try {
            val responseCode = connection.responseCode
            return if (responseCode == HttpURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuffer()
                var readLine: String?
                while (input.readLine().also { readLine = it } != null) {
                    response.append(readLine)
                }
                input.close()
                /*String(*/response.toString()/*.toByteArray(Charset.forName("Windows-1251")))*/
            } else "$responseCode ${connection.responseMessage}"
        } catch (u: UnknownHostException) {
            return "No connection"
        } catch (n: NoRouteToHostException) {
            return "No connection"
        }
    }


    fun patchRequest(address: String, postParams: String): String {
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
        return "${connection.responseCode} ${connection.responseMessage}"
    }
}