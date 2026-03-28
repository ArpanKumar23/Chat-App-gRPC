package com.example.chat_application.server

import chat.Chat
import chat.Chat.ChatEvent
import io.grpc.stub.StreamObserver
import java.util.concurrent.ConcurrentHashMap

object RoomManager {

    private val rooms = ConcurrentHashMap<String, ConcurrentHashMap<String, StreamObserver<ChatEvent>>>()

    fun joinRoom(roomId: String,username: String){
        rooms.computeIfAbsent(roomId) { ConcurrentHashMap() } //creates the room if it dne.
    }

    fun attachObserver(roomId: String,username: String, observer: StreamObserver<ChatEvent>){
        val room = rooms.computeIfAbsent(roomId) { ConcurrentHashMap() }
        room[username] = observer
    }

    fun broadcast(roomId: String, event: ChatEvent){
        rooms[roomId]?.forEach{ (username,responseObserver) ->
            try {
                responseObserver.onNext(event)
            } catch (e: Exception) {
                println("$username has disconnected! Removing Dead connection.")
                removeObserver(roomId, username)
            }
        }
    }

    fun removeObserver(roomId: String, username: String){
        val room = rooms[roomId]

        if(room != null){
            room.remove(username)

            if(room.isEmpty()){
                rooms.remove(roomId) // prevents empty rooms from building up.
            }
        }
    }
}