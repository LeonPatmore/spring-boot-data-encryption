package leon.patmore.encryption

import com.amazonaws.encryptionsdk.AwsCrypto
import com.amazonaws.encryptionsdk.CryptoMaterialsManager
import org.springframework.stereotype.Service

@Service
class EncryptionService(
    val awsCrypto: AwsCrypto,
    val materialsManager: CryptoMaterialsManager,
) {
    fun encrypt(data: ByteArray): ByteArray {
        return awsCrypto.encryptData(materialsManager, data).result
    }

    fun decrypt(data: ByteArray): ByteArray {
        return awsCrypto.decryptData(materialsManager, data).result
    }
}
