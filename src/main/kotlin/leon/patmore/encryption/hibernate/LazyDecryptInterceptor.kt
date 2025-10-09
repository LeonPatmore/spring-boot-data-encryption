package leon.patmore.encryption.hibernate

import leon.patmore.encryption.Encrypted
import leon.patmore.encryption.EncryptionService
import net.bytebuddy.implementation.bind.annotation.Origin
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.SuperCall
import net.bytebuddy.implementation.bind.annotation.This
import java.util.concurrent.Callable

class LazyDecryptInterceptor(
    private val encryptionService: EncryptionService,
) {
    private val cache = mutableMapOf<String, String?>()

    @RuntimeType
    fun intercept(
        @Origin method: java.lang.reflect.Method,
        @SuperCall superCall: Callable<Any>,
        @This obj: Any,
    ): Any? {
        val fieldName = method.name.removePrefix("get").replaceFirstChar { it.lowercaseChar() }

        // Already cached?
        if (cache.containsKey(fieldName)) {
            return cache[fieldName]
        }

        // Call the real getter (will just return null or the already-set plaintext)
        val value = superCall.call()

        if (value != null) {
            cache[fieldName] = value as String
            return value
        }

        // Find the encrypted field from annotation
        val realClass = realClass(obj)
        val field = realClass.declaredFields.firstOrNull { it.name == fieldName }
        val annotation = field?.getAnnotation(Encrypted::class.java)
        if (annotation != null) {
            val encryptedField = realClass.getDeclaredField(annotation.encryptedFieldName)
            encryptedField.isAccessible = true
            val encryptedData = encryptedField.get(obj) as? ByteArray
            if (encryptedData != null) {
                val decrypted = String(encryptionService.decrypt(encryptedData))
                cache[fieldName] = decrypted
                return decrypted
            }
        }

        return null
    }

    fun realClass(obj: Any): Class<*> {
        var clazz = obj::class.java
        while (clazz.name.contains("ByteBuddy")) {
            clazz = clazz.superclass
        }
        return clazz
    }
}
