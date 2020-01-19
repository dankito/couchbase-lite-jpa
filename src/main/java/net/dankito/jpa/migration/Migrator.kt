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

    open fun migrateClass(formerEntityFullQualifiedClassName: String, newEntityFullQualifiedClass: Class<*>) {
        migrateClass(formerEntityFullQualifiedClassName, newEntityFullQualifiedClass.name)
    }

    open fun migrateClass(formerEntityFullQualifiedClassName: String, newEntityFullQualifiedClass: String) {
        val updatedClassNameProperty = mapOf(Dao.TYPE_COLUMN_NAME to newEntityFullQualifiedClass)

        getAllDocumentsOfType(formerEntityFullQualifiedClassName).forEach { document ->
            updateDocument(document, updatedClassNameProperty)
        }
    }


    open fun removeUnusedProperties(entityClass: KClass<*>, propertyNames: List<KProperty<*>>) {
        removeUnusedProperties(entityClass.java, propertyNames.map { it.name })
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