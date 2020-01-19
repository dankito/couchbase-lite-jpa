package net.dankito.jpa.migration

import net.dankito.jpa.DatabaseTestUtil
import net.dankito.jpa.migration.model.Person
import net.dankito.jpa.migration.model.previous.PreviousPerson
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class MigratorTest {

    private val testUtil = DatabaseTestUtil()


    private val underTest = Migrator(testUtil.database)


    @After
    fun tearDown() {
        testUtil.cleanUp()
    }


    @Test
    fun migrateClass() {

        // given
        val personDao = testUtil.getDao(Person::class.java)!!
        val previousPersonDao = testUtil.getDao(PreviousPerson::class.java)!!

        previousPersonDao.create(PreviousPerson("Previous Person 1"))
        previousPersonDao.create(PreviousPerson("Previous Person 2"))


        // when
        underTest.migrateClass(PreviousPerson::class, Person::class)


        // then
        val newPersons = personDao.retrieveAllEntitiesOfType(Person::class.java)
        assertThat(newPersons).hasSize(2)

        val previousPersons = previousPersonDao.retrieveAllEntitiesOfType(PreviousPerson::class.java)
        assertThat(previousPersons).isEmpty()
    }


    @Test
    fun removeUnusedProperties() {

        // given
        val countPersonPropertiesBefore = 5

        val previousPersonDao = testUtil.getDao(PreviousPerson::class.java)!!

        previousPersonDao.create(PreviousPerson("Previous Person 1"))
        previousPersonDao.create(PreviousPerson("Previous Person 2"))

        underTest.migrateClass(PreviousPerson::class, Person::class)

        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.containsKey(PreviousPerson::iAmNotUsedAnyMore.name)).isTrue()
            assertThat(document.properties).hasSize(countPersonPropertiesBefore)
        }


        // when
        underTest.removeUnusedProperties(Person::class, listOf(PreviousPerson::iAmNotUsedAnyMore))


        // then
        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.containsKey(PreviousPerson::iAmNotUsedAnyMore.name)).isFalse()
            assertThat(document.properties).hasSize(countPersonPropertiesBefore - 1)
        }
    }


    @Test
    fun renameProperty() {

        // given
        val newPropertyName = "myNewFancyName"
        val countPersonProperties = 5

        val previousPersonDao = testUtil.getDao(PreviousPerson::class.java)!!

        previousPersonDao.create(PreviousPerson("Previous Person 1", "Not used 1"))
        previousPersonDao.create(PreviousPerson("Previous Person 2", "Not used 2"))

        underTest.migrateClass(PreviousPerson::class, Person::class)

        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.containsKey(PreviousPerson::iAmNotUsedAnyMore.name)).isTrue()
            assertThat(document.properties).hasSize(countPersonProperties)
        }


        // when
        underTest.renameProperty(Person::class, PreviousPerson::iAmNotUsedAnyMore.name, newPropertyName)


        // then
        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.get(newPropertyName) as String).isNotEmpty()
            assertThat(document.properties.containsKey(PreviousPerson::iAmNotUsedAnyMore.name)).isFalse()
            assertThat(document.properties).hasSize(countPersonProperties)
        }
    }

    @Test
    fun renameProperty_PropertyDoesNotExist() {

        // given
        val notExistingPropertyName = "i_do_not_exist"
        val newPropertyName = "myNewFancyName"
        val countPersonProperties = 5

        val previousPersonDao = testUtil.getDao(PreviousPerson::class.java)!!

        previousPersonDao.create(PreviousPerson("Previous Person 1"))
        previousPersonDao.create(PreviousPerson("Previous Person 2"))

        underTest.migrateClass(PreviousPerson::class, Person::class)

        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.containsKey(PreviousPerson::iAmNotUsedAnyMore.name)).isTrue()
            assertThat(document.properties).hasSize(countPersonProperties)
        }


        // when
        underTest.renameProperty(Person::class, notExistingPropertyName, newPropertyName)


        // then
        testUtil.getAllDocumentsOfType(Person::class).forEach { document ->
            assertThat(document.properties.containsKey(newPropertyName)).isFalse()
            assertThat(document.properties.containsKey(notExistingPropertyName)).isFalse()
            assertThat(document.properties).hasSize(countPersonProperties)
        }
    }

}