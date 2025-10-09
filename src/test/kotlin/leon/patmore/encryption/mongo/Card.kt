package leon.patmore.encryption.mongo

import leon.patmore.encryption.Encrypted
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cards")
data class Card(
    @Encrypted val number: String,
    @Encrypted(encryptedFieldName = "existingPiiEncrypted") val existingPii: String? = null,
    val address: Address? = null,
    val bank: String? = null,
    val pins: List<Pin>? = emptyList(),
    @Id val id: ObjectId? = null,
)

data class Address(
    @Encrypted val postCode: String,
    val previousPostCodes: List<String> = emptyList(),
)

data class Pin(
    @Encrypted val pin: String,
    val active: Boolean,
)
