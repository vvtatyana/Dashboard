package com.example.building

class Object(
    private val id: Int?,
    private val idObject: String,
    private val idUser: Int,
    private val idModel: String,
    private val nameObject: String
) {
    fun getId(): Int = id ?: 0

    fun getIdObject(): String = idObject

    fun getIdUser(): Int = idUser

    fun getIdModel(): String = idModel

    fun getNameObject(): String = nameObject
}