package leon.patmore.encryption.mongo

import leon.patmore.encryption.Encrypted
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cards")
data class Card(@Encrypted val number: String, @Id val id: ObjectId? = null)
