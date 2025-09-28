# Spring Boot Data Encryption Library

A comprehensive Spring Boot library that provides transparent field-level encryption for both JPA/Hibernate (PostgreSQL) and MongoDB databases using AWS KMS encryption.

## Features

- **Transparent Encryption**: Automatically encrypts data on save and decrypts on load
- **Multi-Database Support**: Works with both PostgreSQL (JPA/Hibernate) and MongoDB
- **AWS KMS Integration**: Uses AWS KMS for secure key management
- **Field-Level Encryption**: Encrypt only specific fields using annotations
- **Metrics Support**: Built-in Micrometer metrics for encryption operations
- **Nested Object Support**: Supports encryption in nested objects and collections

## Setup Requirements

**Important**: This library requires you to configure a `KmsMasterKeyProvider` bean in your application for the encryption to work. The library provides the encryption infrastructure but does not include the AWS KMS configuration.

### Required Bean Configuration

You must provide a `KmsMasterKeyProvider` bean in your application configuration:

```kotlin
@Configuration
class YourEncryptionConfig {
    
    @Bean
    fun kmsMasterKeyProvider(): KmsMasterKeyProvider {
        return KmsMasterKeyProvider.builder()
            .defaultRegion(Region.US_EAST_1) // Your preferred region
            .buildStrict("arn:aws:kms:us-east-1:123456789012:key/your-key-id")
    }
}
```

**Configuration Requirements:**
- Replace the KMS key ARN with your actual AWS KMS key
- Set the appropriate AWS region for your key
- Ensure your application has the necessary AWS permissions to use the KMS key
- The key must be configured for encryption/decryption operations

**AWS Permissions Required:**
- `kms:Encrypt`
- `kms:Decrypt`
- `kms:GenerateDataKey`
- `kms:DescribeKey`

### Database Configuration

Configure your database connections in your `application.yml` or `application.properties`:

```yaml
spring:
  application:
    name: encryption
  datasource:
    url: jdbc:postgresql://localhost:5432/demo
    username: demo
    password: demo
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true
  data:
    mongodb:
      username: myappuser
      password: mypassword
      database: myappdb
      host: localhost
      port: 27017
```

## Usage

### 1. The @Encrypted Annotation

Use the `@Encrypted` annotation to mark fields that should be encrypted:

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Encrypted(val encryptedFieldName: String = "")
```

### 2. JPA/Hibernate Entities

For PostgreSQL entities, use the `@Encrypted` annotation with a target field name:

```kotlin
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
```

**Key Points:**
- Use `@Transient` for the original field to prevent Hibernate from persisting it
- Create a separate field to store the encrypted data (e.g., `firstNameEncrypted`)
- Specify the encrypted field name in the annotation

### 3. MongoDB Documents

For MongoDB documents, the encryption is more flexible:

```kotlin
@Document(collection = "cards")
data class Card(
    @Encrypted val number: String,
    @Encrypted(encryptedFieldName = "existingPiiEncrypted") val existingPii: String? = null,
    val address: Address? = null,
    val bank: String? = null,
    val pins: List<Pin>? = emptyList(),
    @Id val id: ObjectId? = null,
)

data class Address(@Encrypted val postCode: String, val previousPostCodes: List<String> = emptyList())
data class Pin(@Encrypted val pin: String, val active: Boolean)
```

**Key Points:**
- MongoDB automatically handles the encrypted field storage
- Supports nested objects with encrypted fields
- Supports collections with encrypted fields
- No need for separate encrypted field declarations

### 4. Repository Usage

Use your repositories normally - encryption/decryption happens automatically:

```kotlin
// JPA Repository
@Repository
interface UserRepository : JpaRepository<User, Long>

// MongoDB Repository
@Repository
interface CardRepository : CrudRepository<Card, ObjectId>, QueryByExampleExecutor<Card>
```

## How It Works

### Hibernate Integration

- Uses a custom `EncryptionInterceptor` that implements Hibernate's `Interceptor` interface
- Automatically encrypts fields marked with `@Encrypted` during `onPersist()`
- Automatically decrypts fields during `onLoad()`
- Stores encrypted data as `ByteArray` in the database

### MongoDB Integration

- Uses Spring Data MongoDB event listeners (`BeforeSaveEvent` and `AfterLoadEvent`)
- Automatically encrypts fields during document save operations
- Automatically decrypts fields during document load operations
- Stores encrypted data as `Binary` objects in MongoDB
- Tracks encrypted fields in a special `_encryptedFields` array

## Metrics

The library includes built-in metrics for monitoring encryption operations:

- `encryption_requests_total`: Counter for encryption operations
- `decryption_requests_total`: Counter for decryption operations

Access metrics via Spring Boot Actuator endpoints.

## Testing

### Starting the Required Services

Before running tests, start the required databases using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- MongoDB database on port 27017

### Running Tests

The library includes comprehensive tests demonstrating both JPA and MongoDB usage:

```kotlin
@Test
fun `test postgresql`() {
    val user = repository.save(User("Leon", "abc123"))
    val foundUser = repository.findById(user.id!!).get()
    assertEquals(user.ssn, foundUser.ssn)
    assertEquals(user.firstName, foundUser.firstName)
}

@Test
fun `test mongo`() {
    val card = mongoRepository.save(
        Card(
            "124321",
            existingPii = "someValue",
            address = Address("123", listOf("1", "2")),
            pins = listOf(Pin("1234", true), Pin("4567", false)),
        ),
    )
    
    val card1 = mongoRepository.findById(card.id!!).get()
    assertEquals(card.number, card1.number)
    assertEquals(card.existingPii, card1.existingPii)
}
```

## Security Considerations

1. **Key Management**: Uses AWS KMS for secure key management
2. **Encryption Algorithm**: Uses AES-256-GCM with HKDF-SHA512
3. **Caching**: Implements local caching for encryption materials (100 items)
4. **Region**: Configured for EU West 1 region

## Limitations

- Lazy decryption of fields is not supported
- MongoDB field renaming support is planned but not yet implemented
