package io.github.fatso83.spring_learning

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class MySpecialBean {

    @Throws(InterruptedException::class)
    @Async("myThreadPoolExecutor") // Use the custom executor
    fun aFutureCompletable(): CompletableFuture<Void?> {
        println("Executing on thread: ${Thread.currentThread().name}")
        Thread.sleep(1000)  // Simulate a long-running task
        return CompletableFuture.completedFuture(null)
    }
}