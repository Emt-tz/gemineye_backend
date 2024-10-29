package tz.co.geminey.feature.matches

import kotlinx.serialization.Serializable
import org.ktorm.schema.*

object SwipesTable : Table<Nothing>("swipes") {
    val id = int("id").primaryKey()
    val userId = int("user_id")
    val swipedId = int("swiped_user_id")
    val action = boolean("action")
    val date = varchar("timestamp")
}

object MatchesTable : Table<Nothing>("matches") {
    val id = int("id").primaryKey()
    val user1Id = int("user1_id")
    val user2Id = int("user2_id")
    val percentage = double("percentage")
    val isFirstTime = boolean("is_first_time")
    val isPercentIncreased = boolean("is_percent_increased")
    val user1Like = int("user1_like")
    val user2Like = int("user2_like")
    val user1Favourite = int("user1_favourite")
    val user2Favourite = int("user2_favourite")
}

data class Matches(
    val id: Int?,
    val user1Id: Int?,
    val user2Id: Int?,
    val percentage: Double?,
    val isFistTime: Boolean? = true,
    val isPercentIncreased: Boolean? = false,
    val isFavourite: Int? = 0,
    val swiped: Boolean = false,
    val possibleMatch: Boolean = false,
    val userType: Int = 0,
)

data class MatchesPercent(
    val percentage: Double?,
)

@Serializable
data class SwipeData(val userId: String, val like: Int)


@Serializable
data class MatchedFav(val id: Int?, val userType: Int)

@Serializable
data class UpdateSwipedProfiles(val id: String, val swipeDataList: List<SwipeData>)


@Serializable
data class SwipeMatches(
    val id: Int,
    val type: Int,
    val fav: Boolean? = null
)

@Serializable
data class SavedSwipeMatches(
    val id: Int
)

@Serializable
data class UpdateSwipedMatches(
    val swipeDataList: List<SwipeMatches>
)
