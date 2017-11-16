package com.instructure.dataseeding

import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException

class SoSeedyServer {
    private val port = 50051
    private val server: Server = ServerBuilder.forPort(port)
            .addService(SoSeedyImpl())
            .build()

    @Throws(IOException::class)
    internal fun start() {
        server.start()
        println("Server started on port $port")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("JVM shutdown hook activated. Shutting down...")
                this@SoSeedyServer.stop()
                System.err.println("Server shut down.")
            }
        })
    }

    internal fun stop() {
        server.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    internal fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main(args: Array<String>) {
    val server = SoSeedyServer()
    server.start()
    server.blockUntilShutdown()
}
