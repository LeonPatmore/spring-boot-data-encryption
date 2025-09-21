package leon.patmore.encryption.hibernate

import leon.patmore.encryption.EncryptionService
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HibernateEncryptionConfig {

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
