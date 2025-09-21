package leon.patmore.encryption.hibernate

import jakarta.persistence.Entity
import jakarta.persistence.Table
import leon.patmore.encryption.Encrypted
import org.springframework.data.jpa.domain.AbstractPersistable

@Entity
@Table(name = "users")
data class User(
    @Transient @Encrypted("firstNameEncrypted") val firstName: String?,
    @Encrypted("ssnEncrypted") val ssn: String?,
    val firstNameEncrypted: ByteArray? = null,
    val ssnEncrypted: ByteArray? = null,
) : AbstractPersistable<Long>() {
    constructor() : this(null, null)
}
