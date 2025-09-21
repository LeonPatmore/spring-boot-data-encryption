package leon.patmore.encryption.hibernate

import jakarta.persistence.Entity
import jakarta.persistence.Table
import leon.patmore.encryption.Encrypted
import org.springframework.data.jpa.domain.AbstractPersistable

@Entity
@Table(name = "users")
data class User(
    @Transient val firstName: String?,
    val ssn: String?,
    @Encrypted("firstName") val firstNameEncrypted: ByteArray? = null,
    @Encrypted("ssn") val ssnEncrypted: ByteArray? = null,
) : AbstractPersistable<Long>() {
    constructor() : this(null, null)
}
