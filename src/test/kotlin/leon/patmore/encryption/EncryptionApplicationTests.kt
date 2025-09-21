package leon.patmore.encryption

import leon.patmore.encryption.hibernate.User
import leon.patmore.encryption.hibernate.UserRepository
import leon.patmore.encryption.mongo.Card
import leon.patmore.encryption.mongo.CardRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class EncryptionApplicationTests {

    @Autowired
    private lateinit var encryptionService: EncryptionService

    @Autowired
    private lateinit var repository: UserRepository

    @Autowired
    private lateinit var mongoRepository: CardRepository

    @Test
    fun contextLoads() {
        val data = "Hello, World!".toByteArray()
        val cipertext = encryptionService.encrypt(data)

        val plaintext = encryptionService.decrypt(cipertext)

        print(plaintext.decodeToString())
        print(data.decodeToString())

        repository.save(User("Leon", "abc123"))

        repository.findAll().forEach { println("user [ ${it.firstName} ], SSN [ ${it.ssn} ]") }
    }

    @Test
    fun `test load`() {
        repository.findAll().forEach { println("user [ ${it.firstName} ], SSN [ ${it.ssn} ]") }
    }

    @Test
    fun `test mongo`() {
        val card = mongoRepository.save(Card("124321"))

        val finalCard = mongoRepository.findById(card.id!!).get()
        assertEquals(card.number, finalCard.number)
    }
}
