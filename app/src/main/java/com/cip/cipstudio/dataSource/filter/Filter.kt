package com.cip.cipstudio.dataSource.filter

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.view.View
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import com.cip.cipstudio.R
import com.cip.cipstudio.dataSource.filter.criteria.*
import com.cip.cipstudio.databinding.ReusableFilterLayoutBinding
import com.cip.cipstudio.model.data.PlatformDetails
import com.cip.cipstudio.utils.GameTypeEnum
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject

class Filter(private val binding : ReusableFilterLayoutBinding,
             coroutineScope: CoroutineScope,
             isPageLoading: MutableLiveData<Boolean>,
             private val layoutInflater: android.view.LayoutInflater,
             private val resources: android.content.res.Resources,
             private val drawerLayout: DrawerLayout) {


    private var gameType: GameTypeEnum? = null
    private var filterContainer = FilterContainer()
    private val viewModel: ViewModelFilter = FilterRemote(coroutineScope, isPageLoading)

    private var yearMin: Int = 1950
    private var yearMax: Int = 2030
    var offsetPlatforms = 0
    private val tagContainer = "FilterContainer"
    private val tagOffsetPlatforms = "OffsetPlatforms"

    constructor(binding : ReusableFilterLayoutBinding,
                coroutineScope: CoroutineScope,
                isPageLoading: MutableLiveData<Boolean>,
                layoutInflater: android.view.LayoutInflater,
                resources: android.content.res.Resources,
                gameListType: GameTypeEnum,
                drawerLayout: DrawerLayout) : this(binding, coroutineScope, isPageLoading, layoutInflater, resources, drawerLayout) {
        this.gameType = gameListType
    }

    fun initializeFilters(map: Map<String, Any>? = null) {
        var offset = 0
        if (map != null) {
            if (map.containsKey(tagContainer))
                filterContainer = map[tagContainer] as FilterContainer
            if (map.containsKey(tagOffsetPlatforms))
                offset = map[tagOffsetPlatforms] as Int
        }
        initializeCategory()
        initializePlatforms(offset)
        initializeGenres()
        initializeThemes()
        initializeGameModes()
        initializePlayerPerspectives()
        if (gameType != null){
            when (gameType) {
                GameTypeEnum.BEST_RATED, GameTypeEnum.WORST_RATED, GameTypeEnum.LOVED_BY_CRITICS, GameTypeEnum.MOST_RATED, GameTypeEnum.MOST_POPULAR -> {
                    binding.fFilterTvFilterByRating.visibility = View.GONE
                    binding.fFilterLlRating.visibility = View.GONE
                    binding.fFilterDividerReleaseDateRating.visibility = View.GONE
                    initializeReleaseDate()
                }
                GameTypeEnum.UPCOMING, GameTypeEnum.RECENTLY_RELEASED, GameTypeEnum.MOST_HYPED -> {
                    binding.fFilterTvFilterByReleaseDate.visibility = View.GONE
                    binding.fFilterLlReleaseDate.visibility = View.GONE
                    binding.fFilterDividerThemeReleaseDate.visibility = View.GONE
                    initializeRating()
                }
                GameTypeEnum.FOR_YOU ->{
                    initializeRating()
                    initializeReleaseDate()
                }
                else -> {}
            }
            binding.fFilterTvFilterByStatus.visibility = View.GONE
            binding.fFilterDividerStatusPlatform.visibility = View.GONE
            binding.fFilterTvSortBy.visibility = View.GONE
            binding.fFilterDividerSortCategory.visibility = View.GONE
            binding.fFilterActvChangeSort.visibility = View.GONE
            binding.fFilterTilChangeSort.visibility = View.GONE
        }
        else {
            initializeRating()
            initializeStatus()
            initializeReleaseDate()
            initializeSorting()
        }
        initializeDrawerDispatcher()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeDrawerDispatcher() {
        val sliderFilterReleaseDate = Rect()
        val sliderFilterUserRating = Rect()
        val sliderFilterCriticsRating = Rect()
        val scrollView = Rect()

        drawerLayout.setOnTouchListener { _, event ->
            binding.fFilterSldFilterByReleaseDate.getGlobalVisibleRect(sliderFilterReleaseDate)
            increaseRectClickableArea(sliderFilterReleaseDate)
            binding.fFilterSldFilterByUserRating.getGlobalVisibleRect(sliderFilterUserRating)
            increaseRectClickableArea(sliderFilterUserRating)
            binding.fFilterSldFilterByCriticsRating.getGlobalVisibleRect(sliderFilterCriticsRating)
            increaseRectClickableArea(sliderFilterCriticsRating)

            if (sliderFilterReleaseDate.contains(event.x.toInt(), event.y.toInt()) ||
                sliderFilterUserRating.contains(event.x.toInt(), event.y.toInt()) ||
                sliderFilterCriticsRating.contains(event.x.toInt(), event.y.toInt()) ) { // if the touch event is within the slider
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
                drawerLayout.requestDisallowInterceptTouchEvent(true)
                binding.fFilterFilterScroll.dispatchTouchEvent(event) // dispatch the touch event scrollview
            }
            else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                drawerLayout.requestDisallowInterceptTouchEvent(false)
            }
            false

        }
        binding.fFilterFilterScroll.setOnTouchListener { _, event ->
            binding.fFilterSldFilterByReleaseDate.getGlobalVisibleRect(sliderFilterReleaseDate)
            increaseRectClickableArea(sliderFilterReleaseDate)
            binding.fFilterSldFilterByUserRating.getGlobalVisibleRect(sliderFilterUserRating)
            increaseRectClickableArea(sliderFilterUserRating)
            binding.fFilterSldFilterByCriticsRating.getGlobalVisibleRect(sliderFilterCriticsRating)
            increaseRectClickableArea(sliderFilterCriticsRating)
            binding.fFilterFilterScroll.getGlobalVisibleRect(scrollView)

            if (sliderFilterReleaseDate.contains(event.x.toInt(), event.y.toInt())) {
                event.setLocation(event.x - scrollView.left, event.y)
                binding.fFilterSldFilterByReleaseDate.dispatchTouchEvent(event)

            } else if (sliderFilterUserRating.contains(event.x.toInt(), event.y.toInt())) {
                event.setLocation(event.x - scrollView.left, event.y)
                binding.fFilterSldFilterByUserRating.dispatchTouchEvent(event)

            } else if (sliderFilterCriticsRating.contains(event.x.toInt(), event.y.toInt())) {
                event.setLocation(event.x - scrollView.left, event.y)
                binding.fFilterSldFilterByCriticsRating.dispatchTouchEvent(event)

            }
                false

        }
    }

    private fun increaseRectClickableArea(rect: Rect, increase: Int = 4) {
        rect.top -= increase
        rect.bottom += increase
    }

    private fun createChip(id : String, name : String, chipGroup: ChipGroup) : Chip {
        val chipButton = layoutInflater.inflate(R.layout.genre_chip_filter, chipGroup, false) as Chip
        chipButton.id = id.toInt()
        chipButton.text = name
        chipButton.isClickable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chipButton.typeface = resources.getFont(R.font.montserrat_regular)
        }
        else {
            chipButton.typeface = Typeface.createFromAsset(resources.assets, "fonts/montserrat_regular.ttf")
        }
        return chipButton
    }

    private fun initializeTextViewSetOnClick(textView: TextView, child:View, drawerLayout: DrawerLayout? = null, vararg otherChildren: View) {
        textView.setOnClickListener {
            if (child.visibility == View.VISIBLE) {
                child.visibility = View.GONE
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
                for (c in otherChildren) {
                    c.visibility = View.GONE
                }
            }
            else {
                child.visibility = View.VISIBLE
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
                for (c in otherChildren) {
                    c.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initializeChipGroup(chipGroup: ChipGroup, list: List<Int>? = null, getFilter: ViewModelFilter.((List<*>) -> Unit) -> Unit) {
        viewModel.getFilter {
            chipGroup.removeAllViews()
            it.forEach { chipObject ->
                val id: String
                val name: String
                when (chipObject) {
                    is PlatformDetails -> {
                        id = chipObject.id
                        name = chipObject.name
                    }
                    is JSONObject -> {
                        id = chipObject.getString("id")
                        name = chipObject.getString("name")
                    }
                    else -> {
                        id = ""
                        name = ""
                    }
                }
                val chipButton = createChip(id, name, chipGroup)
                chipGroup.addView(chipButton)
            }
            if (list != null && list.isNotEmpty()) {
                list?.forEach {id ->
                    chipGroup.check(id)
                }
            }
        }
    }

    fun getFilterCriteria() : Criteria {
        return filterContainer.getFilterCriteria()
    }

    fun getSortCriteria() : SortCriteria {
        return filterContainer.getSortCriteria()
    }

    fun buildFilterContainer() {
        if (binding.fFilterActvChangeSort.visibility == View.VISIBLE) {
            filterContainer.sorting = binding.fFilterActvChangeSort.text ?: "Default"
        }
        if (binding.fFilterTvFilterByCategory.visibility == View.VISIBLE) {
            filterContainer.categoryList = binding.fFilterCgFilterByCategory.checkedChipIds
        }
        if (binding.fFilterTvFilterByStatus.visibility == View.VISIBLE) {
            filterContainer.statusList = binding.fFilterCgFilterByStatus.checkedChipIds
        }
        if (binding.fFilterTvFilterByPlatform.visibility == View.VISIBLE) {
            filterContainer.platformList = binding.fFilterCgFilterByPlatform.checkedChipIds
        }
        if (binding.fFilterTvFilterByGenre.visibility == View.VISIBLE) {
            filterContainer.genreList = binding.fFilterCgFilterByGenres.checkedChipIds
        }
        if (binding.fFilterTvFilterByPlayerPerspective.visibility == View.VISIBLE) {
            filterContainer.playerPerspectiveList = binding.fFilterCgFilterByPlayerPerspectives.checkedChipIds
        }
        if (binding.fFilterTvFilterByGameMode.visibility == View.VISIBLE) {
            filterContainer.gameModesList = binding.fFilterCgFilterByGameModes.checkedChipIds
        }
        if (binding.fFilterTvFilterByTheme.visibility == View.VISIBLE) {
            filterContainer.themeList = binding.fFilterCgFilterByTheme.checkedChipIds
        }

        if (binding.fFilterTvFilterByReleaseDate.visibility == View.VISIBLE) {
            val min = binding.fFilterSldFilterByReleaseDate.values[0]
            val max = binding.fFilterSldFilterByReleaseDate.values[1]
            filterContainer.releaseDateMin = if (min.toInt() != yearMin)
                     min
                else
                    null

            filterContainer.releaseDateMax = if (max.toInt() != yearMax)
                     max
                else
                    null
        }
        if (binding.fFilterTvFilterByRating.visibility == View.VISIBLE) {
            val tempUserRating = binding.fFilterSldFilterByUserRating.value
            filterContainer.userRating = if (tempUserRating != 0f)
                    tempUserRating
                else
                    null



            val tempCriticsRating = binding.fFilterSldFilterByCriticsRating.value
            filterContainer.criticsRating = if (tempCriticsRating != 0f)
                     tempCriticsRating
                else
                    null
        }
    }

    private fun initializeCategory() {
        initializeChipGroup(binding.fFilterCgFilterByCategory, filterContainer.categoryList, ViewModelFilter::getCategory)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByCategory, binding.fFilterCgFilterByCategory)
    }

    private fun initializeGenres(){
        initializeChipGroup(binding.fFilterCgFilterByGenres,filterContainer.genreList, ViewModelFilter::getGenres)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByGenre, binding.fFilterCgFilterByGenres)
    }

    private fun initializePlayerPerspectives(){
        initializeChipGroup(binding.fFilterCgFilterByPlayerPerspectives, filterContainer.playerPerspectiveList, ViewModelFilter::getPlayerPerspectives)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByPlayerPerspective, binding.fFilterCgFilterByPlayerPerspectives)
    }

    private fun initializeGameModes(){
        initializeChipGroup(binding.fFilterCgFilterByGameModes, filterContainer.gameModesList, ViewModelFilter::getGameModes)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByGameMode, binding.fFilterCgFilterByGameModes)
    }

    private fun initializeThemes(){
        initializeChipGroup(binding.fFilterCgFilterByTheme, filterContainer.themeList, ViewModelFilter::getThemes)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByTheme, binding.fFilterCgFilterByTheme)
    }

    private fun initializeReleaseDate() {
        initializeTextViewSetOnClick(binding.fFilterTvFilterByReleaseDate, binding.fFilterLlReleaseDate, drawerLayout)
        viewModel.getYears {
            binding.fFilterSldFilterByReleaseDate.valueFrom = it.first()
            yearMin = it.first().toInt()
            binding.fFilterSldFilterByReleaseDate.valueTo = it.last()
            yearMax = it.last().toInt()
            val tempMin = if (filterContainer.releaseDateMin != null ) filterContainer.releaseDateMin else it.first()
            val tempMax = if (filterContainer.releaseDateMax != null ) filterContainer.releaseDateMax else it.last()
            binding.fFilterSldFilterByReleaseDate.values = listOf<Float>(tempMin!!, tempMax!!)
        }
    }

    private fun initializePlatforms(offset: Int = 0) {
        initializeChipGroup(binding.fFilterCgFilterByPlatform, null, ViewModelFilter::getPlatforms)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByPlatform, binding.fFilterCgFilterByPlatform, null, binding.fFilterRlFilterByPlatform)
        initializeMorePlatforms(offset)

        binding.fFilterTvLoadMorePlatforms.setOnClickListener {
            binding.fFilterCpLoadingPlatformsIndicator.visibility = View.VISIBLE
            binding.fFilterTvLoadMorePlatforms.visibility = View.GONE
            viewModel.getPlatforms { list ->
                list.forEach {
                    val chip = createChip(it.id, it.name, binding.fFilterCgFilterByPlatform)
                    binding.fFilterCgFilterByPlatform.addView(chip)
                }
                offsetPlatforms++
                binding.fFilterCpLoadingPlatformsIndicator.visibility = View.GONE
                binding.fFilterTvLoadMorePlatforms.visibility = View.VISIBLE
            }

        }
    }

    private fun initializeMorePlatforms(offset: Int) {
        if (offset > 0) {
            viewModel.getPlatforms {
                it.forEach {
                    val chip = createChip(it.id, it.name, binding.fFilterCgFilterByPlatform)
                    binding.fFilterCgFilterByPlatform.addView(chip)
                }
                offsetPlatforms++
                if (offset > offsetPlatforms)
                    initializeMorePlatforms(offset)
                else {
                    if (filterContainer.platformList != null) {
                        filterContainer.platformList?.forEach { id ->
                            binding.fFilterCgFilterByPlatform.check(id)
                        }
                    }
                }
            }
        }
        else {
            if (filterContainer.platformList != null) {
                filterContainer.platformList?.forEach { id ->
                    binding.fFilterCgFilterByPlatform.check(id)
                }
            }
        }
    }

    private fun initializeRating() {
        initializeTextViewSetOnClick(binding.fFilterTvFilterByRating, binding.fFilterLlRating, drawerLayout)
        if (filterContainer.userRating != null) {
            binding.fFilterSldFilterByUserRating.value = filterContainer.userRating!!
        }
        if (filterContainer.criticsRating != null) {
            binding.fFilterSldFilterByCriticsRating.value = filterContainer.criticsRating!!
        }
    }

    private fun initializeStatus() {
        initializeChipGroup(binding.fFilterCgFilterByStatus, filterContainer.statusList, ViewModelFilter::getStatus)
        initializeTextViewSetOnClick(binding.fFilterTvFilterByStatus, binding.fFilterCgFilterByStatus)
    }

    fun getMap() : Map<String, Any> {
        val map : HashMap<String, Any> = HashMap()
        map[tagContainer] = filterContainer
        map[tagOffsetPlatforms] = offsetPlatforms
        return map
    }

    private fun initializeSorting() {
        if (filterContainer.sorting != null && filterContainer.sorting.toString() != "Default") {
            binding.fFilterActvChangeSort.setText(filterContainer.sorting as Editable, false)
        }
    }




}