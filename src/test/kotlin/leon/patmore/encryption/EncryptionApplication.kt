package leon.patmore.encryption

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class EncryptionApplication

fun main(args: Array<String>) {
    runApplication<EncryptionApplication>(*args)
}
