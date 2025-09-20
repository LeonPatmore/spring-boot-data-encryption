package leon.patmore.encryption

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EncryptionApplicationTests {

    @Autowired
    private lateinit var encryptionService: EncryptionService

    @Autowired
    private lateinit var repository: UserRepository

    @Test
    fun contextLoads() {
        val data = "Hello, World!".toByteArray()
        val cipertext = encryptionService.encrypt(data)

        val plaintext = encryptionService.decrypt(cipertext)

        print(plaintext.decodeToString())
        print(data.decodeToString())

        repository.save(User("Leon"))

        repository.findAll().forEach { println("user: " + it.firstName) }
    }
}
