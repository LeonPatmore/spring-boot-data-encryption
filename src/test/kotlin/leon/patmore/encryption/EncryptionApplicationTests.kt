package leon.patmore.encryption

import leon.patmore.encryption.hibernate.User
import leon.patmore.encryption.hibernate.UserRepository
import leon.patmore.encryption.mongo.Address
import leon.patmore.encryption.mongo.Card
import leon.patmore.encryption.mongo.CardRepository
import leon.patmore.encryption.mongo.Pin
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.util.UUID.randomUUID
import kotlin.test.assertEquals

@SpringBootTest
class EncryptionApplicationTests {

    @Autowired
    private lateinit var repository: UserRepository

    @Autowired
    private lateinit var mongoRepository: CardRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Test
    fun `test postgresql`() {
        val user = repository.save(User("Leon", "abc123"))

        val foundUser = repository.findById(user.id!!).get()
        assertEquals(user.ssn, foundUser.ssn)
        assertEquals(user.firstName, foundUser.firstName)
    }

    @Test
    fun `test load`() {
        repository.findAll().forEach { println("user [ ${it.firstName} ], SSN [ ${it.ssn} ]") }
    }

    @Test
    fun `test mongo`() {
        val card = mongoRepository.save(
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

    private fun removeMongoField(id: ObjectId, fieldName: String) {
        val query = Query(Criteria.where("_id").`is`(id))
        val update = Update().unset(fieldName)
        mongoTemplate.updateFirst(query, update, "cards")
    }
}
