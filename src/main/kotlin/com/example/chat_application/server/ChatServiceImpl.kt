package com.example.chat_application.server

import chat.Chat
import chat.ChatServiceGrpc
import chat.ChatServiceGrpcKt
import com.example.chat_application.common.GrpcKeys
import com.example.chat_application.entity.MessageEntity
import com.example.chat_application.repository.MessageRepository
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ChatServiceImpl(private val messageRepository: MessageRepository) : ChatServiceGrpc.ChatServiceImplBase() {

    override fun joinRoom(request: Chat.JoinRoomRequest, responseObserver: StreamObserver<Empty>) {
        RoomManager.joinRoom(request.roomId,request.username)
        println("${request.username} joined room ${request.roomId}!")

        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
    }

    override fun getHistory(request: Chat.GetHistoryRequest, responseObserver: StreamObserver<Chat.GetHistoryResponse>) {
        val messageEntities = messageRepository.findByRoomId(request.roomId)

        //convert entities to proto buffers
        val messages = messageEntities.map { message ->
            Chat.ChatMessage.newBuilder()
                .setMessageId(message.messageId.toString())
                .setRoomId(message.roomId)
                .setTimestamp(message.timestamp)
                .setUsername(message.username)
                .setContent(message.content)
                .build()
        }

        val response = Chat.GetHistoryResponse.newBuilder()
            .addAllMessages(messages)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun chatStream(responseObserver: StreamObserver<Chat.ChatEvent>): StreamObserver<Chat.ChatEvent> {


        //Get the username,roomID from headers and then connect user to responseObserver in roomManager.(Instant Registration)
        val roomId = GrpcKeys.ROOM_ID_CONTEXT_KEY.get() //This for some reason is always thread safe and accurate (Thread Local Storage) LEARN THIS L8R.
        val username = GrpcKeys.USERNAME_CONTEXT_KEY.get()
        RoomManager.attachObserver(roomId,username,responseObserver)

        return object : StreamObserver<Chat.ChatEvent> {

            override fun onNext(event: Chat.ChatEvent) {
                if(!event.hasMessage()) return

                val message = event.message

                val messageEntity = MessageEntity(
                    username = message.username,
                    roomId = message.roomId,
                    content = message.content,
                    timestamp = System.currentTimeMillis()
                )

                try{
                    messageRepository.save(messageEntity)
                    RoomManager.broadcast(message.roomId,event)
                } catch (e: Exception){
                    println("Failed to save message to DB: ${e.message}!")
                }
            }

            override fun onError(p0: Throwable?) {
                println("Connection Lost to $username: ${p0?.message}")
                RoomManager.removeObserver(roomId,username)
            }

            override fun onCompleted() {
                println("$username has exited the chat!")
                RoomManager.removeObserver(roomId,username)
            }
        }
    }
}