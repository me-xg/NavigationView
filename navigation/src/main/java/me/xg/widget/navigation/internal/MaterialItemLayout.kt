package me.xg.widget.navigation.internal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import me.xg.widget.R
import me.xg.widget.navigation.MaterialMode
import me.xg.widget.navigation.interfaces.ItemController

import me.xg.widget.navigation.item.BaseTabItem
import me.xg.widget.navigation.item.MaterialItemView
import me.xg.widget.navigation.listener.OnTabItemSelectedListener
import me.xg.widget.navigation.listener.SimpleTabItemSelectedListener
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author xg
 * des ：存放 Material Design 风格按钮的水平布局
 */
class MaterialItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), ItemController {
    private val materialBottomNavigationActiveItemMaxWidth: Int
    private val materialBottomNavigationItemMaxWidth: Int
    private val bottomNavigationItemMinWidth: Int
    private val bottomNavigationItemHeight: Int
    private val mSimpleListeners: MutableList<SimpleTabItemSelectedListener> = ArrayList()
    private val mListeners: MutableList<OnTabItemSelectedListener> = ArrayList()
    private val mItems: MutableList<MaterialItemView> = ArrayList()
    private var mItemTintIcon = false
    private var mItemDefaultColor = 0

    //原质化设计规范限制最多只能有5个导航按钮
    private val mTempChildWidths: IntArray = IntArray(5)
    private var mItemTotalWidth = 0
    private var mSelected = -1
    private var mOldSelected = -1
    private var mHideTitle = false
    private lateinit var mInterpolator: Interpolator
    private var mChangeBackgroundMode = false
    private lateinit var mBackgroundColors: MutableList<Int>
    private lateinit var mOvals: MutableList<Oval>
    private lateinit var mTempRectF: RectF
    private lateinit var mPaint: Paint

    /**
     * 最后手指抬起的坐标
     */
    private var mLastUpX = 0f
    private var mLastUpY = 0f

    init {
        val res = resources
        materialBottomNavigationActiveItemMaxWidth =
            res.getDimensionPixelSize(R.dimen.material_bottom_navigation_active_item_max_width)
        materialBottomNavigationItemMaxWidth =
            res.getDimensionPixelSize(R.dimen.material_bottom_navigation_item_max_width)
        bottomNavigationItemMinWidth =
            res.getDimensionPixelSize(R.dimen.material_bottom_navigation_item_min_width)
        bottomNavigationItemHeight =
            res.getDimensionPixelSize(R.dimen.material_bottom_navigation_height)
    }

    /**
     * 初始化方法
     *
     * @param items                按钮集合
     * @param checkedColors        选中颜色的集合
     * @param mode                 [MaterialMode]
     * @param animateLayoutChanges 是否应用默认的布局动画
     * @param doTintIcon           item是否需要对图标染色
     * @param color                item的默认状态颜色
     */
    fun initialize(
        items: List<MaterialItemView>,
        checkedColors: MutableList<Int>,
        mode: Int,
        animateLayoutChanges: Boolean,
        doTintIcon: Boolean,
        color: Int
    ) {
        if (animateLayoutChanges) {
            layoutTransition = LayoutTransition()
        }
        mItems.clear()
        mItems.addAll(items)
        mItemTintIcon = doTintIcon
        mItemDefaultColor = color

        //判断是否需要切换背景
        val defaultSelected = 0
        if (mode and MaterialMode.CHANGE_BACKGROUND_COLOR > 0) {
            //初始化一些成员变量
            mChangeBackgroundMode = true
            mOvals = ArrayList()
            mBackgroundColors = checkedColors
            mInterpolator = AccelerateDecelerateInterpolator()
            mTempRectF = RectF()
            mPaint = Paint()

            //设置默认的背景
            setBackgroundColor(mBackgroundColors[defaultSelected])
        } else {
            //设置按钮点击效果
            for (i in mItems.indices) {
                val v = mItems[i]
                v.background = RippleDrawable(
                    ColorStateList(
                        arrayOf(intArrayOf()),
                        intArrayOf(0xFFFFFF and checkedColors[i] or 0x56000000)
                    ), null, null
                )
            }
        }

        //判断是否隐藏文字
        if (mode and MaterialMode.HIDE_TEXT > 0) {
            mHideTitle = true
            for (v in mItems) {
                v.setHideTitle(true)
            }
        }

        //添加按钮到布局，并注册点击事件
        val n = mItems.size
        for (i in 0 until n) {
            val tabItem = mItems[i]
            tabItem.setChecked(false)
            this.addView(tabItem)
            tabItem.setOnClickListener {
                val index = mItems.indexOf(tabItem)
                if (index >= 0) {
                    setSelect(index, mLastUpX, mLastUpY, true)
                }
            }
        }

        //默认选中第一项
        mSelected = defaultSelected
        mItems[defaultSelected].setChecked(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //排除空状态
        if (mItems.size <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val count = childCount
        val heightSpec =
            MeasureSpec.makeMeasureSpec(bottomNavigationItemHeight, MeasureSpec.EXACTLY)
        if (mHideTitle) {
            val inactiveCount = count - 1
            val activeMaxAvailable =
                width - inactiveCount * bottomNavigationItemMinWidth
            val activeWidth =
                min(activeMaxAvailable, materialBottomNavigationActiveItemMaxWidth)
            val inactiveMaxAvailable =
                if (inactiveCount == 0) 0 else (width - activeWidth) / inactiveCount
            val inactiveWidth =
                min(inactiveMaxAvailable, materialBottomNavigationItemMaxWidth)
            for (i in 0 until count) {
                when (i) {
                    mSelected -> {
                        mTempChildWidths[i] =
                            ((activeWidth - inactiveWidth) * mItems[mSelected].getAnimValue() + inactiveWidth).toInt()
                    }
                    mOldSelected -> {
                        mTempChildWidths[i] =
                            (activeWidth - (activeWidth - inactiveWidth) * mItems[mSelected].getAnimValue()).toInt()
                    }
                    else -> {
                        mTempChildWidths[i] = inactiveWidth
                    }
                }
            }
        } else {
            val maxAvailable = width / if (count == 0) 1 else count
            val childWidth =
                min(maxAvailable, materialBottomNavigationActiveItemMaxWidth)
            for (i in 0 until count) {
                mTempChildWidths[i] = childWidth
            }
        }
        mItemTotalWidth = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(mTempChildWidths[i], MeasureSpec.EXACTLY),
                heightSpec
            )
            val params = child.layoutParams
            params.width = child.measuredWidth
            mItemTotalWidth += child.measuredWidth
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val width = right - left
        val height = bottom - top
        //只支持top、bottom的padding
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        var used = 0
        if (mItemTotalWidth in 1 until width) {
            used = (width - mItemTotalWidth) / 2
        }
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                child.layout(
                    width - used - child.measuredWidth,
                    paddingTop,
                    width - used,
                    height - paddingBottom
                )
            } else {
                child.layout(used, paddingTop, child.measuredWidth + used, height - paddingBottom)
            }
            used += child.measuredWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mChangeBackgroundMode) {
            val width = width
            val height = height
            val iterator = mOvals.iterator()
            while (iterator.hasNext()) {
                val oval = iterator.next()
                mPaint.color = oval.color
                if (oval.r < oval.maxR) {
                    mTempRectF[oval.left, oval.top, oval.right] = oval.bottom
                    canvas.drawOval(mTempRectF, mPaint)
                } else {
                    setBackgroundColor(oval.color)
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint)
                    iterator.remove()
                }
                invalidate()
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            mLastUpX = ev.x
            mLastUpY = ev.y
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return MaterialItemLayout::class.java.name
    }

    override fun setSelect(index: Int) {
        setSelect(index, true)
    }

    override fun setSelect(index: Int, listener: Boolean) {
        // 不正常的选择项
        if (index >= mItems.size || index < 0) {
            return
        }
        val v: View = mItems[index]
        setSelect(index, v.x + v.width / 2f, v.y + v.height / 2f, listener)
    }

    override fun setMessageNumber(index: Int, number: Int) {
        mItems[index].setMessageNumber(number)
    }

    override fun setHasMessage(index: Int, hasMessage: Boolean) {
        mItems[index].setHasMessage(hasMessage)
    }

    override fun addTabItemSelectedListener(listener: OnTabItemSelectedListener) {
        mListeners.add(listener)
    }

    override fun addSimpleTabItemSelectedListener(listener: SimpleTabItemSelectedListener) {
        mSimpleListeners.add(listener)
    }

    override fun setTitle(index: Int, title: String) {
        mItems[index].title = title
    }

    override fun setDefaultDrawable(index: Int, drawable: Drawable) {
        mItems[index].setDefaultDrawable(drawable)
    }

    override fun setSelectedDrawable(index: Int, drawable: Drawable) {
        mItems[index].setSelectedDrawable(drawable)
    }

    override fun getSelected(): Int {
        return mSelected
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun getItemTitle(index: Int): String {
        return mItems[index].title
    }

    override fun removeItem(index: Int): Boolean {
        if (index == mSelected || index >= mItems.size || index < 0) {
            return false
        }
        if (mSelected > index) {
            mSelected--
        }
        removeViewAt(index)
        mItems.removeAt(index)
        if (mChangeBackgroundMode) {
            mBackgroundColors.removeAt(index)
        }
        return true
    }

    override fun addMaterialItem(
        index: Int,
        defaultDrawable: Drawable,
        selectedDrawable: Drawable,
        title: String,
        selectedColor: Int
    ) {
        val materialItemView = MaterialItemView(context)
        materialItemView.initialization(
            title,
            defaultDrawable,
            selectedDrawable,
            mItemTintIcon,
            mItemDefaultColor,
            if (mChangeBackgroundMode) Color.WHITE else selectedColor
        )
        materialItemView.setChecked(false)
        materialItemView.setOnClickListener {
            val index1 = mItems.indexOf(materialItemView)
            if (index1 >= 0) {
                setSelect(index1)
            }
        }
        if (mHideTitle) {
            // 隐藏文字
            materialItemView.setHideTitle(true)
        }
        if (mSelected >= index) {
            mSelected++
        }
        if (index >= mItems.size) {
            if (mChangeBackgroundMode) {
                mBackgroundColors.add(selectedColor)
            }
            mItems.add(materialItemView)
            this.addView(materialItemView)
        } else {
            if (mChangeBackgroundMode) {
                mBackgroundColors.add(index, selectedColor)
            }
            mItems.add(index, materialItemView)
            this.addView(materialItemView, index)
        }
    }

    override fun addCustomItem(index: Int, item: BaseTabItem) {
        // nothing
    }

    private fun setSelect(index: Int, x: Float, y: Float, needListener: Boolean) {

        //重复选择
        if (index == mSelected) {
            if (needListener) {
                for (listener in mListeners) {
                    listener.onRepeat(mSelected)
                }
            }
            return
        }

        //记录前一个选中项和当前选中项
        mOldSelected = mSelected
        mSelected = index

        //切换背景颜色
        if (mChangeBackgroundMode) {
            addOvalColor(mBackgroundColors[mSelected], x, y)
        }

        //前一个选中项必须不小于0才有效
        if (mOldSelected >= 0) {
            mItems[mOldSelected].setChecked(false)
        }
        mItems[mSelected].setChecked(true)
        if (needListener) {
            //事件回调
            for (listener in mListeners) {
                listener.onSelected(mSelected, mOldSelected)
            }
            for (listener in mSimpleListeners) {
                listener.onSelected(mSelected, mOldSelected)
            }
        }
    }

    /**
     * 添加一个圆形波纹动画
     *
     * @param color 颜色
     * @param x     X座标
     * @param y     y座标
     */
    private fun addOvalColor(color: Int, x: Float, y: Float) {
        val oval = Oval(color, 2f, x, y)
        oval.maxR = getR(x, y)
        mOvals.add(oval)
        val valueAnimator = ValueAnimator.ofFloat(oval.r, oval.maxR)
        valueAnimator.interpolator = mInterpolator

        // 切换背景颜色时使用
        val animTime = 300
        valueAnimator.duration = animTime.toLong()
        valueAnimator.addUpdateListener { valueAnimator1: ValueAnimator ->
            oval.r = valueAnimator1.animatedValue as Float
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                invalidate()
            }
        })
        valueAnimator.start()
    }

    /**
     * 以矩形内一点为圆心画圆，覆盖矩形，求这个圆的最小半径
     *
     * @param x 横坐标
     * @param y 纵坐标
     * @return 最小半径
     */
    private fun getR(x: Float, y: Float): Float {
        val width = width
        val height = height
        val r1Square = (x * x + y * y).toDouble()
        val r2Square = ((width - x) * (width - x) + y * y).toDouble()
        val r3Square = ((width - x) * (width - x) + (height - y) * (height - y)).toDouble()
        val r4Square = (x * x + (height - y) * (height - y)).toDouble()
        return sqrt(
            max(max(r1Square, r2Square), max(r3Square, r4Square))
        ).toFloat()
    }

    private inner class Oval(
        var color: Int,
        var r: Float,
        var x: Float,
        var y: Float
    ) {
        var maxR = 0f
        val left: Float
            get() = x - r
        val top: Float
            get() = y - r
        val right: Float
            get() = x + r
        val bottom: Float
            get() = y + r
    }

}