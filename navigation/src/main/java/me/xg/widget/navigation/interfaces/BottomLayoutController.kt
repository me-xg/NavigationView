package me.xg.widget.navigation.interfaces

import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

interface BottomLayoutController {
    /**
     * 方便适配ViewPager页面切换
     *
     *
     * 注意：ViewPager页面数量必须等于导航栏的Item数量
     *
     * @param viewPager [ViewPager]
     */
    fun setupWithViewPager(viewPager: ViewPager)

    /**
     * 方便适配ViewPager页面切换
     *
     *
     * 注意：ViewPager2页面数量必须等于导航栏的Item数量
     *
     * @param viewPager [ViewPager2]
     */
    fun setupWithViewPager2(viewPager: ViewPager2)

    /**
     * 向下移动隐藏导航栏
     */
    fun hideBottomLayout()

    /**
     * 向上移动显示导航栏
     */
    fun showBottomLayout()
}