package yz.li.feature.home

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.KeepAliveTabFragment
import yz.li.feature.home.databinding.HomeFrgBinding


/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/17 17:36
 */
@AndroidEntryPoint
class HomeFrg : KeepAliveTabFragment<HomeFrgBinding>(HomeFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initViewAndLoadData() {

    }
}