package leon.patmore.encryption

import com.amazonaws.encryptionsdk.AwsCrypto
import com.amazonaws.encryptionsdk.CryptoAlgorithm
import com.amazonaws.encryptionsdk.CryptoMaterialsManager
import com.amazonaws.encryptionsdk.DefaultCryptoMaterialsManager
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class EncryptionConfig {

    @Bean
    fun awsCrypto(): AwsCrypto = AwsCrypto.builder()
        .withEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_256_GCM_HKDF_SHA512_COMMIT_KEY)
        .build()

    @Bean
    fun materialsCache() = LocalCryptoMaterialsCache(100)

    @Bean
    fun cryptoMaterialsManager(
        meterRegistry: MeterRegistry,
        kmsMasterKeyProvider: KmsMasterKeyProvider,
        cryptoMaterialsCache: CryptoMaterialsCache,
    ): CryptoMaterialsManager {
        val cryptoMaterialsManager = ObservedCryptoMaterialsManager(
            meterRegistry,
            DefaultCryptoMaterialsManager(kmsMasterKeyProvider),
        )
        return CachingCryptoMaterialsManager.newBuilder()
            .withBackingMaterialsManager(cryptoMaterialsManager)
            .withCache(cryptoMaterialsCache)
            .withMaxAge(5, TimeUnit.SECONDS)
            .withMessageUseLimit(5)
            .build()
    }
}
