package leon.patmore.encryption.mongo

import org.bson.types.ObjectId
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.stereotype.Repository

@Repository
interface CardRepository :
    CrudRepository<Card, ObjectId>,
    QueryByExampleExecutor<Card>
