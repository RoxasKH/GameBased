package com.cip.cipstudio.view.fragment

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginRight
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cip.cipstudio.R
import com.cip.cipstudio.adapters.GameScreenshotsRecyclerViewAdapter
import com.cip.cipstudio.adapters.GamesRecyclerViewAdapter
import com.cip.cipstudio.databinding.FragmentGameDetailsBinding
import com.cip.cipstudio.model.data.GameDetails
import com.cip.cipstudio.model.data.Loading
import com.cip.cipstudio.model.data.PlatformDetails
import com.cip.cipstudio.repository.IGDBRepository
import com.cip.cipstudio.view.dialog.PlatformDetailsDialog
import com.cip.cipstudio.viewmodel.GameDetailsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameDetailsFragment : Fragment() {
    private lateinit var gameDetailsViewModel: GameDetailsViewModel
    private lateinit var gameDetailsBinding: FragmentGameDetailsBinding
    private val TAG : String = "GameDetailsFragment"
    private var myView : View? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //if(myView == null){
            gameDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_game_details, container, false)

        gameDetailsBinding.fGameDetailsClPageLayout.visibility = View.GONE

        gameDetailsBinding.loadingModel = Loading()

        initializeFragment()

        gameDetailsBinding.lifecycleOwner = this
        return gameDetailsBinding.root
    }



    private fun initializeShowMore() {
        gameDetailsBinding.fGameDetailsTvShowMoreDescription.setOnClickListener {
            val params = gameDetailsBinding.fGameDetailsTvGameDetailsDescription.layoutParams
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            gameDetailsBinding.fGameDetailsTvGameDetailsDescription.layoutParams = params
            gameDetailsBinding.fGameDetailsTvShowMoreDescription.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                gameDetailsBinding.fGameDetailsTvGameDetailsDescription.foreground = null
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeFragment() {
        val gameId = arguments?.get("game_id") as String
        gameDetailsViewModel = GameDetailsViewModel(
            gameId,
            gameDetailsBinding,
            { setScreenshots(it)},
            { setSimilarGames(it)},
            { setPlatforms(it)},
            { setGenres(it)},
        ) {
            // onSuccess
            gameDetailsBinding.vm = gameDetailsViewModel
            gameDetailsBinding.fGameDetailsClPageLayout.visibility = View.VISIBLE
            gameDetailsBinding.loadingModel!!.isPageLoading.postValue(false)
            initializeShowMore()
        }
    }

    private fun setScreenshots(screenshotList: List<JSONObject>) {
        val screenshotsRecyclerView = gameDetailsBinding.fGameDetailsRvScreenshots
        val manager = LinearLayoutManager(context)
        manager.orientation = RecyclerView.HORIZONTAL
        val rvGameScreenshotsAdapter = GameScreenshotsRecyclerViewAdapter(requireContext(), screenshotList )
        screenshotsRecyclerView.layoutManager = manager
        screenshotsRecyclerView.setItemViewCacheSize(50)
        screenshotsRecyclerView.itemAnimator = null
        screenshotsRecyclerView.adapter = rvGameScreenshotsAdapter
    }

    private fun setSimilarGames(similarGamesList: List<GameDetails>) {
        val similarGamesRecyclerView = gameDetailsBinding.fGameDetailsRvSimilarGames
        val manager = LinearLayoutManager(context)
        manager.orientation = RecyclerView.HORIZONTAL
        val isFromFavourite = arguments?.get("isFromFavouriteScreen")
        val isFromSearchScreen = arguments?.get("isFromSearchScreen")
        var rvSimilarGamesAdapter : GamesRecyclerViewAdapter
        if(isFromFavourite != null && isFromFavourite as Boolean)
            rvSimilarGamesAdapter = GamesRecyclerViewAdapter(requireContext(), similarGamesList, R.id.action_gameDetailsFragment3_self)
        else if(isFromSearchScreen != null && isFromSearchScreen as Boolean)
            rvSimilarGamesAdapter = GamesRecyclerViewAdapter(requireContext(), similarGamesList, R.id.action_gameDetailsFragment4_self)
        else
            rvSimilarGamesAdapter = GamesRecyclerViewAdapter(requireContext(), similarGamesList, R.id.action_gameDetailsFragment2_self)
        similarGamesRecyclerView.layoutManager = manager
        similarGamesRecyclerView.setItemViewCacheSize(50)
        similarGamesRecyclerView.itemAnimator = null
        similarGamesRecyclerView.adapter = rvSimilarGamesAdapter
    }

    private fun createChip(label: String):Chip {
        val chip = Chip(requireContext(), null, R.layout.genre_chip)
        val chipDrawable = ChipDrawable.createFromAttributes(
            requireContext(),
            null,
            0,
            com.cip.cipstudio.R.style.genre_chip
        )
        chip.setChipDrawable(chipDrawable)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(15, 0, 15, 0)
        chip.layoutParams = params
        chip.text = label
        return chip
    }

    private fun setGenres(genres: List<JSONObject>) {
        for (genre in genres) {
            gameDetailsBinding.fGameDetailsGlGridGenreLayout.addView(
                createChip(
                    genre.getString("name")
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setPlatforms(platforms: List<PlatformDetails>) {
        Log.i("PLATFORMS", platforms.toString())
        for (platform in platforms){
            gameDetailsBinding.fGameDetailsGlGridPlatformsLayout.addView(
                _setPlatformText(platform)
            )
        }
        //gameDetailsBinding.fGameDetailsTvGameDetailsPlatforms.text = platforms
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun _setPlatformText(platform : PlatformDetails) : TextView{
        val text = TextView(requireContext(), null, R.layout.platform_item)
        text.setTextColor(requireContext().getColor(R.color.primary_color))
        text.text = platform.name
        text.typeface= ResourcesCompat.getFont(requireContext(), R.font.montserrat_regular)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 15, 0)
        text.layoutParams = params
        text.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.platform_bottom_sheet, null)
            val dialog = PlatformDetailsDialog(requireContext(), platform, view)


            dialog.show()
        }
        return text
    }

}