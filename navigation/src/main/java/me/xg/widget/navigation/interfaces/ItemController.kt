package me.xg.widget.navigation.interfaces

import android.graphics.drawable.Drawable
import me.xg.widget.navigation.item.BaseTabItem
import me.xg.widget.navigation.listener.OnTabItemSelectedListener
import me.xg.widget.navigation.listener.SimpleTabItemSelectedListener

interface ItemController {
    /**
     * 设置选中项
     *
     * @param index 顺序索引
     */
    fun setSelect(index: Int)

    /**
     * 设置选中项，并可以控制是否回调监听事件
     *
     * @param index    顺序索引
     * @param listener true:假如存在监听事件[OnTabItemSelectedListener]，就会调用相关的回调方法。false:不会触发监听事件
     */
    fun setSelect(index: Int, listener: Boolean)

    /**
     * 设置导航按钮上显示的圆形消息数字，通过顺序索引。
     *
     * @param index  顺序索引
     * @param number 消息数字
     */
    fun setMessageNumber(index: Int, number: Int)

    /**
     * 设置显示无数字的消息小原点
     *
     * @param index      顺序索引
     * @param hasMessage true显示
     */
    fun setHasMessage(index: Int, hasMessage: Boolean)

    /**
     * 导航栏按钮点击监听
     *
     * @param listener [OnTabItemSelectedListener]
     */
    fun addTabItemSelectedListener(listener: OnTabItemSelectedListener)

    /**
     * 导航栏按钮点击监听(只有选中事件)
     *
     * @param listener [SimpleTabItemSelectedListener]
     */
    fun addSimpleTabItemSelectedListener(listener: SimpleTabItemSelectedListener)

    /**
     * 设置标题
     *
     * @param index 顺序索引
     * @param title 标题文字
     */
    fun setTitle(index: Int, title: String)

    /**
     * 设置未选中状态下的图标
     *
     * @param index    顺序索引
     * @param drawable 图标资源
     */
    fun setDefaultDrawable(index: Int, drawable: Drawable)

    /**
     * 设置选中状态下的图标
     *
     * @param index    顺序索引
     * @param drawable 图标资源
     */
    fun setSelectedDrawable(index: Int, drawable: Drawable)

    /**
     * 获取当前选中项索引
     *
     * @return 索引
     */
    fun getSelected(): Int

    /**
     * 获取导航按钮总数
     *
     * @return 总数
     */
    fun getItemCount(): Int
    /**
     * 获取导航按钮文字
     *
     * @param index 顺序索引
     * @return 文字
     */
    fun getItemTitle(index: Int): String

    /**
     * 移除指定的导航项.需要注意,不能移除当前选中的导航项
     *
     * @return 移除是否成功
     */
    fun removeItem(index: Int): Boolean

    /**
     * 添加一个材料设计样式的TabItem.注意,只对`material()`构建的导航栏有效
     *
     * @param index            顺序索引
     * @param defaultDrawable  未选中状态的图标资源
     * @param selectedDrawable 选中状态的图标资源
     * @param title            标题
     * @param selectedColor    选中状态的颜色
     */
    fun addMaterialItem(
        index: Int,
        defaultDrawable: Drawable,
        selectedDrawable: Drawable,
        title: String,
        selectedColor: Int
    )

    /**
     * 添加一个自定义样式的TabItem.注意,只对`custom()`构建的导航栏有效
     *
     * @param index 顺序索引
     * @param item  自定义的Item
     */
    fun addCustomItem(index: Int, item: BaseTabItem)
}