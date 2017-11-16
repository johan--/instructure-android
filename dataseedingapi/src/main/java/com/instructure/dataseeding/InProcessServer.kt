package com.instructure.dataseeding

import com.instructure.soseedy.SoSeedyGrpc
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import java.util.concurrent.TimeUnit

object InProcessServer {
    private val UNIQUE_SERVER_NAME = "android-soseedy"
    private val server = InProcessServerBuilder.forName(UNIQUE_SERVER_NAME)
            .addService(SoSeedyImpl()).directExecutor().build()
    private val channel = InProcessChannelBuilder.forName(UNIQUE_SERVER_NAME).directExecutor().build()

    val stub: SoSeedyGrpc.SoSeedyBlockingStub = SoSeedyGrpc.newBlockingStub(channel)

    init {
        server.start()
    }

    fun stop() {
        channel.shutdown()
        server.shutdown()
        channel.awaitTermination(1, TimeUnit.MINUTES)
        server.awaitTermination(1, TimeUnit.MINUTES)
    }
}
