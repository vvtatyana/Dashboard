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
    fun getId(): Int = id ?: 0

    fun getIdUser(): String = idUser

    fun getUsername(): String = username

    fun getLogin(): String = login

    fun getPassword(): String = password

    fun getAddress(): String = address

    fun setAddress(newValue: String) {
        address = newValue
    }

    fun getToken(): String = token

    fun setToken(newValue: String) {
        token = newValue
    }

    fun getCastle(): Boolean = castle

    fun getIcon(): Int = icon

    fun setIcon(newValue: Int) {
        icon = newValue
    }

    fun getTheme(): String = theme

    fun getTimer(): Int = timer

    fun setTimer(newValue: Int) {
        timer = newValue
    }
}