package com.example.building

class Object(
    private val id: Int?,
    private val idObject: String,
    private val idUser: Int,
    private val idModel: String,
    private val nameObject: String
) {

    fun getId(): Int {
        return id ?: 0
    }

    fun getIdObject(): String {
        return idObject
    }

    fun getIdUser(): Int {
        return idUser
    }

    fun getIdModel(): String {
        return idModel
    }

    fun getNameObject(): String {
        return nameObject
    }
}