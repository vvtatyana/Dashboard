package com.example.restAPI

import THEME
import com.example.building.Object
import com.example.building.User
import com.example.util.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
* Класс для работы json-данными
*/
class ProcessingJSON {

    /**
     * Читает данные
     * @jsonObject - данные для чтения
     * @description - элемен, который нужно вернуть
     */
    fun read(jsonObject: JsonObject, value: String): JsonElement? {
        return jsonObject.get(value)
    }

    /**
     * Читает данные о пользователях
     * @jsonArray - массив данных с пользователями
     */
    fun readAllUsers(jsonArray: JsonArray): List<User> {
        val iterator: Iterator<*> = jsonArray.iterator()
        val users = ArrayList<User>()
        while (iterator.hasNext()) {
            val slide: JsonObject = Gson().fromJson(iterator.next().toString(), JsonObject::class.java)
            users.add(
                User(
                    null,
                    slide.get(_ID).asString,
                    slide.get(NAME).asString,
                    slide.get(LOGIN).asString,
                    DEFAULT_ADDRESS,
                    TOKEN,
                    true,
                    1,
                    THEME
                )
            )
        }
        return users
    }

    /**
     * Читает параметры модели
     * @JsonObject - данные с моделью
     */
    fun readModelParams(jsonObject: JsonObject): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val children: JsonArray =
            jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN).asJsonArray
        for (i in 1 until children.size()) {
            if (children[i].asJsonObject.get(DATA_TYPE) != null)
                params[children[i].asJsonObject.get(ID).asString] = children[i].asJsonObject.get(DATA_TYPE).asString
        }
        return params
    }

    /* *
     * Читает состоянияя модели
     * JsonObject - данные с моделью
     */
    fun readModelState(jsonObject: JsonObject): List<String> {
        val params = readModelParams(jsonObject)
        val state = mutableListOf<String>()
        for (st in params)
            state.add(st.key)
        return state
    }

    /**
     * Изменяет данные о модели
     * @data - исходные данные о модели
     * @value - поле для изменения
     * @field - новое значение
     */
    fun updateModel(data: String, name: String, value: String, field: String): String {
        val jsonModel: JsonObject = Gson().fromJson(data, JsonObject::class.java)
        val children = jsonModel.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN)
            .asJsonArray

        for(ch in children){
            if (ch.asJsonObject.get(ID).asString == name){
                val values = ch.asJsonObject.get(LEVELS).asJsonObject.get(VALUE).asJsonArray
                for (i in 0 until values.size()) {
                    if (values[i].asJsonObject.get(NAME).asString == value) {
                        var jsonValue = values[i].asJsonObject.get(VALUE).asJsonObject
                        jsonValue.addProperty("a", field)
                        jsonValue = values[i - 1].asJsonObject.get(VALUE).asJsonObject
                        jsonValue.addProperty("b", field)
                        return jsonModel.toString()
                    }
                }
            }
        }

        return ""
    }

    /**
     * Читает уровни модели для определенного показателя
     * @jsonObject - данные о модели
     */
    fun readBorder(jsonObject: JsonObject, name: String): Map<String, String> {
        val border = mutableMapOf<String, String>()
        val children = jsonObject.get(DATA).asJsonObject.get(CHILDREN).asJsonArray[1].asJsonObject.get(CHILDREN)
            .asJsonArray

        for(ch in children) {
            if (ch.asJsonObject.get(ID).asString == name) {
                val values: JsonArray = ch.asJsonObject.get(LEVELS).asJsonObject.get(VALUE).asJsonArray
                for (i in 1 until values.size()) {
                    border[values[i].asJsonObject.get(NAME).asString] =
                        values[i].asJsonObject.get(VALUE).asJsonObject.get("a").asString
                }
            }
        }
        return border
    }

    /**
     * Читает даные о всех объектах
     * @jsonArray - массив данных об объектах
     */
    fun readAllObjects(jsonArray: JsonArray): List<Object> {
        val iterator: Iterator<*> = jsonArray.iterator()
        val objects = ArrayList<Object>()
        while (iterator.hasNext()) {
            val slide: JsonObject = Gson().fromJson(iterator.next().toString(), JsonObject::class.java)
            val obj =
                Object(null, slide.get(_ID).asString, ID_USER, slide.get(MODEL).asString, slide.get(NAME).asString)
            objects.add(obj)
        }
        return objects
    }

    /**
     * Читает показатели у объекта для построения графика, а имменно время и показатели в это время
     * @jsonArray - массив данных об объектах
     * @topic - название показателя
     */
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
                one.add(payload.asString.replace(",", ".").toDouble())
                axisData.add(one)
            }
        }
        return axisData
    }
}
