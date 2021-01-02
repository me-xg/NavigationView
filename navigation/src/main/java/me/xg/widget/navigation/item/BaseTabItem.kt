package me.xg.widget.navigation.item

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes

/**
 * 所有自定义Item都必须继承此类
 */

abstract class BaseTabItem : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    /**
     * 设置选中状态
     */
    abstract fun setChecked(checked: Boolean)

    /**
     * 设置消息数字。注意：一般数字需要大于0才会显示
     */
    abstract fun setMessageNumber(number: Int)

    /**
     * 设置是否显示无数字的小圆点。注意：如果消息数字不为0，优先显示带数字的圆
     */
    abstract fun setHasMessage(hasMessage: Boolean)

    /**
     * 设置未选中状态下的图标
     */
    abstract fun setDefaultDrawable(drawable: Drawable)

    /**
     * 设置选中状态下的图标
     */
    abstract fun setSelectedDrawable(drawable: Drawable)
    /**
     * 获取标题文字
     */
    /**
     * 设置标题
     */
    abstract var title: String

    /**
     * 已选中的状态下再次点击时触发
     */
    fun onRepeat() {}
}