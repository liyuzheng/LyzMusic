package yz.li.core_mvvm

import androidx.viewbinding.ViewBinding
import yz.li.core_common.BaseAct

/**
 * desc: ViewBinding 活动基类
 * created by liyuzheng on 2023/8/28 17:25
 */
abstract class BaseBindingAct<B : ViewBinding> : BaseAct() {

    protected abstract val mBinding: B

    final override fun getLayoutId(): Int = 0

    override fun setup() {
        beforeSetContentView()
        setContentView(mBinding.root)
        setupBinding(mBinding)
        setupView()
        setupData()
    }

    /**
     * 补充 Binding 相关的初始配置（按需重写）
     */
    protected open fun setupBinding(binding: B) = Unit

    /**
     * 控件初始化（如 RecyclerView 适配器绑定、点击事件设置）
     */
    protected open fun setupView() = Unit

    /**
     * 数据加载与 StateFlow 观察
     */
    protected open fun setupData() = Unit

    open fun beforeSetContentView() = Unit

    final override fun setupContentView() {
//        super.setupContentView()
    }
}
