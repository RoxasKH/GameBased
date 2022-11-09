package com.cip.cipstudio.model.data

import com.cip.cipstudio.repository.MyFirebaseRepository
import com.cip.cipstudio.repository.IGDBRepository
import com.google.android.gms.tasks.Task
import java.io.Serializable

data class Game(
    var name : String,
    val description : String,
    val releaseDate : String,
    val userRatingValue : Int,
    val userRatingCount : Int,
    val criticsRatingValue : Int,
    val criticsRatingCount : Int,
    val platformsId : ArrayList<Int>,
    val genreIds : ArrayList<Int>,
    val similarGamesIds : ArrayList<Int>,
    val screenShotIds : ArrayList<Int>,
    var isGameFavourite : Boolean,
    val gameId : Int,
                ) : Serializable{

    var coverUrl : String? = null

    fun getCover(onSuccess:(String)->Unit){
        if(coverUrl==null){
            val gameRepo = IGDBRepository(generate = false)
            gameRepo.getGameCover(gameId){
                coverUrl = it
                coverUrl = coverUrl!!.replace("t_thumb", "t_cover_big")
                onSuccess.invoke(coverUrl!!)
            }
        }
    }

    fun setGameToFavourite() : Task<Void> {
        return MyFirebaseRepository.getInstance().setGameToFavourite(gameId.toString()).addOnSuccessListener {
            isGameFavourite = true
        }
    }

    fun removeGameFromFavourite() : Task<Void> {
        return MyFirebaseRepository.getInstance().removeGameFromFavourite(gameId.toString()).addOnSuccessListener {
            isGameFavourite = false
        }
    }

}