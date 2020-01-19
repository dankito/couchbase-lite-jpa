package net.dankito.jpa.migration.model.previous

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


@Entity
class PreviousPerson(
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