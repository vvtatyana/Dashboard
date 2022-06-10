package com.example.restAPI

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.util.*
import org.slf4j.LoggerFactory
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.nio.charset.Charset

class RequestGeneration {

    fun addressGeneration(address: String, description: String): String =
        "$address/$description"

    fun getRequest(address: String): String {
        val connection = URL(address).openConnection() as HttpURLConnection
        connection.requestMethod = GET
        connection.setRequestProperty(AUTHORIZATION, HEADERS_AUTH)
        connection.setRequestProperty(CONTENT_TYPE, HEADERS_CONTENT)
        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuffer()
                var readLine: String?
                while (input.readLine().also { readLine = it } != null) {
                    response.appendLine(readLine)
                }
                input.close()
                /*String(*/response.toString()/*.toByteArray(Charset.forName("Windows-1251")))*/
            } else "$responseCode ${connection.responseMessage}"
        } catch (u: UnknownHostException) { "No connection" }
        catch (n: NoRouteToHostException) { "No connection" }
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