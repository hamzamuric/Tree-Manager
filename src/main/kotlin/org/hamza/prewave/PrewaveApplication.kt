package org.hamza.prewave

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrewaveApplication

fun main(args: Array<String>) {
    runApplication<PrewaveApplication>(*args)
}
