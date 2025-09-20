package leon.patmore.encryption

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long>
