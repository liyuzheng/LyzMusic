package yz.li.music.fragment

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import yz.li.core_mvvm.BaseBindingFragment
import yz.li.music.databinding.MainFrgBinding


/**
 * @Description: 
 * @Author: yuzhe
 * @CreateDate: 2026/11/17 17:36
 */
@AndroidEntryPoint
class MainFrg : BaseBindingFragment<MainFrgBinding>(MainFrgBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}