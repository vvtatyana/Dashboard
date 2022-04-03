package com.example.back

import THEME
import com.example.building.Object
import com.example.building.User
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class Parsing {

    //чтение данных из JsonObject в тип данных JsonElement
    fun read(jsonObject: JsonObject, value: String): JsonElement? {
        return jsonObject.get(value)
    }

    //чтение данных о всех пользователях из JsonArray в List
    fun readAllUsers(jsonArray: JsonArray): List<User> {
        val i: Iterator<*> = jsonArray.iterator()
        val listUsers = ArrayList<User>()
        while (i.hasNext()) {
            val slide: JsonObject = Gson().fromJson(i.next().toString(), JsonObject::class.java)
            listUsers.add(
                User(
                    null,
                    slide.get(_ID).asString,
                    slide.get(NAME).asString,
                    slide.get(LOGIN).asString,
                    DEFAULT_ADDRESS,
                    TOKEN,
                    true,
                    THEME
                )
            )
        }
        return listUsers
    }

    //чтение параметров модели из JsonObject в Map
    fun readModelParams(jsonObject: JsonObject): Map<String, String> {
        val state = mutableMapOf<String, String>()
        val data: JsonArray =
            jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN).asJsonArray
        for (i in 1 until data.size()) {
            if (data[i].asJsonObject.get(DATA_TYPE) != null)
                state[data[i].asJsonObject.get(ID).asString] = data[i].asJsonObject.get(DATA_TYPE).asString
        }
        return state
    }

    //изменяет данные о модели
    fun updateModel(data: String, value: String, field: String): String {
        var startIndex = data.indexOf(CHILDREN, 0) + 8
        if (startIndex > -1) {
            startIndex = data.indexOf(CHILDREN, startIndex) + 8
            if (startIndex > -1) {
                startIndex = data.indexOf(
                    "\"$CHILDREN\": [\n" +
                            "                            {", startIndex
                ) + 10
                if (startIndex > -1) {
                    startIndex = data.indexOf("\"$NAME\": \"$value\"", startIndex)
                    if (startIndex > -1) {
                        startIndex = data.indexOf("\"$VALUE\": ", startIndex)
                        if (startIndex > -1) {
                            val endIndex = data.indexOf(",", startIndex)
                            val strBuilder = StringBuilder(data)
                            strBuilder.replace(data.indexOf(" ", startIndex) + 1, endIndex, field)
                            return strBuilder.toString()
                        }
                    }
                }
            }
        }
        return ""
    }

    //чтение уровни модели для определенного параметра из JsonObject в Map
    fun readBorder(jsonObject: JsonObject): Map<String, String> {
        val map = mutableMapOf<String, String>()

        val data: JsonArray =
            jsonObject.get(DATA).asJsonObject.get("children").asJsonArray[1].asJsonObject.get("children").asJsonArray[1].asJsonObject.get(
                "children"
            ).asJsonArray
        for (i in 1 until data.size()) {
            map[data[i].asJsonObject.get("name").asString] = data[i].asJsonObject.get("value").asString
        }
        return map
    }


    //чтение данных о всех объектах из JsonArray в List
    fun readAllObjects(jsonArray: JsonArray): List<Object> {
        val i: Iterator<*> = jsonArray.iterator()
        val list = ArrayList<Object>()
        while (i.hasNext()) {
            val slide: JsonObject = Gson().fromJson(i.next().toString(), JsonObject::class.java)
            val objects =
                Object(null, slide.get(_ID).asString, ID_USER, slide.get(MODEL).asString, slide.get(NAME).asString)
            list.add(objects)
        }
        return list
    }

    //читает показатели у объекта  для графика
    fun readForChart(jsonArray: JsonArray, topic: String): List<List<Number>> {
        val i: Iterator<*> = jsonArray.iterator()
        val list = ArrayList<ArrayList<Number>>()
        while (i.hasNext()) {
            val one = ArrayList<Number>()
            val slide: JsonObject = Gson().fromJson(i.next().toString(), JsonObject::class.java)
            val getTopic = slide.get("topic")

            if (getTopic != null && getTopic.asString == topic) {
                val time = slide.get(TIME)
                val payload = slide.get(PAYLOAD)
                one.add(time.asLong)
                one.add(payload.asDouble)
                list.add(one)
            }
        }
        return list
    }
}
