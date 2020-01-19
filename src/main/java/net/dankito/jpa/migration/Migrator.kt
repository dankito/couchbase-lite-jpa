package net.dankito.jpa.migration

import com.couchbase.lite.Database
import com.couchbase.lite.Document
import net.dankito.jpa.couchbaselite.Dao
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


open class Migrator(
        protected val database: Database
) {

    companion object {
        private val log = LoggerFactory.getLogger(Migrator::class.java)
    }


    open fun migrateClass(formerEntityClass: KClass<*>, newEntityClass: KClass<*>) {
        migrateClass(formerEntityClass.java, newEntityClass.java)
    }

    open fun migrateClass(formerEntityClass: Class<*>, newEntityClass: Class<*>) {
        migrateClass(formerEntityClass.name, newEntityClass)
    }

    open fun migrateClass(formerEntityFullQualifiedClassName: String, newEntityClass: Class<*>) {
        migrateClass(formerEntityFullQualifiedClassName, newEntityClass.name)
    }

    open fun migrateClass(formerEntityFullQualifiedClassName: String, newEntityFullQualifiedClass: String) {
        val updatedClassNameProperty = mapOf(Dao.TYPE_COLUMN_NAME to newEntityFullQualifiedClass)

        getAllDocumentsOfType(formerEntityFullQualifiedClassName).forEach { document ->
            updateDocument(document, updatedClassNameProperty)
        }
    }


    open fun removeUnusedProperties(entityClass: KClass<*>, properties: List<KProperty<*>>) {
        removeUnusedProperties(entityClass, properties.map { it.name })
    }

    @JvmName(name = "removeUnusedPropertiesString") // cannot make method open due to @JvmName
    fun removeUnusedProperties(entityClass: KClass<*>, propertyNames: List<String>) {
        removeUnusedProperties(entityClass.java, propertyNames)
    }

    open fun removeUnusedProperties(entityClass: Class<*>, propertyNames: List<String>) {
        removeUnusedProperties(entityClass.name, propertyNames)
    }

    open fun removeUnusedProperties(fullQualifiedClassName: String, propertyNames: List<String>) {
        getAllDocumentsOfType(fullQualifiedClassName).forEach { document ->
            // see http://blog.couchbase.com/2016/july/better-updates-couchbase-lite
            try {
                document.update { newRevision ->
                    val properties = newRevision.userProperties

                    propertyNames.forEach { propertyName ->
                        properties.remove(propertyName)
                    }

                    newRevision.userProperties = properties
                    true
                }
            } catch (e: Exception) {
                log.error("Could not remove properties $propertyNames from Document with Id ${document.id}", e)
            }
        }
    }


    open fun renameProperty(entityClass: KClass<*>, formerProperty: KProperty<*>, newProperty: KProperty<*>) {
        renameProperty(entityClass, formerProperty.name, newProperty.name)
    }

    open fun renameProperty(entityClass: KClass<*>, formerPropertyName: String, newPropertyName: String) {
        renameProperty(entityClass.java, formerPropertyName, newPropertyName)
    }

    open fun renameProperty(entityClass: Class<*>, formerPropertyName: String, newPropertyName: String) {
        renameProperty(entityClass.name, formerPropertyName, newPropertyName)
    }

    open fun renameProperty(fullQualifiedClassName: String, formerPropertyName: String, newPropertyName: String) {
        getAllDocumentsOfType(fullQualifiedClassName).forEach { document ->
            // see http://blog.couchbase.com/2016/july/better-updates-couchbase-lite
            try {
                document.update { newRevision ->
                    val properties = newRevision.userProperties

                    if (properties.containsKey(formerPropertyName)) {
                        properties.set(newPropertyName, properties.get(formerPropertyName))

                        properties.remove(formerPropertyName)

                        newRevision.userProperties = properties
                        return@update true
                    }

                    false
                }
            } catch (e: Exception) {
                log.error("Could not rename property $formerPropertyName to $newPropertyName for Document with Id ${document.id}", e)
            }
        }
    }


    protected open fun getAllDocumentsOfType(fullQualifiedClassName: String): List<Document> {
        return database.createAllDocumentsQuery().run()
                .filter { fullQualifiedClassName == it.document.getProperty(Dao.TYPE_COLUMN_NAME) }
                .map { it.document }
    }

    protected open fun updateDocument(storedDocument: Document, updatedProperties: Map<String, Any>) {
        // see http://blog.couchbase.com/2016/july/better-updates-couchbase-lite
        try {
            storedDocument.update { newRevision ->
                val properties = newRevision.userProperties
                properties.putAll(updatedProperties)
                newRevision.userProperties = properties
                true
            }
        } catch (e: Exception) {
            log.error("Could not update Document with Id ${storedDocument.id} to Properties: ${updatedProperties}", e)
        }
    }

}