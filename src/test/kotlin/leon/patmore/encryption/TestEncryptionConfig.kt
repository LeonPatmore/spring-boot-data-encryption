package leon.patmore.encryption

import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKeyProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region

@Configuration
class TestEncryptionConfig {
    @Bean
    fun kmsMasterKeyProvider(): KmsMasterKeyProvider =
        KmsMasterKeyProvider
            .builder()
            .defaultRegion(Region.EU_WEST_1)
            .buildStrict("arn:aws:kms:eu-west-1:136306849848:key/b74e3f6d-67c8-425a-a4de-2d9b1524dee4")
}
