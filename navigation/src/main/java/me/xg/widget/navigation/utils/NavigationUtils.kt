package me.xg.widget.navigation.utils

import android.graphics.drawable.Drawable
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import me.xg.widget.navigation.interfaces.BottomLayoutController
import me.xg.widget.navigation.interfaces.ItemController
import me.xg.widget.navigation.item.BaseTabItem
import me.xg.widget.navigation.listener.OnTabItemSelectedListener
import me.xg.widget.navigation.listener.SimpleTabItemSelectedListener
import me.xg.widget.navigation.utils.Utils.newDrawable

class NavigationUtils constructor(
    private val mBottomLayoutController: BottomLayoutController,
    private val mItemController: ItemController
) : ItemController, BottomLayoutController {
    override fun setSelect(index: Int) {
        mItemController.setSelect(index)
    }

    override fun setSelect(index: Int, listener: Boolean) {
        mItemController.setSelect(index, listener)
    }

    override fun setMessageNumber(index: Int, number: Int) {
        mItemController.setMessageNumber(index, number)
    }

    override fun setHasMessage(index: Int, hasMessage: Boolean) {
        mItemController.setHasMessage(index, hasMessage)
    }

    override fun addTabItemSelectedListener(listener: OnTabItemSelectedListener) {
        mItemController.addTabItemSelectedListener(listener)
    }

    override fun addSimpleTabItemSelectedListener(listener: SimpleTabItemSelectedListener) {
        mItemController.addSimpleTabItemSelectedListener(listener)
    }

    override fun setTitle(index: Int, title: String) {
        mItemController.setTitle(index, title)
    }

    override fun setDefaultDrawable(index: Int, drawable: Drawable) {
        mItemController.setDefaultDrawable(index, drawable)
    }

    override fun setSelectedDrawable(index: Int, drawable: Drawable) {
        mItemController.setSelectedDrawable(index, drawable)
    }


    override fun getSelected(): Int {
        return mItemController.getSelected()
    }

    override fun getItemCount(): Int {
        return mItemController.getItemCount()
    }

    override fun getItemTitle(index: Int): String {
        return mItemController.getItemTitle(index)
    }

    override fun removeItem(index: Int): Boolean {
        return mItemController.removeItem(index)
    }

    override fun addMaterialItem(
        index: Int,
        defaultDrawable: Drawable,
        selectedDrawable: Drawable,
        title: String,
        selectedColor: Int
    ) {
        mItemController.addMaterialItem(
            index,
            newDrawable(defaultDrawable),
            newDrawable(selectedDrawable),
            title,
            selectedColor
        )
    }

    override fun addCustomItem(index: Int, item: BaseTabItem) {
        mItemController.addCustomItem(index, item)
    }

    override fun setupWithViewPager(viewPager: ViewPager) {
        mBottomLayoutController.setupWithViewPager(viewPager)
    }

    /**
     * 方便适配ViewPager页面切换
     *
     *
     * 注意：ViewPager2页面数量必须等于导航栏的Item数量
     *
     * @param viewPager [ViewPager2]
     */
    override fun setupWithViewPager2(viewPager: ViewPager2) {
        mBottomLayoutController.setupWithViewPager2(viewPager)
    }

    override fun hideBottomLayout() {
        mBottomLayoutController.hideBottomLayout()
    }

    override fun showBottomLayout() {
        mBottomLayoutController.showBottomLayout()
    }
}