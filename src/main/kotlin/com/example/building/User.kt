package com.example.building

class User(
    private val id: Int?,
    private val idUser: String,
    private val username: String,
    private val login: String,
    private val password: String,
    private val address: String,
    private val token: String,
    private val castle: Boolean,
    private val alarm: Boolean,
    private val icon: Int,
    private val theme: String
) {
    fun getId(): Int {
        return id ?: 0
    }

    fun getIdUser(): String {
        return idUser
    }

    fun getUsername(): String {
        return username
    }

    fun getLogin(): String {
        return login
    }

    fun getPassword(): String {
        return password
    }

    fun getAddress(): String {
        return address
    }

    fun getToken(): String {
        return token
    }

    fun getCastle(): Boolean{
        return castle
    }

    fun getAlarm(): Boolean{
        return alarm
    }

    fun getIcon(): Int{
        return icon
    }

    fun getTheme(): String {
        return theme
    }
}