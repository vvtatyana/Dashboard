package com.example.restAPI

import com.example.building.Object
import com.example.building.User
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ProcessingJSON {
    private val INFINITY = "Infinity"

    fun read(jsonObject: JsonObject, value: String): JsonElement? = jsonObject.get(value)

    fun readAllUsers(jsonArray: JsonArray): List<User> {
        val iterator: Iterator<*> = jsonArray.iterator()
        val users = ArrayList<User>()
        while (iterator.hasNext()) {
            val slide: JsonObject = Gson().fromJson(iterator.next().toString(), JsonObject::class.java)
            users.add(User(slide.get(_ID).asString, slide.get(NAME).asString,slide.get(LOGIN).asString))
        }
        return users
    }

    fun readAllObjects(jsonArray: JsonArray): List<Object> {
        val iterator: Iterator<*> = jsonArray.iterator()
        val objects = ArrayList<Object>()
        while (iterator.hasNext()) {
            val slide: JsonObject = Gson().fromJson(iterator.next().toString(), JsonObject::class.java)
            val obj =
                Object(slide.get(_ID).asString, slide.get(MODEL).asString, slide.get(NAME).asString)
            objects.add(obj)
        }
        return objects
    }


    fun readModelParameters(jsonObject: JsonObject): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val children: JsonArray =
            jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN).asJsonArray
        for (i in 1 until children.size()) {
            if (children[i].asJsonObject.get(DATA_TYPE) != null)
                params[children[i].asJsonObject.get(ID).asString] = children[i].asJsonObject.get(DATA_TYPE).asString
        }
        return params
    }

    fun readModelState(jsonObject: JsonObject): List<String> {
        val params = readModelParameters(jsonObject)
        val state = mutableListOf<String>()
        params.forEach { state.add(it.key) }
        return state
    }


    private fun readBorder(jsonObject: JsonObject, name: String): JsonArray? {
        val children =
            jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN).asJsonArray

        children.forEach {
            if (it.asJsonObject.get(ID).asString == name && it.asJsonObject.get(LEVELS) != null
                && it.asJsonObject.get(LEVELS).asJsonObject.get(VALUE).toString() != "null"
            ) return it.asJsonObject.get(LEVELS).asJsonObject.get(VALUE).asJsonArray
        }
        return null
    }

    fun readBorderColor(jsonObject: JsonObject, name: String): Map<String, String> {
        val borderColor = mutableMapOf<String, String>()
        val children =
            jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN).asJsonArray

        children.forEach { it ->
            if (it.asJsonObject.get(ID).asString == name) {
                val values: JsonArray = it.asJsonObject.get(LEVELS).asJsonObject.get(VALUE).asJsonArray
                values.forEach {
                    borderColor[it.asJsonObject.get(NAME).asString] =
                        it.asJsonObject.get("color").asString
                }
            }
        }
        return borderColor
    }

    fun typeNumeric(jsonObject: JsonObject, name: String): Boolean {
        val values = readBorder(jsonObject, name)
        if (values != null) {
            try {
                if (values[0].asJsonObject.get(VALUE).asJsonObject.get("a").asString != null)
                    return true
            } catch (ill: IllegalStateException) {
                return false
            }
        }
        return false
    }

    fun readBorderFrom(jsonObject: JsonObject, name: String): Map<String, String> {
        val border = mutableMapOf<String, String>()
        val values = readBorder(jsonObject, name)
        if (values != null) {
            try {
                if (values[0].asJsonObject.get(VALUE).asJsonObject.get("a").asString != null) {
                   values.forEach {
                        if (it.asJsonObject.get(VALUE).asJsonObject.get("a").asString != "-$INFINITY")
                            border[it.asJsonObject.get(NAME).asString] =
                                it.asJsonObject.get(VALUE).asJsonObject.get("a").asString
                        else if (it.asJsonObject.get(VALUE).asJsonObject.get("a").asString == "-$INFINITY") {
                            border[it.asJsonObject.get(NAME).asString] =
                                (it.asJsonObject.get(VALUE).asJsonObject.get("b").asInt - 10).toString()
                        }
                    }
                }
            } catch (ill: IllegalStateException) {
                values.forEach {
                    border[it.asJsonObject.get(NAME).asString] = it.asJsonObject.get(VALUE).asString
                }
            }
        }
        return border
    }

    fun readBorderTo(jsonObject: JsonObject, name: String): Map<String, String> {
        val border = mutableMapOf<String, String>()
        val values = readBorder(jsonObject, name)
        if (values != null) {
            try {
                values.forEach {
                    if (it.asJsonObject.get(VALUE).asJsonObject.get("b").asString != INFINITY)
                        border[it.asJsonObject.get(NAME).asString] =
                            it.asJsonObject.get(VALUE).asJsonObject.get("b").asString
                    else if (it.asJsonObject.get(VALUE).asJsonObject.get("b").asString == INFINITY) {
                        border[it.asJsonObject.get(NAME).asString] =
                            ((it.asJsonObject.get(VALUE).asJsonObject.get("a").asInt / 10 + 2) * 10).toString()
                    }
                }
            } catch (ill: IllegalStateException) {
                return border
            }
        }
        return border
    }

    fun readBorderBooleanOrString(jsonObject: JsonObject, name: String): Map<String, String> {
        val border = mutableMapOf<String, String>()
        readBorder(jsonObject, name)?.forEach {
            border[it.asJsonObject.get(NAME).asString] = it.asJsonObject.get(VALUE).asString
        }
        return border
    }

    fun readForChart(jsonArray: JsonArray, topic: String): List<List<Number>> {
        val iterator: Iterator<*> = jsonArray.iterator()
        val axisData = ArrayList<ArrayList<Number>>()
        while (iterator.hasNext()) {
            val one = ArrayList<Number>()
            val slide: JsonObject = Gson().fromJson(iterator.next().toString(), JsonObject::class.java)
            val getTopic = slide.get(TOPIC)
            if (getTopic != null && getTopic.asString == topic) {
                val time = slide.get(TIME)
                val payload = slide.get(PAYLOAD)
                one.add(time.asLong)
                if (payload.asString.indexOf("true") == -1 && payload.asString.indexOf("false") == -1) {
                    one.add(payload.asString.replace(",", ".").toDouble())
                } else {
                    if (payload.asString.indexOf("true") != -1) one.add(1)
                    else if (payload.asString.indexOf("false") != -1) one.add(0)
                }
                axisData.add(one)
            }
        }
        return axisData
    }

    fun updateModel(data: String, name: String, value: String, field: String, property: String): String? {
        val jsonModel: JsonObject = Gson().fromJson(data, JsonObject::class.java)
        readBorder(jsonModel, name)?.forEach {
            if (it.asJsonObject.get(NAME).asString == value) {
                it.asJsonObject.addProperty(property, field)
                return jsonModel.toString()
            }
        }
        return null
    }

    fun updateBorder(data: String, name: String, value: String, field: String, level: String): String? {
        val jsonModel: JsonObject = Gson().fromJson(data, JsonObject::class.java)
        readBorder(jsonModel, name)?.forEach {
            if (it.asJsonObject.get(NAME).asString == value) {
                it.asJsonObject.get(VALUE).asJsonObject.addProperty(level, field)
                return jsonModel.toString()
            }
        }
        return null
    }
}
