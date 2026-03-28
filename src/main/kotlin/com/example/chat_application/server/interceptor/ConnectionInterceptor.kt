package com.example.chat_application.server.interceptor

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import com.example.chat_application.common.GrpcKeys
import com.example.chat_application.common.GrpcKeys.ROOM_ID_META_KEY
import com.example.chat_application.common.GrpcKeys.USERNAME_META_KEY
import io.grpc.*

@GrpcGlobalServerInterceptor
class ConnectionInterceptor: ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT>{

        //Get the data from the headers.
        val roomId = headers.get(ROOM_ID_META_KEY)
        val username = headers.get(USERNAME_META_KEY)

        //We attach our data to the context then tell gRPc to continue processing and send this to our ServiceImpl.
        val context = Context.current()
            .withValue(GrpcKeys.ROOM_ID_CONTEXT_KEY,roomId)
            .withValue(GrpcKeys.USERNAME_CONTEXT_KEY,username)

        return Contexts.interceptCall(context, call, headers, next)
    }
}