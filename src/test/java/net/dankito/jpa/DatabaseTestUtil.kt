package net.dankito.jpa

import com.couchbase.lite.*
import net.dankito.jpa.apt.generated.GeneratedEntityConfigsUtil
import net.dankito.jpa.cache.DaoCache
import net.dankito.jpa.cache.ObjectCache
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.jpa.util.ValueConverter
import net.dankito.utils.io.FileUtils
import kotlin.reflect.KClass


open class DatabaseTestUtil {

    val database: Database

    protected lateinit var manager: Manager
    protected val objectCache = ObjectCache()
    protected val daoCache = DaoCache()

    protected val fileUtils = FileUtils()


    init {
        val readEntities = GeneratedEntityConfigsUtil().getGeneratedEntityConfigs() ?: listOf()

        database = setUpDatabase()

        val valueConverter = ValueConverter()

        for (entity in readEntities) {
            val dao = Dao(database, entity, objectCache, daoCache, null, valueConverter)
            daoCache.addDao(entity.getEntityClass(), dao)
        }
    }


    open fun cleanUp() {
        deleteDatabase()
    }

    open fun getDao(entityClass: Class<*>): Dao? {
        return daoCache.getDaoForEntity(entityClass)
    }


    open fun getAllDocumentsOfType(entityClass: KClass<*>): List<Document> {
        return getAllDocumentsOfType(entityClass.java)
    }

    open fun getAllDocumentsOfType(entityClass: Class<*>): List<Document> {
        return getAllDocumentsOfType(entityClass.name)
    }

    open fun getAllDocumentsOfType(fullQualifiedClassName: String): List<Document> {
        return database.createAllDocumentsQuery().run()
                .filter { fullQualifiedClassName == it.document.getProperty(Dao.TYPE_COLUMN_NAME) }
                .map { it.document }
    }


    @Throws(Exception::class)
    protected open fun setUpDatabase(): Database {
        manager = Manager(JavaContext(), Manager.DEFAULT_OPTIONS)

        deleteDatabase()

        val options = DatabaseOptions()
        options.isCreate = true

        return manager.openDatabase("test_db", options)
    }

    protected open fun deleteDatabase() {
        fileUtils.deleteFolderRecursively(manager.directory)
    }

}