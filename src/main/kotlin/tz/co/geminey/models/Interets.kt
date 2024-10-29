package tz.co.geminey.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table


object Interets: Table(){
    val id = integer("id").autoIncrement()
    val name = text("name")
    val icon = varchar("icon_id", 128)
//    val status = enumerationByName<InterestStatus>("status", 15)
    val createdDate = long("created_date")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Interest(
    val id : Int,
    val name: String,
    val icon: String,
//    val status: InterestStatus,
    val createdDate: Long
)

//enum class InterestStatus(status: String){
//    ToDo("to-do"),
//    InProgress("in-progress"),
//    Done("done")
//}