package leon.patmore.encryption

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Encrypted(
    val lazy: Boolean = true,
    val encryptedFieldName: String = "",
)
