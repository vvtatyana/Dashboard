package com.example.util

enum class TableBD (val column: Int){
    USERS(11),
    OBJECTS(5),
    INDICATORS(8),
    CHARTS(8)
}

enum class UsersTable{
    ID,
    ID_USER,
    USERNAME,
    LOGIN,
    PASSWORD,
    ADDRESS,
    TOKEN,
    CASTLE,
    ALARM,
    ICON,
    THEME
}

enum class ObjectsTable{
    ID,
    ID_OBJECT,
    ID_USER,
    ID_MODEL,
    NAME_OBJECT
}

enum class IndicatorsTable{
    ID,
    ID_OBJECT,
    NAME_INDICATOR,
    LAYOUT_X,
    LAYOUT_Y,
    NAME,
    UNIT,
    TYPE
}

enum class ChartsTable{
    ID,
    ID_OBJECT,
    NAME_INDICATOR,
    LAYOUT_X,
    LAYOUT_Y,
    NAME,
    UNIT,
    TYPE
}

