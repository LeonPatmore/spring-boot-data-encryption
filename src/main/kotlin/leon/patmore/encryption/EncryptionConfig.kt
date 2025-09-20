package leon.patmore.encryption

import com.amazonaws.encryptionsdk.AwsCrypto
import com.amazonaws.encryptionsdk.CryptoAlgorithm
import com.amazonaws.encryptionsdk.CryptoMaterialsManager
import com.amazonaws.encryptionsdk.DefaultCryptoMaterialsManager
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKeyProvider
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import java.util.concurrent.TimeUnit

@Configuration
class EncryptionConfig {

    @Bean
    fun kmsMasterKeyProvider(): KmsMasterKeyProvider {
        return KmsMasterKeyProvider.builder()
            .defaultRegion(Region.EU_WEST_1)
            .buildStrict("arn:aws:kms:eu-west-1:136306849848:key/b74e3f6d-67c8-425a-a4de-2d9b1524dee4")
    }

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

    @Bean
    fun encryptionInterceptor(encryptionService: EncryptionService): EncryptionInterceptor {
        return EncryptionInterceptor(encryptionService)
    }

    @Bean
    fun hibernatePropertiesCustomizer(interceptor: EncryptionInterceptor): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { props: MutableMap<String?, Any?>? ->
            props!!.put(
                "hibernate.session_factory.interceptor",
                interceptor,
            )
        }
    }
}
