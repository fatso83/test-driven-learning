package io.github.fatso83.spring_learning

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@SpringJUnitConfig(SpringAsyncTest.MyTestConfig::class)
class SpringAsyncTest {

    @Autowired
    lateinit var sut: MySpecialBean

    @Autowired
    lateinit var scheduler: ManualScheduler

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

    @Configuration
    @EnableAsync
    class MyTestConfig {
        @Bean(name = ["myThreadPoolExecutor"])
        fun asyncExecutor(): ThreadPoolTaskExecutor {
            val executor = ThreadPoolTaskExecutor()
            executor.corePoolSize = 10
            executor.maxPoolSize = 10
            executor.queueCapacity = 2000
            executor.setThreadNamePrefix("batchProcess-")
            executor.setWaitForTasksToCompleteOnShutdown(true) // to make sure it wait till the current execution complete before pod shutdown.
            executor.setAwaitTerminationSeconds(300)
            executor.initialize()
            return executor
        }

        @Bean
        fun sut(): MySpecialBean = MySpecialBean()

        @Bean
        fun manualScheduler(myThreadPoolExecutor: ThreadPoolTaskExecutor): ManualScheduler =
            ManualScheduler(myThreadPoolExecutor);
    }

    @Component
    class ManualScheduler(val executor: ThreadPoolTaskExecutor) {
        fun manuallyScheduledTask(function: () -> CompletableFuture<Void?>): CompletableFuture<Void?> {
            return CompletableFuture.supplyAsync({
                println("Executing on thread: ${Thread.currentThread().name}")
                Thread.sleep(1000)  // Simulate a long-running task
                null
            }, executor)
        }
    }

    @Test
    @Throws(InterruptedException::class, ExecutionException::class)
    fun testSpringAsync() {
        val startTime = System.currentTimeMillis()

        CompletableFuture.allOf(
            sut.aFutureCompletable(),
            sut.aFutureCompletable(),
            sut.aFutureCompletable(),
            sut.aFutureCompletable(),
            sut.aFutureCompletable()
        ).get()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        println("Test took $duration milliseconds")
    }

    @Test
    @Throws(InterruptedException::class, ExecutionException::class)
    fun testManualScheduler() {
        val startTime = System.currentTimeMillis()

        CompletableFuture.allOf(
            scheduler.manuallyScheduledTask { sut.aFutureCompletable() },
            scheduler.manuallyScheduledTask { sut.aFutureCompletable() },
            scheduler.manuallyScheduledTask { sut.aFutureCompletable() },
            scheduler.manuallyScheduledTask { sut.aFutureCompletable() },
            scheduler.manuallyScheduledTask { sut.aFutureCompletable() },
        ).get()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        println("Test took $duration milliseconds")
    }
}