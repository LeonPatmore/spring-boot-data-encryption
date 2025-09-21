package leon.patmore.encryption.hibernate

// class EncryptedConverter : AttributeConverter<Any, ByteArray> {
//    private val encryptionService = EncryptionService(AwsCryptoProvider.awsCrypto, AwsCryptoProvider.cmm)
//
//    override fun convertToDatabaseColumn(attribute: Any?): ByteArray? {
//        return attribute?.toString()?.toByteArray()?.let { encryptionService.encrypt(it) }
//    }
//
//    override fun convertToEntityAttribute(dbData: ByteArray?): Any? {
//        return dbData?.let { String(encryptionService.decrypt(it)) }
//    }
// }
