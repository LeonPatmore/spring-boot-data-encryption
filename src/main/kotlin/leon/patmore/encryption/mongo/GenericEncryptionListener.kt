package leon.patmore.encryption.mongo

import leon.patmore.encryption.Encrypted
import leon.patmore.encryption.EncryptionService
import org.bson.Document
import org.bson.types.Binary
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("unused")
@Component
class GenericEncryptionListener(
    private val encryptionService: EncryptionService,
) {

    @EventListener
    fun beforeSave(event: BeforeSaveEvent<Any>) {
        if (event.document != null) {
            encryptDocument(event.document!!, event.source)
        }
    }

    @EventListener
    fun afterLoad(event: AfterLoadEvent<Any>) {
        if (event.document != null) {
            decryptDocument(event.document!!, event.type)
        }
    }

    private fun encryptDocument(document: Document, entity: Any, path: String = "$", encryptedPaths: MutableList<String> = mutableListOf()) {
        for (prop in entity::class.memberProperties) {
            val shouldEncrypt = prop.javaField?.getAnnotation(Encrypted::class.java) != null
            val currentPath = if (path.isEmpty()) prop.name else "$path.${prop.name}"

            when (val value = document[prop.name]) {
                is Document -> {
                    if (shouldEncrypt) throw Error("Encrypting a document directly is not supported, please specify the specific fields")
                    encryptDocument(value, prop.getter.call(entity)!!, currentPath, encryptedPaths)
                }

                is List<*> -> {
                    if (shouldEncrypt) throw Error("Encrypting a list directly is not supported yet")
                    encryptList(value, prop.getter.call(entity)!! as List<*>, currentPath, encryptedPaths)
                }

                else -> {
                    if (shouldEncrypt) {
                        encryptFieldIfRequired(document, entity, prop, path, encryptedPaths)
                    }
                }
            }
        }

        if (path == "$") {
            document["_encryptedFields"] = encryptedPaths
        }
    }

    private fun encryptList(list: List<*>, entity: List<*>, path: String, encryptedPaths: MutableList<String>) {
        list.forEachIndexed { index, it ->
            val currentPath = "$path[$index]"
            when (it) {
                is Document -> encryptDocument(it, entity[index]!!, currentPath, encryptedPaths)
                is List<*> -> encryptList(it, entity[index]!! as List<*>, currentPath, encryptedPaths)
            }
        }
    }

    private fun encryptFieldIfRequired(document: Document, entity: Any, prop: KProperty<*>, path: String, encryptedPaths: MutableList<String>) {
        val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: return
        val targetField = annotation.encryptedFieldName.ifEmpty { prop.name }
        val targetValue = (prop.getter.call(entity) as? String) ?: return
        val ciphertext = encryptionService.encrypt(targetValue.toByteArray())
        document[targetField] = ciphertext

        encryptedPaths.add("$path.$targetField")
    }

    private fun decryptDocument(document: Document, entity: Class<*>) {
        val kClass = entity.kotlin
        for (prop in kClass.memberProperties) {
            if (document[prop.name] is Document) {
                @Suppress("UNCHECKED_CAST")
                decryptDocument(document[prop.name] as Document, (prop.returnType.classifier as? KClass<Any>)?.java as Class<*>)
                continue
            }
            if (document[prop.name] is List<*>) {
                @Suppress("UNCHECKED_CAST")
                val listType = (prop.returnType.arguments.first().type!!.classifier as KClass<Any>).java
                decryptList(document[prop.name] as List<*>, listType)
                continue
            }

            decryptFieldIfRequired(document, prop)
        }

        println("Hey")
    }

    private fun decryptFieldIfRequired(document: Document, prop: KProperty<*>) {
        val annotation = prop.javaField?.getAnnotation(Encrypted::class.java) ?: return
        val targetField = annotation.encryptedFieldName.ifEmpty { prop.name }

        val encryptedValue = (document[targetField] as? Binary)?.data ?: return
        val decrypted = String(encryptionService.decrypt(encryptedValue))

        document[prop.name] = decrypted
    }

    private fun decryptList(list: List<*>, listEntity: Class<*>) {
        list.forEachIndexed { index, it ->
            when (it) {
                is Document -> {
                    decryptDocument(it, listEntity)
                }

                is List<*> -> {
                    decryptList(it, listEntity)
                }

                else -> {
                }
            }
        }
    }
}
