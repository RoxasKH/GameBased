package com.cip.cipstudio.dataSource.repository

import com.cip.cipstudio.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import android.util.Base64

object FirebaseRepository {
    private var db : FirebaseDatabase? = null
    private lateinit var userId : String

    init{
        db = FirebaseDatabase.getInstance("https://cip-studio-default-rtdb.europe-west1.firebasedatabase.app")
        login()
    }

    fun login() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            userId = FirebaseAuth.getInstance().currentUser!!.uid
        }
    }

    fun addGenreToFYP(genreId: String): Task<Void> {
        val data: Map<String, Any> = hashMapOf("weight" to 1)
        val ref = db!!.getReference("users/$userId/forYou")
        return ref.child(genreId).setValue(data)
    }

    fun updateFYPGenreWeight(genreId: String, weight : Int) : Task<Void>{
        val data: Map<String, Any> = hashMapOf("weight" to weight)
        val ref = db!!.getReference("users/$userId/forYou")
        return ref.child(genreId).updateChildren(data)
    }

    fun getFYP() : Task<DataSnapshot> {
        val ref = db!!.getReference("users/$userId/forYou")
        return ref.get()
    }

    fun addGamesToRecentlyViewed(gameIdToAdd : String, gameIdToDelete : String? = null) : Task<Void> {
        val data : Map<String, Any> = hashMapOf("dateTime" to System.currentTimeMillis())
        val ref = db!!.getReference("users/$userId/recentlyViewed")
        val task = ref.child(gameIdToAdd).setValue(data)
        if(gameIdToDelete != null){
            ref.child(gameIdToDelete).removeValue()
        }
        return task
    }

    fun deleteGamesFromRecentlyViewed() : Task<Void> {
        return db!!.getReference("users/${User.uid}/recentlyViewed").removeValue()
    }

    fun getRecentlyViewedGames() : Task<DataSnapshot>{
        return db!!
            .getReference("users/$userId/recentlyViewed")
            .get()
    }

    fun addQueryToRecentlySearch(queryToAdd : String, queryToDelete : String? = null) : Task<Void> {
        val data : Map<String, Any> = hashMapOf("dateTime" to System.currentTimeMillis())
        val ref = db!!.getReference("users/$userId/recentlySearch")
        val encodedQueryToAdd = Base64.encodeToString(queryToAdd.toByteArray(), Base64.NO_WRAP)
        val task = ref.child(encodedQueryToAdd).setValue(data)
        if(queryToDelete != null) {
            val encodedQueryToDelete = Base64.encodeToString(queryToDelete.toByteArray(), Base64.NO_WRAP)
            ref.child(encodedQueryToDelete).removeValue()
        }
        return task
    }

    fun deleteAllQueriesFromRecentlySearch() : Task<Void> {
        return db!!.getReference("users/${User.uid}/recentlySearch").removeValue()
    }

    fun deleteQueryFromRecentlySearch(query : String) : Task<Void> {
        val encodedQuery = Base64.encodeToString(query.toByteArray(), Base64.NO_WRAP)
        return db!!.getReference("users/${User.uid}/recentlySearch/$encodedQuery").removeValue()
    }

    fun getRecentlySearchesQueries() : Task<DataSnapshot>{
        return db!!
            .getReference("users/$userId/recentlySearch")
            .get()
    }

    fun setGameToFavourite(gameId : String) : Task<Void> {
        return db!!
            .getReference("users/$userId/favourites")
            .child(gameId).setValue(gameId)
    }

    fun removeGameFromFavourite(gameId : String) : Task<Void>{
        return db!!
            .getReference("users/$userId/favourites")
            .child(gameId)
            .removeValue()
    }

    fun getFavourites() : Task<DataSnapshot>{
        return db!!
            .getReference("users/$userId/favourites").get()
    }


    /**
     * se viene ritornato null allora il gioco non è tra i preferiti
     */
    fun isGameFavourite(gameId : String) : Task<DataSnapshot?>{
        return db!!
            .getReference("users/$userId/favourites/$gameId")
            .get()
    }

}