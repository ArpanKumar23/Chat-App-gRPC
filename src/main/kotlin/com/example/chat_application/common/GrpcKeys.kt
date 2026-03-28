package com.example.chat_application.common

import io.grpc.Context
import io.grpc.Metadata

object GrpcKeys {

    val ROOM_ID_META_KEY : Metadata.Key<String> = Metadata.Key.of("room-id", Metadata.ASCII_STRING_MARSHALLER)
    val USERNAME_META_KEY : Metadata.Key<String> = Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER)

    val ROOM_ID_CONTEXT_KEY : Context.Key<String> = Context.key("room-id")
    val USERNAME_CONTEXT_KEY : Context.Key<String> = Context.key("username")
}