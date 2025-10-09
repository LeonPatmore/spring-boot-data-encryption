package leon.patmore.encryption

import leon.patmore.encryption.hibernate.User
import leon.patmore.encryption.hibernate.UserRepository
import leon.patmore.encryption.mongo.Address
import leon.patmore.encryption.mongo.Card
import leon.patmore.encryption.mongo.CardRepository
import leon.patmore.encryption.mongo.Pin
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.util.UUID.randomUUID

@SpringBootTest
class EncryptionApplicationTests {
    @Autowired
    private lateinit var repository: UserRepository

    @Autowired
    private lateinit var mongoRepository: CardRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @MockitoSpyBean
    private lateinit var encryptionService: EncryptionService

    private val decryptResponses = mutableListOf<String>()

    @BeforeEach
    fun setUp() {
        decryptResponses.clear()
        doAnswer { invocation ->
            val result = invocation.callRealMethod()
            decryptResponses.add((result as ByteArray).decodeToString())
            result
        }.`when`(encryptionService).decrypt(any())
    }

    @Test
    fun `test postgresql`() {
        val user = repository.save(User("Leon", "Patmore", "abc123"))

        val foundUser = repository.findById(user.id!!).get()
        // Last name is not lazy, so should be decrypted immediately. Nothing else should be decrypted yet.
        assertEquals(listOf("Patmore"), decryptResponses)

        assertEquals(user.ssn, foundUser.ssn)
        assertEquals(user.firstName, foundUser.firstName)

        // First name has now been accessed, so should be in the list.
        assertEquals(listOf("Patmore", "Leon"), decryptResponses)

        repeat(5) {
            assertEquals(user.firstName, foundUser.firstName)
            assertEquals(user.lastName, foundUser.lastName)
        }

        // We should only decrypt each field once.
        assertEquals(listOf("Patmore", "Leon"), decryptResponses)
    }

    @Test
    fun `test load`() {
        repository.findAll().forEach { println("user [ ${it.firstName} ], SSN [ ${it.ssn} ]") }
    }

    @Test
    fun `test mongo`() {
        val card =
            mongoRepository.save(
                Card(
                    "124321",
                    existingPii = "someValue",
                    address = Address("123", listOf("1", "2")),
                    pins = listOf(Pin("1234", true), Pin("4567", false)),
                ),
            )

        val card1 = mongoRepository.findById(card.id!!).get()
        assertEquals(card.number, card1.number)
        assertEquals(card.existingPii, card1.existingPii)

        removeMongoField(card.id, "existingPii")

        val card2 = mongoRepository.findById(card.id).get()
        assertEquals(card.number, card2.number)
        assertEquals(card.existingPii, card2.existingPii)
    }

    @Test
    fun `test mongo query by example`() {
        val bank = randomUUID().toString()

        mongoRepository.save(Card("123", bank = bank))
        mongoRepository.save(Card("123", bank = bank))

        val matcher = ExampleMatcher.matching().withIgnorePaths("id", "number", "existingPii")
        val example = Example.of(Card("", bank = bank), matcher)
        val cards = mongoRepository.findAll(example)

        assertEquals(2, cards.count())
        cards.forEach {
            assertEquals("123", it.number)
        }
    }

    private fun removeMongoField(
        id: ObjectId,
        fieldName: String,
    ) {
        val query = Query(Criteria.where("_id").`is`(id))
        val update = Update().unset(fieldName)
        mongoTemplate.updateFirst(query, update, "cards")
    }
}
