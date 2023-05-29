package com.example.bookapp.models

class CategoryModel {
    var id: String = ""
    var category: String = ""
    var timestamp: String = ""
    var uid: String = ""

    constructor()
    constructor(id: String, category: String, timestamp: String, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }
}