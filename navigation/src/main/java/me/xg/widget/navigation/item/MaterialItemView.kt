package me.xg.widget.navigation.item

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

import me.xg.widget.R
import me.xg.widget.navigation.internal.RoundMessageView
import me.xg.widget.navigation.utils.Utils.tinting

/**
 * @author xg
 * des ：原质化设计风格项
 */
@Suppress("unused")
class MaterialItemView : BaseTabItem {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mMessages: RoundMessageView
    private val mLabel: TextView
    private val mIcon: ImageView
    private lateinit var mDefaultDrawable: Drawable
    private lateinit var mCheckedDrawable: Drawable
    private var mDefaultColor = 0
    private var mCheckedColor = 0
    private val mTranslationHideTitle: Float
    private val mTranslation: Float
    private val mTopMarginHideTitle: Int
    private val mTopMargin: Int
    private var mHideTitle = false
    private var mChecked = false
    private lateinit var mAnimator: ValueAnimator

    /**
     * 获取动画运行值[0,1]
     */
    private var mAnimatorValue = 1f
    private var mIsMeasured = false
    private var mTintIcon = true

    fun initialization(
        title: String,
        drawable: Drawable,
        checkedDrawable: Drawable,
        tintIcon: Boolean,
        color: Int,
        checkedColor: Int
    ) {
        mTintIcon = tintIcon
        mDefaultColor = color
        mCheckedColor = checkedColor
        if (mTintIcon) {
            mDefaultDrawable = tinting(drawable, mDefaultColor)
            mCheckedDrawable = tinting(checkedDrawable, mCheckedColor)
        } else {
            mDefaultDrawable = drawable
            mCheckedDrawable = checkedDrawable
        }
        mLabel.text = title
        mLabel.setTextColor(color)
        mIcon.setImageDrawable(mDefaultDrawable)
        mAnimator = ValueAnimator.ofFloat(1f)
        mAnimator.duration = 115L
        mAnimator.interpolator = AccelerateDecelerateInterpolator()
        mAnimator.addUpdateListener { animation: ValueAnimator ->
            //onAnimationUpdate
            mAnimatorValue = animation.animatedValue as Float
            if (mHideTitle) {
                mIcon.translationY = -mTranslationHideTitle * mAnimatorValue
            } else {
                mIcon.translationY = -mTranslation * mAnimatorValue
            }
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f + mAnimatorValue * 2f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mIsMeasured = true
    }

    override fun setChecked(checked: Boolean) {
        if (mChecked == checked) {
            return
        }
        mChecked = checked
        if (mHideTitle) {
            mLabel.visibility = if (mChecked) VISIBLE else INVISIBLE
        }
        if (mIsMeasured) {
            // 切换动画
            if (mChecked) {
                mAnimator.start()
            } else {
                mAnimator.reverse()
            }
        } else if (mChecked) {
            // 布局还未测量时选中，直接转换到选中的最终状态
            if (mHideTitle) {
                mIcon.translationY = -mTranslationHideTitle
            } else {
                mIcon.translationY = -mTranslation
            }
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        } else {
            // 布局还未测量并且未选中，保持未选中状态
            mIcon.translationY = 0f
            mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        // 切换颜色
        if (mChecked) {
            mIcon.setImageDrawable(mCheckedDrawable)
            mLabel.setTextColor(mCheckedColor)
        } else {
            mIcon.setImageDrawable(mDefaultDrawable)
            mLabel.setTextColor(mDefaultColor)
        }
    }

    override fun setMessageNumber(number: Int) {
        mMessages.visibility = VISIBLE
        mMessages.messageNumber = number
    }

    override fun setHasMessage(hasMessage: Boolean) {
        mMessages.visibility = VISIBLE
        mMessages.setHasMessage(hasMessage)
    }

    override fun setDefaultDrawable(drawable: Drawable) {
        mDefaultDrawable = if (mTintIcon) {
            tinting(drawable, mDefaultColor)
        } else {
            drawable
        }
        if (!mChecked) {
            mIcon.setImageDrawable(mDefaultDrawable)
        }
    }

    override fun setSelectedDrawable(drawable: Drawable) {
        mCheckedDrawable = if (mTintIcon) {
            tinting(drawable, mCheckedColor)
        } else {
            drawable
        }
        if (mChecked) {
            mIcon.setImageDrawable(mCheckedDrawable)
        }
    }

    override var title: String
        get() = mLabel.text.toString()
        set(title) {
            mLabel.text = title
        }

    /**
     * 获取动画运行值[0,1]
     */
    fun getAnimValue(): Float {
        return mAnimatorValue
    }
    /**
     * 设置是否隐藏文字
     */
    fun setHideTitle(hideTitle: Boolean) {
        mHideTitle = hideTitle
        val iconParams = mIcon.layoutParams as LayoutParams
        if (mHideTitle) {
            iconParams.topMargin = mTopMarginHideTitle
        } else {
            iconParams.topMargin = mTopMargin
        }
        mLabel.visibility = if (mChecked) VISIBLE else INVISIBLE
        mIcon.layoutParams = iconParams
    }

    /**
     * 设置消息圆形的颜色
     */
    fun setMessageBackgroundColor(@ColorInt color: Int) {
        mMessages.tintMessageBackground(color)
    }

    /**
     * 设置消息数据的颜色
     */
    fun setMessageNumberColor(@ColorInt color: Int) {
        mMessages.setMessageNumberColor(color)
    }

    init {
        val scale = context.resources.displayMetrics.density
        mTranslation = scale * 2
        mTranslationHideTitle = scale * 10
        mTopMargin = (scale * 8).toInt()
        mTopMarginHideTitle = (scale * 16).toInt()
        LayoutInflater.from(context).inflate(R.layout.item_material, this, true)
        mIcon = findViewById(R.id.icon)
        mLabel = findViewById(R.id.label)
        mMessages = findViewById(R.id.messages)
    }
}