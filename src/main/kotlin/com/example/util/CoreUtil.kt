package com.example.util

var DEFAULT_ADDRESS = "https://dev.rightech.io/api/v1"

//connect
var HEADERS_AUTH: String = "Bearer "
const val HEADERS_CONTENT = "application/json"

//heading
const val AUTHORIZATION = "Authorization"
const val CONTENT_TYPE = "Content-Type"
var ID_USER = 0
const val TOPIC = "base/state/"

//method
const val GET = "GET"
const val POST = "POST"

//entities
const val MODELS = "models"
const val OBJECTS = "objects"
const val USERS ="users"
const val GROUPS = "groups"
const val MODEL = "model"

//JSON
const val _ID = "_id"
const val NAME = "name"
const val DATA = "data"
const val ID = "id"
const val LOGIN = "login"
const val TOKEN = "token"
const val STATE = "state"
const val CHILDREN = "children"
const val LEVELS = "levels"
const val DATA_TYPE = "dataType"
const val VALUE = "value"
const val TIME = "time"
const val PAYLOAD = "payload"
const val MIN = "Min"
const val MID = "Mid"
const val MAX = "Max"

enum class requestCharacters (val code: Int, val value: Char){
    SLESH(0, '/'),
    QUESTION(1, '?'),
    AND(2, '&');

    companion object {
        fun contains(code: Int): Boolean {
            return values().find { it.code == code } != null
        }
    }
}




