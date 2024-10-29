package tz.co.geminey.feature.matches

import io.ktor.http.*
import org.ktorm.dsl.*
import org.ktorm.support.mysql.toLowerCase
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.config.DBHelper.database
import tz.co.geminey.models.Customer
import tz.co.geminey.models.CustomerTable

class MatchesRepository() {

    /** Start of updateSwipedProfilesInMatches */
    fun updateSwipedProfilesInMatches(updateSwipedProfiles: UpdateSwipedProfiles): ApiResponse {
        return try {
            val customerId = updateSwipedProfiles.id.toInt()
            val swipedUserIds = updateSwipedProfiles.swipeDataList.map { it.userId.toInt() }
            val likedUserIds = updateSwipedProfiles.swipeDataList.filter { it.like == 1 }.map { it.userId.toInt() }

            database.useTransaction {
                val matchesToUpdate = findMatchesToUpdate(customerId, swipedUserIds)
                updateMatchesWithLikes(matchesToUpdate, likedUserIds, customerId)
            }

            ApiResponse(200, "Success", emptyMap<String, Any>())
        } catch (e: Exception) {
            println("Error on updateSwipedProfilesInMatches: ${e.message}")
            ApiResponse(500, "Internal Server Error", emptyMap<String, Any>())
        }
    }

    private fun findMatchesToUpdate(id: Int, swipedUserIds: List<Int>): Map<Int?, Int?> {
        return database.from(MatchesTable)
            .select(MatchesTable.id, MatchesTable.user1Id, MatchesTable.user2Id)
            .where(
                ((MatchesTable.user1Id eq id) and (MatchesTable.user2Id inList swipedUserIds))
                        or ((MatchesTable.user2Id eq id) and (MatchesTable.user1Id inList swipedUserIds))
            ).map {
                val matchId = it[MatchesTable.id]
                val matchedUserId =
                    if (it[MatchesTable.user1Id] == id) it[MatchesTable.user2Id]?.toInt() else it[MatchesTable.user1Id]?.toInt()
                matchId to matchedUserId
            }.toMap()
    }

    private fun updateMatchesWithLikes(matchesToUpdate: Map<Int?, Int?>, likedUserIds: List<Int>, userId: Int) {
        matchesToUpdate.forEach { (matchId, matchedUserId) ->
            if (matchId != null) {
                val userLikesMatch = likedUserIds.contains(matchedUserId)
                val isUser1 = getUser1OrNull(matchId)
                updateLikesForMatch(matchId, isUser1, userId, userLikesMatch)
            }
        }
    }

    private fun getUser1OrNull(matchId: Int): Int? {
        return database.from(MatchesTable).select(MatchesTable.user1Id)
            .where { MatchesTable.id eq matchId }
            .map { it[MatchesTable.user1Id] }
            .firstOrNull()
    }

    private fun updateLikesForMatch(matchId: Int, isUser1: Int?, userId: Int, userLikesMatch: Boolean) {
        database.update(MatchesTable) {
            where { MatchesTable.id eq matchId }
            if (isUser1 != null && isUser1 == userId)
                set(MatchesTable.user1Like, if (userLikesMatch) 1 else 0)
            else
                set(MatchesTable.user2Like, if (userLikesMatch) 1 else 0)
        }
    }

    /** END of updateSwipedProfilesInMatches */


    fun getFavouritesProfiles(username: String): Any {
        return try {
            val profiles = database.from(SwipesTable).select().where {
                (SwipesTable.userId eq username.toInt()) and SwipesTable.action eq true
            }.mapNotNull {
                it[SwipesTable.swipedId]
            }
            if (profiles.isNotEmpty()) {
                val customers = database.from(CustomerTable).select()
                    .where {
                        CustomerTable.id inList profiles
                    }.mapNotNull { row ->
                        mapOf(
                            "id" to row[CustomerTable.id],
                            "profilePic" to row[CustomerTable.profileUrl],
                            "location" to row[CustomerTable.location],
                            "dob" to row[CustomerTable.dob],
                            "fullName" to row[CustomerTable.fullName]

                        )
                    }
                println("CHECK RES:: $customers")
                ApiResponse(
                    code = 200,
                    message = "Success",
                    body = mapOf("profiles" to customers)
                )
            } else {
                ApiResponse(
                    code = 200,
                    message = "No favourite profile found!",
                    body = emptyMap<String, Any>()
                )
            }
        } catch (e: Exception) {
            println("Error on getFavouritesProfiles: ${e.message}")
            ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
        }
    }

    private fun getLastMatchingTimestamp(): Long {
//    return System.currentTimeMillis() - (3 * 60 * 1000)
//    return System.currentTimeMillis() - (3 * 60 * 60 * 1000)
        return System.currentTimeMillis() - (24 * 60 * 60 * 1000)
    }

    data class MatchCustomer(
        val id: Int?,
        val gender: String,
        val interest: List<Int>
    )

    private fun getUpdatedCustomersSince(timestamp: Long): List<MatchCustomer> {
        println("THIS IS CALLED: getUpdatedCustomersSince")
        return database.from(CustomerTable)
            .select(CustomerTable.id, CustomerTable.gender, CustomerTable.interests)
            .where { CustomerTable.createdAt greater timestamp }
            .mapNotNull { row ->
                MatchCustomer(
                    id = row[CustomerTable.id],
                    gender = row[CustomerTable.gender] ?: "",
                    interest = row[CustomerTable.interests] ?: emptyList()
                )
            }
    }

    private fun getOppositeGenderCustomers(gender: String): List<MatchCustomer> {
        return database.from(CustomerTable)
            .select(CustomerTable.id, CustomerTable.gender, CustomerTable.interests)
            .where { CustomerTable.gender.toLowerCase() eq gender.lowercase() }
            .mapNotNull { row ->
                MatchCustomer(
                    id = row[CustomerTable.id],
                    gender = row[CustomerTable.gender] ?: "",
                    interest = row[CustomerTable.interests] ?: emptyList()
                )
            }
    }

    private fun shouldCompare(customer1: MatchCustomer, customer2: MatchCustomer): Boolean {
        // Add additional criteria for selective matching if needed
        return true
    }

    private fun getCustomerBaseOnGender(customer: MatchCustomer) {
        val gender = customer.gender
        if (gender.lowercase() == "male") {
            val oppositeGenderCustomers = getOppositeGenderCustomers("female")
            matchCustomerWithOppositeGender(customer, oppositeGenderCustomers)
        } else if (gender.lowercase() == "female") {
            val oppositeGenderCustomers = getOppositeGenderCustomers("male")
            matchCustomerWithOppositeGender(customer, oppositeGenderCustomers)
        }
    }


    private fun matchCustomerWithOppositeGender(customer: MatchCustomer, oppositeGenderCustomers: List<MatchCustomer>) {
        val matchesToInsert = mutableListOf<Matches>()
        val matchesToUpdate = mutableListOf<Matches>()

        for (oppositeGenderCustomer in oppositeGenderCustomers) {
            processMatch(customer, oppositeGenderCustomer, matchesToInsert, matchesToUpdate)
        }

        insertMatches(matchesToInsert)
        updateMatches(matchesToUpdate)
    }

    private fun processMatch(
        customer: MatchCustomer,
        oppositeGenderCustomer: MatchCustomer,
        matchesToInsert: MutableList<Matches>,
        matchesToUpdate: MutableList<Matches>
    ) {
        val customerId = customer.id
        val interests = customer.interest

        if (shouldCompare(customer, oppositeGenderCustomer)) {
            val oppositeCustomerId = oppositeGenderCustomer.id
            val oppositeInterests = oppositeGenderCustomer.interest

            val commonInterests = interests.intersect(oppositeInterests.toSet())
            val matchPercentage = (commonInterests.size.toDouble() /
                    (interests.union(oppositeInterests).size.toDouble())) * 100

            if (customerId != null && oppositeCustomerId != null) {
                val existingMatch = getExistingMatch(customerId, oppositeCustomerId)

                if (existingMatch == null) {
                    matchesToInsert += Matches(
                        null,
                        customerId,
                        oppositeCustomerId,
                        matchPercentage,
                        true
                    )
                } else {
                    matchesToUpdate += Matches(
                        null,
                        customerId,
                        oppositeCustomerId,
                        matchPercentage,
                        isPercentIncreased = matchPercentage > (existingMatch.percentage ?: 0.0)
                    )
                }
            }
        }
    }


    private fun getExistingMatch(customerId: Int, oppositeCustomerId: Int): MatchesPercent? {
        return database.from(MatchesTable).select(MatchesTable.percentage)
            .where {
                ((MatchesTable.user1Id eq customerId) and (MatchesTable.user2Id eq oppositeCustomerId)) or
                        ((MatchesTable.user1Id eq oppositeCustomerId) and (MatchesTable.user2Id eq customerId))
            }.map {
                MatchesPercent(
                    it[MatchesTable.percentage],
                )
            }.firstOrNull()
    }

    private fun insertMatches(matchesToInsert: List<Matches>) {
        if (matchesToInsert.isNotEmpty()) {
            database.batchInsert(MatchesTable) {
                matchesToInsert.forEach { match ->
                    item {
                        set(MatchesTable.user1Id, match.user1Id)
                        set(MatchesTable.user2Id, match.user2Id)
                        set(MatchesTable.percentage, match.percentage)
                        set(MatchesTable.isFirstTime, true)
                    }
                }
            }
        }
    }

    private fun updateMatches(matchesToUpdate: List<Matches>) {
        if (matchesToUpdate.isNotEmpty()) {
            matchesToUpdate.forEach { match ->
                if (match.user1Id != null && match.user2Id != null) {
                    database.update(MatchesTable) {
                        set(MatchesTable.percentage, match.percentage)
                        set(MatchesTable.isPercentIncreased, (match.isPercentIncreased == true))
                        where {
                            ((MatchesTable.user1Id eq match.user1Id) and (MatchesTable.user2Id eq match.user2Id)) or
                                    ((MatchesTable.user1Id eq match.user2Id) and (MatchesTable.user2Id eq match.user1Id))
                        }
                    }
                }
            }
        }
    }

    fun matchAndStoreMatches() {
        val lastMatchingTimestamp = getLastMatchingTimestamp()
        val updatedCustomers = getUpdatedCustomersSince(lastMatchingTimestamp)
        updatedCustomers.forEach { customer ->
            getCustomerBaseOnGender(customer)
        }
    }


    fun fetchMatchesById(userRequest: Int): ApiResponse {
        return try {
            val matches = queryNewMatches(userRequest)
            val list: List<Any> = matches.filter { !it.swiped }
                .flatMap { match ->
                    database.from(CustomerTable)
                        .select()
                        .where {
                            CustomerTable.id eq (match.user2Id ?: 0)
                        }
                        .mapNotNull {
                            mapOf(
                                "matchedPercent" to match.percentage,
                                "possibleMatch" to match.possibleMatch,
                                "swiped" to match.swiped,
                                "id" to it[CustomerTable.id],
                                "matchedId" to match.id,
                                "userType" to match.userType,
                                "profilePic" to it[CustomerTable.profileUrl],
                                "location" to it[CustomerTable.location],
                                "dob" to it[CustomerTable.dob],
                                "fullName" to it[CustomerTable.fullName],
                                "isFav" to match.isFavourite
                            )
                        }
                }
            ApiResponse(
                code = 200,
                message = "Success",
                body = mapOf("profiles" to list)
            )
        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
        }
    }

    private fun queryNewMatches(id: Int): List<Matches> {
        return database.from(CustomerTable)
            .innerJoin(
                MatchesTable,
                on = ((MatchesTable.user1Id eq CustomerTable.id) or (MatchesTable.user2Id eq CustomerTable.id))
                        and (MatchesTable.percentage greater 10.0)
            )
            .select()
            .where(CustomerTable.userId eq id)
            .mapNotNull {
                val user2: Int
                val swiped: Boolean
                val possibleMatch: Boolean
                val userType: Int
                val isFav: Int
                if (it[CustomerTable.id] == it[MatchesTable.user1Id]) {
                    swiped = it[MatchesTable.user1Like] == 1
                    possibleMatch = it[MatchesTable.user2Like] == 1
                    user2 = it[MatchesTable.user2Id] ?: 0
                    userType = 1
                    isFav = it[MatchesTable.user1Favourite] ?: 0
                } else {
                    swiped = it[MatchesTable.user2Like] == 1
                    possibleMatch = it[MatchesTable.user1Like] == 1
                    user2 = it[MatchesTable.user1Id] ?: 0
                    userType = 2
                    isFav = it[MatchesTable.user2Favourite] ?: 0
                }
                Matches(
                    id = it[MatchesTable.id],
                    user1Id = id,
                    user2Id = user2,
                    percentage = it[MatchesTable.percentage],
                    isFistTime = it[MatchesTable.isFirstTime],
                    isPercentIncreased = it[MatchesTable.isPercentIncreased],
                    swiped = swiped,
                    possibleMatch = possibleMatch,
                    userType = userType,
                    isFavourite = isFav
                )
            }
    }

    fun fetchLikedMatchesById(userRequest: Int): ApiResponse {
        val resp = ApiResponse(
            code = HttpStatusCode.BadRequest.value,
            message = HttpStatusCode.BadRequest.description,
            body = emptyMap<String, Any>()
        )
        return try {
            val matches = queryMatches(userRequest)
            val list: List<Any> = matches.flatMap { match ->
                val profiles = database.from(CustomerTable)
                    .select()
                    .where {
                        CustomerTable.id eq (match.user2Id ?: 0)
                    }
                    .mapNotNull {
                        mapOf(
                            "matchedId" to match.id,
                            "matchedPercent" to match.percentage,
                            "isFav" to match.isFavourite,
                            "userType" to match.userType,
                            "customer" to rowToCustomer(it)
                        )
                    }
                profiles
            }
            ApiResponse(
                code = 200,
                message = "Success",
                body = mapOf("profiles" to list)
            )

        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
        }
    }

    private fun queryMatches(id: Int): List<Matches> {
        return database.from(CustomerTable)
            .innerJoin(
                MatchesTable,
                on = (MatchesTable.user1Like eq 1 and (MatchesTable.user2Like eq 1))
                        and ((MatchesTable.user1Id eq CustomerTable.id)
                        or (MatchesTable.user2Id eq CustomerTable.id))
            )
            .select()
            .where { CustomerTable.userId eq id }
            .map {
                val user2: Int?
                val isFav: Int?
                val userType: Int?

                if (it[CustomerTable.id] == it[MatchesTable.user1Id]) {
                    user2 = it[MatchesTable.user2Id]
                    isFav = it[MatchesTable.user1Favourite]
                    userType = 1
                } else {
                    user2 = it[MatchesTable.user1Id]
                    isFav = it[MatchesTable.user2Favourite]
                    userType = 2
                }

                Matches(
                    id = it[MatchesTable.id],
                    user1Id = it[CustomerTable.id],
                    user2Id = user2,
                    percentage = it[MatchesTable.percentage],
                    isFistTime = it[MatchesTable.isFirstTime],
                    isPercentIncreased = it[MatchesTable.isPercentIncreased],
                    isFavourite = isFav,
                    userType = userType
                )
            }
    }

    private fun rowToCustomer(row: QueryRowSet): Customer {
        return Customer(
            id = row[CustomerTable.id] ?: 0,
            userId = row[CustomerTable.userId] ?: 0,
            fullName = row[CustomerTable.fullName] ?: "",
            phone = row[CustomerTable.phone] ?: "",
            dateOfBirth = row[CustomerTable.dob] ?: "",
            gender = row[CustomerTable.gender] ?: "",
            interests = row[CustomerTable.interests] ?: emptyList(),
            joinReasons = row[CustomerTable.joinReasons] ?: "",
            bio = row[CustomerTable.bio] ?: "",
            connectId = row[CustomerTable.connectId] ?: "",
            profilePic = row[CustomerTable.profileUrl] ?: "",
            countryCode = ""
        )
    }

    //TODO REMOVE THIS SINCE IS FOR TESTING PURPOSE ONLY
    fun resetMatches() {
        try {
            database.useTransaction {
                database.update(MatchesTable) {
                    set(MatchesTable.user1Like, 0)
                    set(MatchesTable.user2Like, 0)
                    set(MatchesTable.isFirstTime, false)
                    set(MatchesTable.user1Favourite, 0)
                    set(MatchesTable.user2Favourite, 0)
                }
            }
            ApiResponse(
                code = 200,
                message = "Success",
                body = emptyMap<String, Any>()
            )
        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = e.message.toString(),
                body = emptyMap<String, Any>()
            )
        }
    }

    //TODO REMOVE THIS SINCE IS FOR TESTING PURPOSE ONLY
    fun setMatches() {
        try {
            database.useTransaction {
                database.update(MatchesTable) {
                    set(MatchesTable.user1Like, 1)
                    set(MatchesTable.user2Like, 1)
                    set(MatchesTable.isFirstTime, true)
                    set(MatchesTable.user1Favourite, 1)
                    set(MatchesTable.user2Favourite, 1)
                }
            }
            ApiResponse(
                code = 200,
                message = "Success",
                body = emptyMap<String, Any>()
            )
        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = e.message.toString(),
                body = emptyMap<String, Any>()
            )
        }
    }

    fun updateFavourite(updateFav: UpdateSwipedMatches): ApiResponse {
        var resp = ApiResponse(
            code = HttpStatusCode.BadRequest.value,
            message = HttpStatusCode.BadRequest.description,
            body = emptyMap<String, Any>()
        )
        try {
            var saveIds = listOf<SavedSwipeMatches>()
            for (markFav in updateFav.swipeDataList) {
                val rowSet = database.update(MatchesTable) {
                    where { MatchesTable.id eq markFav.id }
                    if (markFav.type == 1)
                        set(MatchesTable.user2Favourite, if (markFav.fav == true) 1 else 0)
                    else
                        set(MatchesTable.user1Favourite, if (markFav.fav == true) 1 else 0)
                }
                if (rowSet > 0) {
                    saveIds = saveIds + SavedSwipeMatches(markFav.id)
                }
            }

            resp = ApiResponse(
                code = HttpStatusCode.OK.value,
                message = HttpStatusCode.OK.description,
                body = mapOf("savedIds" to saveIds)
            )

        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
        }
        return resp
    }

    fun updateSwipedMatches(swipedProfiles: UpdateSwipedMatches): ApiResponse {
        var resp = ApiResponse(
            code = HttpStatusCode.BadRequest.value,
            message = HttpStatusCode.BadRequest.description,
            body = emptyMap<String, Any>()
        )
        var saveIds = listOf<SavedSwipeMatches>()
        try {
            for (swiped in swipedProfiles.swipeDataList) {
                val rowCount = database.update(MatchesTable) {
                    where { MatchesTable.id eq swiped.id }
                    if (swiped.type == 1) {
                        set(MatchesTable.user1Like, 1)
                    } else {
                        set(MatchesTable.user2Like, 1)
                    }
                }
                if (rowCount > 0) {
                    saveIds = saveIds + SavedSwipeMatches(swiped.id)
                }
            }
            resp = ApiResponse(
                code = HttpStatusCode.OK.value,
                message = HttpStatusCode.OK.description,
                body = mapOf("savedIds" to saveIds)
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return resp
    }

}