package com.cip.cipstudio.view.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cip.cipstudio.R
import com.cip.cipstudio.adapters.GamesBigRecyclerViewAdapter
import com.cip.cipstudio.adapters.RecentSearchesRecyclerViewAdapter
import com.cip.cipstudio.dataSource.repository.HistoryRepository
import com.cip.cipstudio.dataSource.repository.RecentSearchesRepository
import com.cip.cipstudio.dataSource.repository.historyRepositoryImpl.RecentSearchesRepositoryLocal
import com.cip.cipstudio.databinding.FragmentSearchBinding
import com.cip.cipstudio.model.User
import com.cip.cipstudio.utils.ActionGameDetailsEnum
import com.cip.cipstudio.utils.AuthTypeErrorEnum
import com.cip.cipstudio.utils.GameTypeEnum
import com.cip.cipstudio.view.MainActivity
import com.cip.cipstudio.viewmodel.SearchViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.*

class SearchFragment : Fragment() {

    private lateinit var searchBinding: FragmentSearchBinding
    private lateinit var searchViewModel: SearchViewModel
    private val TAG = "SearchFragment"
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var searchDB : RecentSearchesRepository

    private var resultsOffset : Int = 0
    private var recentOffset : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

        searchViewModel = SearchViewModel(searchBinding)

        initializeSearchView()

        searchBinding.executePendingBindings()
        searchBinding.lifecycleOwner = this

        // roba che fa avviata la gui

        sharedPreferences = searchBinding.root.context.getSharedPreferences(
            getString(R.string.setting_preferences),
            AppCompatActivity.MODE_PRIVATE
        )



        return searchBinding.root
    }


    private fun initializeSearchView() {

        searchBinding.fSearchBg.visibility = View.VISIBLE

        searchBinding.fSearchSearchBox.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                initializeRecentSearchesList(newText)

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                // roba che fa avviata la ricerca
                initializeSearchResultsList(query)

                return false
            }

        })

    }

    fun initializeSearchResultsList(query: String) {
        // SearchResults

        searchBinding.fSearchBg.visibility = View.GONE
        searchBinding.fSearchHistory.visibility = View.GONE
        searchBinding.fSearchResults.visibility = View.VISIBLE

        searchDB = RecentSearchesRepositoryLocal(requireContext())

        GlobalScope.launch {
            User.addSearchToRecentlySearched(query, searchDB)
        }

        initializeResultsRecyclerView(
            query,
            searchBinding.fSearchResults,
            searchBinding.fSearchShimmerLayoutResults
        )
    }

    // senza shimmer, non posso usare la stessa funzione, da vedere dopo
    fun initializeRecentSearchesList(newText: String = "") {
        // SearchHistory

        searchBinding.fSearchBg.visibility = View.GONE
        searchBinding.fSearchResults.visibility = View.GONE
        searchBinding.fSearchHistory.visibility = View.VISIBLE

        initializeRecentRecyclerView(
            newText,
            searchBinding.fSearchHistoryList
        )
    }

    private fun initializeResultsRecyclerView(
        query: String,
        recyclerView: RecyclerView,
        shimmerLayout: ShimmerFrameLayout
    ) {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        val adapter = GamesBigRecyclerViewAdapter(
            requireContext(),
            ArrayList(),
            ActionGameDetailsEnum.SEARCH
        )

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(50)

        // chiamata iniziale

        resultsOffset = 0

        searchViewModel.addGameResults(resultsOffset, query) {
            adapter.addItems(it)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
        }

        // setta l'onscroll listener

        searchBinding.fSearchResults.clearOnScrollListeners()

        searchBinding.fSearchResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && searchViewModel.isPageLoading.value == false) {
                    resultsOffset++
                    Log.i(TAG, "OFFSET")
                    Log.i(TAG, resultsOffset.toString())
                    searchViewModel.addGameResults(resultsOffset, query) { games ->
                        (searchBinding.fSearchResults.adapter as GamesBigRecyclerViewAdapter).addItems(games)
                        Log.i(TAG, games.toString())
                    }
                    Log.i(TAG, "onScrollStateChanged")

                }
            }
        })

    }

    private fun initializeRecentRecyclerView(
        query: String,
        recyclerView: RecyclerView
    ) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        val adapter = RecentSearchesRecyclerViewAdapter(
            requireContext(),
            ArrayList(),
            ::initializeSearchResultsList
            //ActionGameDetailsEnum.SEARCH
        )

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(50)

        searchDB = RecentSearchesRepositoryLocal(requireContext())

        recentOffset = 0

        searchViewModel.addRecentSearches(recentOffset, query, searchDB) {
            adapter.addItems(it as ArrayList<String>)
        }

        searchBinding.fSearchResults.clearOnScrollListeners()

        searchBinding.fSearchHistoryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && searchViewModel.isPageLoading.value == false) {
                    recentOffset++
                    Log.i(TAG, "OFFSET")
                    Log.i(TAG, recentOffset.toString())
                    searchViewModel.addRecentSearches(recentOffset, query, searchDB) { queries ->
                        (searchBinding.fSearchHistoryList.adapter as RecentSearchesRecyclerViewAdapter).addItems(queries as ArrayList<String>)
                        Log.i(TAG, queries.toString())
                    }
                    Log.i(TAG, "onScrollStateChanged")

                }
            }
        })

    }

}