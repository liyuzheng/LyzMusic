package yz.li.feature.profile

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.KeepAliveTabFragment
import yz.li.feature.profile.databinding.ProfileFrgBinding


/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/17 17:36
 */
@AndroidEntryPoint
class ProfileFrg : KeepAliveTabFragment<ProfileFrgBinding>(ProfileFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initViewAndLoadData() {

    }
}