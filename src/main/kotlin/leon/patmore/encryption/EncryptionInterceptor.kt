package leon.patmore.encryption

import org.hibernate.Interceptor
import org.hibernate.type.Type
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class EncryptionInterceptor(private val encryptionService: EncryptionService) : Interceptor {

    override fun onPersist(
        entity: Any,
        id: Any,
        state: Array<Any>,
        propertyNames: Array<String>,
        types: Array<Type>,
    ): Boolean {
        val kClass = entity::class
        for (prop in kClass.memberProperties) {
            val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: continue
            val targetField = annotation.targetField

            val targetProp = kClass.memberProperties.find { it.name == targetField } ?: continue
            val targetValue = (targetProp.getter.call(entity) as? String) ?: continue

            val index = propertyNames.indexOf(prop.name)
            if (index >= 0) {
                state[index] = encryptionService.encrypt(targetValue.toByteArray())
            }
        }

        return true
    }

    override fun onLoad(
        entity: Any,
        id: Any,
        state: Array<Any>,
        propertyNames: Array<String>,
        types: Array<Type>,
    ): Boolean {
        val kClass = entity::class
        var modified = false

        for (prop in kClass.memberProperties) {
            val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: continue
            val targetField = annotation.targetField

            val index = propertyNames.indexOf(prop.name)
            if (index >= 0) {
                val encryptedData = state[index] as? ByteArray ?: continue
                val decrypted = String(encryptionService.decrypt(encryptedData))

                // Find the target property and set the decrypted value
                val targetProp = kClass.memberProperties.find { it.name == targetField } ?: continue
                targetProp.javaField?.let { field ->
                    field.isAccessible = true
                    field.set(entity, decrypted)
                    modified = true
                }
            }
        }

        return modified
    }
}
