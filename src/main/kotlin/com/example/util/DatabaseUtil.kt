package com.example.util

enum class tableBD (val column: Int){
    USERS(8),
    OBJECTS(5),
    INDICATORS(7),
    CHARTS(8),
}

enum class usersTable{
    ID,
    ID_USER,
    USERNAME,
    LOGIN,
    ADDRESS,
    TOKEN,
    CASTLE,
    THEME
}

enum class objectsTable{
    ID,
    ID_OBJECT,
    ID_USER,
    ID_MODEL,
    NAME_OBJECT
}

enum class indicatorsTable{
    ID,
    ID_OBJECT,
    NAME_INDICATOR,
    LAYOUT_X,
    LAYOUT_Y,
    NAME,
    UNIT,
}

enum class chartsTable{
    ID,
    ID_OBJECT,
    NAME_INDICATOR,
    LAYOUT_X,
    LAYOUT_Y,
    NAME,
    UNIT,
    TYPE,
}

