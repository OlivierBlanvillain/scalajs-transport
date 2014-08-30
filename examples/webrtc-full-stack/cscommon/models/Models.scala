package models

case class User(uuid: Long)
case class Message(text: String, user: User, timestamp: Long = System.currentTimeMillis())
