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

    constructor(name: String, iAmNotUsedAnyMore: String) : this(name) {
        this.iAmNotUsedAnyMore = iAmNotUsedAnyMore
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: String? = null

    var iAmNotUsedAnyMore: String = ""


    override fun toString(): String {
        return name
    }

}