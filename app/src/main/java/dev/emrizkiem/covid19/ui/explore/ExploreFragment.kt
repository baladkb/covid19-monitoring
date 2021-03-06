package dev.emrizkiem.covid19.ui.explore

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.emrizkiem.covid19.R
import dev.emrizkiem.covid19.data.model.explore.ArticlesItem
import dev.emrizkiem.covid19.ui.explore.adapter.ExploreAdapter
import kotlinx.android.synthetic.main.fragment_explore.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.jamshid.library.progress_bar.CircleProgressBar

class ExploreFragment : Fragment() {

    private lateinit var adapter: ExploreAdapter

    private val exploreViewModel: ExploreViewModel by viewModel()
    private val listExplore: MutableList<ArticlesItem> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null) {
            adapter = ExploreAdapter(listExplore) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(it.url))
            }
            swipeRefresh.setRefreshListener {
                Handler().postDelayed({
                    exploreViewModel.getExplore()
                }, 3000)
            }
            context?.let { CircleProgressBar(it) }?.let { swipeRefresh.setCustomBar(it) }

            rv_explore.setHasFixedSize(true)
            rv_explore.layoutManager = LinearLayoutManager(context)
            rv_explore.adapter = adapter
            rv_explore.visibility = View.VISIBLE
            observeViewModel()
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observeViewModel() {
        exploreViewModel.state.observe(this, Observer {
            swipeRefresh.setRefreshing(false)
            shimmerExplore.startShimmer()
        })
        exploreViewModel.explore.observe(this, Observer {
            it?.let {
                listExplore.clear()
                listExplore.addAll(it)
                adapter.notifyDataSetChanged()
                shimmerExplore.stopShimmer()
                shimmerExplore.visibility = View.GONE
            }
        })
        exploreViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            shimmerExplore.visibility = View.GONE
        })
    }

    override fun onResume() {
        super.onResume()
        shimmerExplore.startShimmer()
    }

    override fun onPause() {
        shimmerExplore.stopShimmer()
        super.onPause()
    }
}
