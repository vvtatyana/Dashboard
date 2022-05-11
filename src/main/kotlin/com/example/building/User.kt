package com.example.building

class User(
    private val id: Int?,
    private val idUser: String,
    private val username: String,
    private val login: String,
    private val password: String,
    private var address: String,
    private var token: String,
    private val castle: Boolean,
    private var icon: Int,
    private val theme: String,
    private var timer: Int
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

    fun setAddress(newValue: String) {
        address = newValue
    }

    fun getToken(): String {
        return token
    }

    fun setToken(newValue: String) {
        token = newValue
    }

    fun getCastle(): Boolean{
        return castle
    }

    fun getIcon(): Int{
        return icon
    }

    fun setIcon(newValue: Int) {
        icon = newValue
    }

    fun getTheme(): String {
        return theme
    }

    fun getTimer(): Int {
        return timer
    }

    fun setTimer(newValue: Int) {
        timer = newValue
    }
}