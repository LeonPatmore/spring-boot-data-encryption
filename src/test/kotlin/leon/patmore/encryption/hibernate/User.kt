package leon.patmore.encryption.hibernate

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Transient
import leon.patmore.encryption.Encrypted
import org.springframework.data.jpa.domain.AbstractPersistable

@Entity
@Table(name = "users")
open class User(
    @Transient
    @Encrypted
    open var firstName: String? = null,
    @Transient
    @Encrypted(lazy = false)
    open var lastName: String? = null,
    @Encrypted
    open var ssn: String? = null,
    open var firstNameEncrypted: ByteArray? = null,
    open var lastNameEncrypted: ByteArray? = null,
    open var ssnEncrypted: ByteArray? = null,
) : AbstractPersistable<Long>()
