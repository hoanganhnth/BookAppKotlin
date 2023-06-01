package com.example.bookapp.models

class CommentModel {
    var id: String = ""
    var bookId: String = ""
    var comment: String = ""
    var uid: String = ""
    var timestamp: Long = 0
    constructor()
    constructor(id: String, bookId: String, comment: String, uid: String, timestamp: Long) {
        this.id = id
        this.bookId = bookId
        this.comment = comment
        this.uid = uid
        this.timestamp = timestamp
    }

}