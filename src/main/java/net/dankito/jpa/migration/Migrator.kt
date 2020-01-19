package net.dankito.jpa.migration

import com.couchbase.lite.Database
import com.couchbase.lite.Document
import net.dankito.jpa.couchbaselite.Dao
import org.slf4j.LoggerFactory


open class Migrator(
        protected val database: Database
) {

    companion object {
        private val log = LoggerFactory.getLogger(Migrator::class.java)
    }


    open fun migrateClass(formerFullQualifiedClass: Class<*>, newFullQualifiedClass: Class<*>) {
        migrateClass(formerFullQualifiedClass.name, newFullQualifiedClass)
    }

    open fun migrateClass(formerFullQualifiedClassName: String, newFullQualifiedClass: Class<*>) {
        migrateClass(formerFullQualifiedClassName, newFullQualifiedClass.name)
    }

    open fun migrateClass(formerFullQualifiedClassName: String, newFullQualifiedClassName: String) {
        val updatedClassNameProperty = mapOf(Dao.TYPE_COLUMN_NAME to newFullQualifiedClassName)

        database.createAllDocumentsQuery().run().forEach { queryRow ->
            val document = queryRow.document

            if (formerFullQualifiedClassName == document.getProperty(Dao.TYPE_COLUMN_NAME)) {
                updateDocument(document, updatedClassNameProperty)
            }
        }
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
            log.error("Could not update Document with Id " + storedDocument.id + " to Properties: " + updatedProperties)
        }
    }

}