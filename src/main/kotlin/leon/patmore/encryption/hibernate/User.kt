package leon.patmore.encryption.hibernate

import jakarta.persistence.Entity
import jakarta.persistence.Table
import leon.patmore.encryption.Encrypted
import org.springframework.data.jpa.domain.AbstractPersistable

@Entity
@Table(name = "users")
open class User(
    @Transient
    @Encrypted("firstNameEncrypted")
    open var firstName: String? = null,
    @Encrypted("ssnEncrypted")
    open var ssn: String? = null,
    open var firstNameEncrypted: ByteArray? = null,
    open var ssnEncrypted: ByteArray? = null,
) : AbstractPersistable<Long>()
