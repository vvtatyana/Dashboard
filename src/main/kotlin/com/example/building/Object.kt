package com.example.building

class Object {
    private val idObject: String
    private var idUser: Int = 0
    private val idModel: String
    private val nameObject: String

    constructor(idObject: String, idUser: Int, idModel: String, nameObject: String){
        this.idObject = idObject
        this.idUser = idUser
        this.idModel = idModel
        this.nameObject = nameObject
    }

    constructor(idObject: String, idModel: String, nameObject: String){
        this.idObject = idObject
        this.idModel = idModel
        this.nameObject = nameObject
    }

    fun getIdObject(): String = idObject

    fun getIdUser(): Int = idUser

    fun getIdModel(): String = idModel

    fun getNameObject(): String = nameObject
}