package io.github.fatso83.spring_learning

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


@SpringJUnitConfig(SpringAsyncTest.MyTestConfig::class)
class SpringAsyncTest {

    @Autowired
    lateinit var sut: MySpecialBean


    @TestConfiguration
    class MyTestConfig {
        @Bean(name = ["myThreadPoolExecutor"])
        fun asyncExecutor(): ThreadPoolTaskExecutor {
            val executor = ThreadPoolTaskExecutor()
            executor.corePoolSize = 4
            executor.maxPoolSize = 4
            executor.queueCapacity = 2000
            executor.setThreadNamePrefix("batchProcess-")
            executor.setWaitForTasksToCompleteOnShutdown(true) // to make sure it wait till the current execution complete before pod shutdown.
            executor.setAwaitTerminationSeconds(300)
            executor.initialize()
            return executor
        }

        @Bean
        fun sut(): MySpecialBean = MySpecialBean()
    }

    @Component
    open class MySpecialBean {

//        @Async
        @Throws(InterruptedException::class)
        @Async("myThreadPoolExecutor") // Use the custom executor
        open fun aFutureCompletable(): CompletableFuture<Void?> {
            Thread.sleep(1000)
            return CompletableFuture.completedFuture(null)
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
}