package io.github.fatso83.spring_learning

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringLearningApplication

fun main(args: Array<String>) {
	runApplication<SpringLearningApplication>(*args)
}
