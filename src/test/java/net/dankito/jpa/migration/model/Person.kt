package net.dankito.jpa.migration.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity
class Person(
    var name: String
) {


    internal constructor() : this("") // for object deserializers


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: String? = null


    override fun toString(): String {
        return name
    }

}