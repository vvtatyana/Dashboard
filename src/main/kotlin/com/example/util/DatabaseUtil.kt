package com.example.util

enum class TableBD (val column: Int){
    USERS(11),
    WIDGETS(10)
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
    ICON,
    THEME,
    TIMER
}

enum class WidgetsTable{
    ID,
    ID_OBJECT,
    TYPE_WIDGET,
    NAME_INDICATOR,
    LAYOUT_X,
    LAYOUT_Y,
    NAME,
    UNIT,
    TYPE
}
