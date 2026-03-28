package com.example.chat_application.client

import chat.Chat
import chat.ChatServiceGrpc
import chat.ChatServiceGrpcKt
import com.example.chat_application.common.GrpcKeys.ROOM_ID_CONTEXT_KEY
import com.example.chat_application.common.GrpcKeys.ROOM_ID_META_KEY
import com.example.chat_application.common.GrpcKeys.USERNAME_META_KEY
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.MetadataUtils
import io.grpc.Metadata
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.system.exitProcess

fun main() = runBlocking{
    print("Enter your username: ")
    val username : String = readln()

    print("Hello $username! What room do you want to join: ")
    val roomId : String = readln()

    val channel = ManagedChannelBuilder
        .forAddress("localhost", 9090)
        .usePlaintext()
        .build()


    val metadata = Metadata()
    metadata.put(USERNAME_META_KEY, username)
    metadata.put(ROOM_ID_META_KEY, roomId)
    val headerInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)

    val blockingStub = ChatServiceGrpc.newBlockingStub(channel).withInterceptors(headerInterceptor) // Is it neccessary for every stub to send headers?
    val asyncStub = ChatServiceGrpc.newStub(channel).withInterceptors(headerInterceptor)

    //Join the room
    val success = attemptJoinRoom(blockingStub,roomId,username)
    if(!success){
        channel.shutdown()
        return@runBlocking
    }

    //Try to get rooms history.
    displayMessageHistory(blockingStub,roomId)

    //How you will receive data
    val responseObserver = object : StreamObserver<Chat.ChatEvent>{
        override fun onNext(event: Chat.ChatEvent) {
            if(event.hasMessage() && event.message.username != username){
                println("\r[${event.message.username}]: ${event.message.content}")
            }
        }

        override fun onError(t: Throwable?) {
            println("Connection Lost: ${t?.message}")
            exitProcess(1)
        }

        override fun onCompleted() {
            println("Server Closed the connection")
            exitProcess(0)
        }
    }

    //Open the stream. This is how we send data.
    val requestObserver : StreamObserver<Chat.ChatEvent> = asyncStub.chatStream(responseObserver)

    while(true){
        val input = readln()

        if(input.lowercase() == "exit") break

        if(input.isBlank() || input.isEmpty()) continue

        val chatMessage = Chat.ChatMessage.newBuilder()
            .setMessageId("") //Never used DB generates its own messageIDs.
            .setUsername(username)
            .setRoomId(roomId)
            .setContent(input)
            .setTimestamp(System.currentTimeMillis())
            .build()

        requestObserver.onNext(Chat.ChatEvent.newBuilder().setMessage(chatMessage).build())
    }

    //Clean exit.
    println("EXITING CHAT")
    requestObserver.onCompleted()
    channel.shutdown()

}

private fun attemptJoinRoom(blockingStub: ChatServiceGrpc.ChatServiceBlockingStub,roomId: String,username: String): Boolean {
    return try {
        val response = blockingStub.joinRoom(
            Chat.JoinRoomRequest.newBuilder()
                .setUsername(username)
                .setRoomId(roomId)
                .build()
        )
        println("Successfully joined the room!")
        true

    } catch (e: StatusRuntimeException) {
        println("Failed to connect to the room: $e.status")
        false
    }
}

private fun displayMessageHistory(blockingStub: ChatServiceGrpc.ChatServiceBlockingStub, roomId: String) {
    try {
        val history = blockingStub.getHistory(
            Chat.GetHistoryRequest.newBuilder()
                .setRoomId(roomId)
                .build()
        )

        println(">>> Room History:")
        history.messagesList.forEach { message ->
            println("[${message.username}]: ${message.content}")
        }
        println(">>> End of Room History")
    } catch (e: StatusRuntimeException) {
        println("Could not load the History!") //We dont want to terminate program here, lets try to join stream.
    }
}