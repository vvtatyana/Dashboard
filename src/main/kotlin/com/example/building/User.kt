package com.example.building

class User {
    private var id: Int = 0
    private var idUser: String
    private var username: String
    private var login: String
    private lateinit var password: String
    private lateinit var address: String
    private lateinit var token: String
    private var castle: Boolean = false
    private lateinit var theme: String
    private var timer: Int = 1

    constructor(
        id: Int,
        idUser: String,
        username: String,
        login: String,
        password: String,
        address:
        String,
        token: String,
        castle: Boolean,
        theme: String,
        timer: Int
    ) {
        this.id = id
        this.idUser = idUser
        this.username = username
        this.login = login
        this.password = password
        this.address = address
        this.token = token
        this.castle = castle
        this.theme = theme
        this.timer = timer
    }

    constructor(idUser: String, username: String, login: String, password: String, address: String,
        token: String, castle: Boolean, theme: String, timer: Int) {
        this.idUser = idUser
        this.username = username
        this.login = login
        this.password = password
        this.address = address
        this.token = token
        this.castle = castle
        this.theme = theme
        this.timer = timer
    }

    constructor(idUser: String, username: String, login: String) {
        this.idUser = idUser
        this.username = username
        this.login = login
    }

    override fun toString(): String {
        return "_id: $idUser, \nname: $username, \nlogin: $login"
    }
    fun getId(): Int = id
    fun getIdUser(): String = idUser
    fun getUsername(): String = username
    fun getLogin(): String = login
    fun getPassword(): String = password
    fun getAddress(): String = address
    fun setAddress(newValue: String) { address = newValue }
    fun getToken(): String = token
    fun setToken(newValue: String) { token = newValue }
    fun getCastle(): Boolean = castle
    fun getTheme(): String = theme
    fun getTimer(): Int = timer
    fun setTimer(newValue: Int) { timer = newValue }
}