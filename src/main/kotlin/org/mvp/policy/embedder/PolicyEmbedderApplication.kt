package org.mvp.policy.embedder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PolicyEmbedderApplication

fun main(args: Array<String>) {
    runApplication<PolicyEmbedderApplication>(*args)
}
