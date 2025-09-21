package leon.patmore.encryption

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Encrypted(val encryptedFieldName: String = "")
