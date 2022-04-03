package com.example.building

class User(
    private val id: Int?,
    private val idUser: String,
    private val username: String,
    private val login: String,
    private val address: String,
    private val token: String,
    private val castle: Boolean,
    private val theme: String
) {
    fun getId(): Int? {
        return id
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

    fun getAddress(): String {
        return address
    }

    fun getToken(): String {
        return token
    }

    fun getCastle(): Boolean{
        return castle
    }

    fun getTheme(): String {
        return theme
    }
}