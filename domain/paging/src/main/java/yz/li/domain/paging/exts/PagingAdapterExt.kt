package yz.li.domain.paging.exts

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import yz.li.core_paging.exts.RemotePresentationState
import yz.li.core_paging.exts.asRemotePresentationState
import yz.li.core_paging.exts.refreshAdapter
import yz.li.domain.paging.adapter.EmptyAdapter
import yz.li.domain.paging.adapter.FooterAdapter

/**
 * desc:
 * createed by liyuzheng on 2023/3/29 16:41
 */

fun concatAdapter(
    adapter: PagingDataAdapter<*, *>,
    footerAdapter: FooterAdapter? = null,
    emptyAdapter: EmptyAdapter? = null
): ConcatAdapter {
    return adapter.concat(footerAdapter, emptyAdapter)
}

fun PagingDataAdapter<*, *>.concat(
    footerAdapter: FooterAdapter? = null, emptyAdapter: EmptyAdapter? = null
): ConcatAdapter {
    val concatAdapter = ConcatAdapter()
    concatAdapter.addAdapter(this)
    footerAdapter?.run {
        concatAdapter.addAdapter(this)
    }
    emptyAdapter?.run {
        concatAdapter.addAdapter(this)
    }
    return concatAdapter
}

inline fun PagingDataAdapter<*, *>.setup(
    scope: LifecycleCoroutineScope,
    rv: RecyclerView,
    crossinline stateChanged: (state: CombinedLoadStates) -> Unit
) {
    setup(scope, rv, true, stateChanged)
}

inline fun PagingDataAdapter<*, *>.setup(
    lifecycleScope: LifecycleCoroutineScope,
    rv: RecyclerView,
    requestFocusWhenLaunched: Boolean,
    crossinline stateChanged: (state: CombinedLoadStates) -> Unit
) {
    lifecycleScope.launch {
        loadStateFlow.collectLatest { loadState ->
            stateChanged(loadState)
        }
    }

    val remotePresentationState = loadStateFlow.asRemotePresentationState().map {
        it == RemotePresentationState.PRESENTED
    }

    lifecycleScope.launch {
        remotePresentationState.distinctUntilChanged().collectLatest {
            if (it) {
                rv.post {
                    if (requestFocusWhenLaunched) {
                        rv.requestFocus()
                    }
                    rv.refreshAdapter()
                }
            }
        }
    }
}