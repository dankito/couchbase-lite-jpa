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

        applyTransformationAllDocumentsOfType(formerEntityFullQualifiedClassName) { _, properties ->
            properties.putAll(updatedClassNameProperty)
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
        applyTransformationAllDocumentsOfType(fullQualifiedClassName) { _, properties ->
            propertyNames.forEach { propertyName ->
                properties.remove(propertyName)
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
        applyTransformationAllDocumentsOfType(fullQualifiedClassName) { _, properties ->
            if (properties.containsKey(formerPropertyName)) {
                properties.put(newPropertyName, properties.get(formerPropertyName))

                properties.remove(formerPropertyName)
            }
        }
    }


    protected open fun getAllDocumentsOfType(fullQualifiedClassName: String): List<Document> {
        return database.createAllDocumentsQuery().run()
                .filter { fullQualifiedClassName == it.document.getProperty(Dao.TYPE_COLUMN_NAME) }
                .map { it.document }
    }

    protected open fun applyTransformationAllDocumentsOfType(fullQualifiedClassName: String,
                                                             transformationCallback: (Document, latestRevisionProperties: MutableMap<String, Any?>) -> Unit) {

        getAllDocumentsOfType(fullQualifiedClassName).forEach { storedDocument ->
            // see http://blog.couchbase.com/2016/july/better-updates-couchbase-lite
            try {
                storedDocument.update { newRevision ->
                    val properties = newRevision.userProperties

                    transformationCallback(storedDocument, properties)

                    newRevision.userProperties = properties
                    true
                }
            } catch (e: Exception) {
                log.error("Could not update Document with Id ${storedDocument.id}", e)
            }
        }
    }

}