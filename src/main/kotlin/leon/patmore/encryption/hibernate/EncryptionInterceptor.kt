package leon.patmore.encryption.hibernate

import leon.patmore.encryption.Encrypted
import leon.patmore.encryption.EncryptionService
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import org.hibernate.Interceptor
import org.hibernate.metamodel.RepresentationMode
import org.hibernate.metamodel.spi.EntityRepresentationStrategy
import org.hibernate.type.Type
import org.springframework.data.jpa.domain.AbstractPersistable
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class EncryptionInterceptor(
    private val encryptionService: EncryptionService,
) : Interceptor {
    override fun instantiate(
        entityName: String?,
        representationStrategy: EntityRepresentationStrategy?,
        id: Any,
    ): Any = generateUserObject(id)

    override fun instantiate(
        entityName: String?,
        representationMode: RepresentationMode?,
        id: Any,
    ): Any = generateUserObject(id)

    private fun generateUserObject(id: Any): User {
        val proxyClass =
            ByteBuddy()
                .subclass(User::class.java)
                .method(ElementMatchers.isGetter())
                .intercept(MethodDelegation.to(LazyDecryptInterceptor(encryptionService)))
                .make()
                .load(User::class.java.classLoader)
                .loaded

        val user = proxyClass.getDeclaredConstructor().newInstance()
        val field = AbstractPersistable::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(user, id)

        return user
    }

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
            val targetField = annotation.encryptedFieldName
            if (targetField.isEmpty()) {
                throw Exception("Target field required")
            }

            val targetValue = (prop.getter.call(entity) as? String) ?: continue

            val targetIndex = propertyNames.indexOf(targetField)
            if (targetIndex >= 0) {
                println("Updating state for name ${prop.name}")
                state[targetIndex] = encryptionService.encrypt(targetValue.toByteArray())
            } else {
                throw Exception("Target field does not exist in entity")
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
            val targetField = annotation.encryptedFieldName

            val encryptedIndex = propertyNames.indexOf(targetField)
            if (encryptedIndex >= 0) {
                val encryptedData = state[encryptedIndex] as? ByteArray ?: continue
                val decrypted = String(encryptionService.decrypt(encryptedData))

                prop.javaField?.let { field ->
                    field.isAccessible = true
                    field.set(entity, decrypted)
                    modified = true

                    val plaintextIndex = propertyNames.indexOf(prop.name)
                    if (plaintextIndex >= 0) {
                        state[plaintextIndex] = decrypted
                    }
                }
            }
        }

        return modified
    }
}
