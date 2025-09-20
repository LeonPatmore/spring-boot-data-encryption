package leon.patmore.encryption

import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.AbstractPersistable

@Entity
@Table(name = "users")
data class User(
    @Transient val firstName: String?,
    @Encrypted("firstName") val firstNameEncrypted: ByteArray? = null,
) : AbstractPersistable<Long>() {
    constructor() : this(null, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (firstName != other.firstName) return false
        if (firstNameEncrypted != null) {
            if (other.firstNameEncrypted == null) return false
            if (!firstNameEncrypted.contentEquals(other.firstNameEncrypted)) return false
        } else if (other.firstNameEncrypted != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (firstNameEncrypted?.contentHashCode() ?: 0)
        return result
    }
}
