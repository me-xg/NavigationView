package me.xg.widget.navigation


import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import me.xg.widget.R
import me.xg.widget.navigation.interfaces.BottomLayoutController
import me.xg.widget.navigation.interfaces.ItemController
import me.xg.widget.navigation.internal.MaterialItemLayout
import me.xg.widget.navigation.item.MaterialItemView
import me.xg.widget.navigation.listener.OnTabItemSelectedListener
import me.xg.widget.navigation.utils.NavigationUtils
import me.xg.widget.navigation.utils.Utils.getColorPrimary
import me.xg.widget.navigation.utils.Utils.newDrawable
import java.util.*
import kotlin.math.max

/**
 * @author xg
 * Date：2020/12/12
 * Des：导航栏
 */
@Suppress("unused")
class NavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private lateinit var mViewPager: ViewPager

    private lateinit var mViewPager2: ViewPager2

    private lateinit var mNavigationUtils: NavigationUtils

    private var mViewPagerPageChangeListener: ViewPagerPageChangeListener? = null

    var mTabPaddingTop: Int = 0

    var mTabPaddingBottom: Int = 0

    init {
        initAttributes(attrs)
    }

    private fun initAttributes(attrs: AttributeSet?) {

        setPadding(0, 0, 0, 0)

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.NavigationView)

        if (typedArray.hasValue(R.styleable.NavigationView_navigationPaddingTop)) {
            mTabPaddingTop = typedArray.getDimensionPixelSize(
                R.styleable.NavigationView_navigationPaddingTop, 0
            )
        }

        if (typedArray.hasValue(R.styleable.NavigationView_navigationPaddingBottom)) {
            mTabPaddingBottom =
                typedArray.getDimensionPixelSize(
                    R.styleable.NavigationView_navigationPaddingBottom, 0
                )
        }
        typedArray.recycle()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val count = childCount

        var maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        var maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxWidth = max(maxWidth, child.measuredWidth)
            maxHeight = max(maxHeight, child.measuredHeight)
        }
        setMeasuredDimension(maxWidth, maxHeight)

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        val count = childCount
        val width = r - l
        val height = b - t

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            child.layout(0, 0, width, height)
        }
    }

    inner class Builder {
        private val itemDataList: MutableList<MaterialItemViewData> = mutableListOf()
        private val defaultColorConst = 0x56000000
        private var animateLayoutChanges = false
        private var messageBackgroundColor = 0
        private var doTintIcon = true
        private var messageNumberColor = 0
        private var defaultColor = 0
        private var mode = 0

        /**
         * build
         *
         * @return {@link NavigationUtils }
         * @throws RuntimeException 没有添加tab时会抛出
         */
        fun build(): NavigationUtils {

            //没有添加tab
            if (itemDataList.isEmpty()) {
                throw RuntimeException("must add a navigation item")
            }

            // 设置默认颜色
            if (defaultColor == 0) {
                defaultColor = defaultColorConst
            }

            //水平布局
            val changeBackground = mode and MaterialMode.CHANGE_BACKGROUND_COLOR > 0
            val items: MutableList<MaterialItemView> = ArrayList<MaterialItemView>()
            val checkedColors: MutableList<Int> = ArrayList()
            for (data in itemDataList) {
                // 记录设置的选中颜色
                checkedColors.add(data.checkedColor)
                val materialItemView = MaterialItemView(context)
                // 初始化Item,需要切换背景颜色就将选中颜色改成白色
                materialItemView.initialization(
                    data.title,
                    data.drawable,
                    data.checkedDrawable,
                    doTintIcon,
                    defaultColor,
                    if (changeBackground) Color.WHITE else data.checkedColor
                )

                //检查是否设置了消息圆点的颜色
                if (messageBackgroundColor != 0) {
                    materialItemView.setMessageBackgroundColor(messageBackgroundColor)
                }

                //检查是否设置了消息数字的颜色
                if (messageNumberColor != 0) {
                    materialItemView.setMessageNumberColor(messageNumberColor)
                }
                items.add(materialItemView)
            }
            val materialItemLayout = MaterialItemLayout(context)
            materialItemLayout.initialize(
                items,
                checkedColors,
                mode,
                animateLayoutChanges,
                doTintIcon,
                defaultColor
            )
            materialItemLayout.setPadding(0, mTabPaddingTop, 0, mTabPaddingBottom)
            this@NavigationView.removeAllViews()
            this@NavigationView.addView(materialItemLayout)

            val itemController: ItemController = materialItemLayout
            mNavigationUtils = NavigationUtils(Controller(), itemController)
            mNavigationUtils.addTabItemSelectedListener(mTabItemListener)

            return mNavigationUtils
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawableRes 图标资源
         * @param title       显示文字内容.尽量简短
         * @return [Builder]
         */
        fun addItem(
            @DrawableRes drawableRes: Int,
            title: String
        ): Builder {
            addItem(drawableRes, drawableRes, title, getColorPrimary(context))
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawableRes        图标资源
         * @param checkedDrawableRes 选中时的图标资源
         * @param title              显示文字内容.尽量简短
         * @return [Builder]
         */
        fun addItem(
            @DrawableRes drawableRes: Int,
            @DrawableRes checkedDrawableRes: Int,
            title: String
        ): Builder {
            addItem(
                drawableRes,
                checkedDrawableRes,
                title,
                getColorPrimary(context)
            )
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawableRes  图标资源
         * @param title        显示文字内容.尽量简短
         * @param checkedColor 选中的颜色
         * @return [Builder]
         */
        fun addItem(
            @DrawableRes drawableRes: Int,
            title: String,
            @ColorInt checkedColor: Int
        ): Builder {
            addItem(drawableRes, drawableRes, title, checkedColor)
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawableRes        图标资源
         * @param checkedDrawableRes 选中时的图标资源
         * @param title              显示文字内容.尽量简短
         * @param checkedColor       选中的颜色
         * @return [Builder]
         * @throws Resources.NotFoundException drawable 资源获取异常
         */
        fun addItem(
            @DrawableRes drawableRes: Int,
            @DrawableRes checkedDrawableRes: Int,
            title: String,
            @ColorInt checkedColor: Int
        ): Builder {
            val defaultDrawable = ContextCompat.getDrawable(context, drawableRes)
            val checkDrawable = ContextCompat.getDrawable(context, checkedDrawableRes)
            if (defaultDrawable == null) {
                throw NotFoundException("Resource ID " + Integer.toHexString(drawableRes))
            }
            if (checkDrawable == null) {
                throw NotFoundException("Resource ID " + Integer.toHexString(checkedDrawableRes))
            }
            addItem(defaultDrawable, checkDrawable, title, checkedColor)
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawable 图标资源
         * @param title    显示文字内容.尽量简短
         * @return [Builder]
         */
        fun addItem(
            drawable: Drawable,
            title: String
        ): Builder {
            addItem(drawable, drawable, title, getColorPrimary(context))
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawable        图标资源
         * @param checkedDrawable 选中时的图标资源
         * @param title           显示文字内容.尽量简短
         * @return [Builder]
         */
        fun addItem(
            drawable: Drawable,
            checkedDrawable: Drawable,
            title: String
        ): Builder {
            addItem(drawable, checkedDrawable, title, getColorPrimary(context))
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawable    图标资源
         * @param title       显示文字内容.尽量简短
         * @param checkedColor 选中的颜色
         * @return [Builder]
         */
        fun addItem(
            drawable: Drawable,
            title: String,
            @ColorInt checkedColor: Int
        ): Builder {
            addItem(drawable, drawable, title, checkedColor)
            return this@Builder
        }

        /**
         * 添加一个导航按钮
         *
         * @param drawable        图标资源
         * @param checkedDrawable 选中时的图标资源
         * @param title           显示文字内容.尽量简短
         * @param checkedColor    选中的颜色
         * @return [Builder]
         */
        fun addItem(
            drawable: Drawable,
            checkedDrawable: Drawable,
            title: String,
            @ColorInt checkedColor: Int
        ): Builder {
            val data = MaterialItemViewData(
                checkedColor,
                newDrawable(checkedDrawable),
                newDrawable(drawable),
                title
            )
            itemDataList.add(data)
            return this@Builder
        }

        /**
         * 设置导航按钮的默认（未选中状态）颜色
         *
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return [Builder]
         */
        fun setDefaultColor(@ColorInt color: Int): Builder {
            defaultColor = color
            return this@Builder
        }

        /**
         * 设置消息圆点的颜色
         *
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return [Builder]
         */
        fun setMessageBackgroundColor(@ColorInt color: Int): Builder {
            messageBackgroundColor = color
            return this@Builder
        }

        /**
         * 设置消息数字的颜色
         *
         * @param color 16进制整形表示的颜色，例如红色：0xFFFF0000
         * @return [Builder]
         */
        fun setMessageNumberColor(@ColorInt color: Int): Builder {
            messageNumberColor = color
            return this@Builder
        }

        /**
         * 设置模式(在垂直布局中无效)。默认文字一直显示，且背景色不变。
         * 可以通过[MaterialMode]选择模式。
         *
         * 例如:
         * `MaterialMode.HIDE_TEXT`
         *
         * 或者多选:
         * `MaterialMode.HIDE_TEXT | MaterialMode.CHANGE_BACKGROUND_COLOR`
         *
         * @param mode [MaterialMode]
         * @return [Builder]
         */
        fun setMode(mode: Int): Builder {
            this@Builder.mode = mode
            return this@Builder
        }

        /**
         * 不对图标进行染色
         *
         * @return [Builder]
         */
        fun doNotTintIcon(): Builder {
            doTintIcon = false
            return this@Builder
        }

        /**
         * 通过[Controller]动态移除/添加导航项时,显示默认的布局动画
         *
         * @return [Builder]
         */
        fun enableAnimateLayoutChanges(): Builder {
            animateLayoutChanges = true
            return this@Builder

        }
    }

    /**
     * 材料设计的单项视图信息
     */
    private data class MaterialItemViewData(
        @ColorInt val checkedColor: Int = 0,
        val checkedDrawable: Drawable,
        val drawable: Drawable,
        val title: String
    )

    private inner class Controller : BottomLayoutController {
        private lateinit var animator: ObjectAnimator
        private var hide = false

        /**
         * 方便适配ViewPager页面切换
         *
         *
         * 注意：ViewPager页面数量必须等于导航栏的Item数量
         *
         * @param viewPager [ViewPager]
         */
        override fun setupWithViewPager(viewPager: ViewPager) {
            mViewPager = viewPager

            if (null != mViewPagerPageChangeListener) {
                mViewPager.removeOnPageChangeListener(mViewPagerPageChangeListener!!)
            } else {
                mViewPagerPageChangeListener = ViewPagerPageChangeListener()
            }
            mNavigationUtils.let {
                val position = mViewPager.currentItem
                if (mNavigationUtils.getSelected() != position) {
                    mNavigationUtils.setSelect(position)
                }
                mViewPagerPageChangeListener.let {
                    mViewPager.addOnPageChangeListener(it!!)
                }
            }
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

        }

        /**
         * 向下移动隐藏导航栏
         */
        override fun hideBottomLayout() {
            if (!hide) {
                hide = true
                getAnimator().start()
            }
        }

        /**
         * 向上移动显示导航栏
         */
        override fun showBottomLayout() {
            if (hide) {
                hide = false
                getAnimator().reverse()
            }
        }

        private fun getAnimator(): ObjectAnimator {
            // 水平布局向下隐藏
            animator = ObjectAnimator.ofFloat(
                this@NavigationView,
                "translationY",
                0f,
                this@NavigationView.height.toFloat()
            )
            animator.duration = 300
            animator.interpolator = AccelerateDecelerateInterpolator()
            return animator
        }
    }

    private inner class ViewPagerPageChangeListener : ViewPager.OnPageChangeListener {
        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position Position index of the first page currently being displayed.
         * Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        override fun onPageSelected(position: Int) {
            if (mNavigationUtils.getSelected() != position) {
                mNavigationUtils.setSelect(position)
            }
        }

        /**
         * Called when the scroll state changes. Useful for discovering when the user
         * begins dragging, when the pager is automatically settling to the current page,
         * or when it is fully stopped/idle.
         *
         * @param state The new scroll state.
         * @see ViewPager.SCROLL_STATE_IDLE
         *
         * @see ViewPager.SCROLL_STATE_DRAGGING
         *
         * @see ViewPager.SCROLL_STATE_SETTLING
         */
        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    private val mTabItemListener: OnTabItemSelectedListener = object : OnTabItemSelectedListener {
        /**
         * 选中导航栏的某一项
         *
         * @param index 索引导航按钮，按添加顺序排序
         * @param old   前一个选中项，如果没有就等于-1
         */
        override fun onSelected(index: Int, old: Int) {
            if (this@NavigationView::mViewPager.isInitialized) {
                mViewPager.setCurrentItem(index, false)
            }
        }

        /**
         * 重复选中
         *
         * @param index 索引导航按钮，按添加顺序排序
         */
        override fun onRepeat(index: Int) {
        }

    }
}