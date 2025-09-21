package leon.patmore.encryption

import com.amazonaws.encryptionsdk.CryptoMaterialsManager
import com.amazonaws.encryptionsdk.model.DecryptionMaterials
import com.amazonaws.encryptionsdk.model.DecryptionMaterialsRequest
import com.amazonaws.encryptionsdk.model.EncryptionMaterials
import com.amazonaws.encryptionsdk.model.EncryptionMaterialsRequest
import io.micrometer.core.instrument.MeterRegistry

class ObservedCryptoMaterialsManager(
    private val meterRegistry: MeterRegistry,
    private val delegate: CryptoMaterialsManager,
) : CryptoMaterialsManager {
    override fun getMaterialsForEncrypt(request: EncryptionMaterialsRequest?): EncryptionMaterials {
        meterRegistry.counter("encryption_requests_total").increment()
        return delegate.getMaterialsForEncrypt(request)
    }

    override fun decryptMaterials(request: DecryptionMaterialsRequest?): DecryptionMaterials {
        meterRegistry.counter("decryption_requests_total").increment()
        return delegate.decryptMaterials(request)
    }
}
