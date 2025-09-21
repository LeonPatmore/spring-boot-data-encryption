package leon.patmore.encryption.mongo

import leon.patmore.encryption.Encrypted
import leon.patmore.encryption.EncryptionService
import org.bson.Document
import org.bson.types.Binary
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Component
class GenericEncryptionListener(
    private val encryptionService: EncryptionService,
) {

    @EventListener
    fun beforeSave(event: BeforeSaveEvent<Any>) {
        if (event.document != null) {
            encryptFields(event.document!!, event.source)
        }
    }

    @EventListener
    fun afterLoad(event: AfterLoadEvent<Any>) {
        if (event.document != null) {
            decryptFields(event.document!!, event.type)
        }
    }

    private fun encryptFields(document: Document, entity: Any) {
        for (prop in entity::class.memberProperties) {
            val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: continue

            val targetField = annotation.encryptedFieldName.ifEmpty { prop.name }

            val targetValue = (prop.getter.call(entity) as? String) ?: continue

            val ciphertext = encryptionService.encrypt(targetValue.toByteArray())

            document[targetField] = ciphertext
        }
    }

    private fun decryptFields(document: Document, entity: Class<Any>) {
        val kClass = entity.kotlin
        for (prop in kClass.memberProperties) {
            val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: continue
            val targetField = annotation.encryptedFieldName.ifEmpty { prop.name }

            val encryptedValue = (document[targetField] as? Binary)?.data ?: continue
            val decrypted = String(encryptionService.decrypt(encryptedValue))

            document[prop.name] = decrypted
        }
    }
}
