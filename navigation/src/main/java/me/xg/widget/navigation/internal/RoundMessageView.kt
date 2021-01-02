package me.xg.widget.navigation.internal

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import me.xg.widget.R
import me.xg.widget.navigation.utils.Utils.tinting
import java.text.MessageFormat

/**
 * @author Lyle
 * des ：底部导航内圆形消息
 */
@Suppress("unused")
class RoundMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? ,
    defStyleAttr: Int = 0
) : FrameLayout(
    context, attrs, defStyleAttr
) {
    private val mMessages: TextView
    private var mMessageNumber = 0
    private var mHasMessage = false
    private val mOval: View

    fun setHasMessage(hasMessage: Boolean) {
        mHasMessage = hasMessage
        if (hasMessage) {
            mOval.visibility =
                if (mMessageNumber > 0) {
                    INVISIBLE
                } else {
                    VISIBLE
                }
        } else {
            mOval.visibility = INVISIBLE
        }
    }

    fun tintMessageBackground(@ColorInt color: Int) {
        val drawable: Drawable =
            tinting(ContextCompat.getDrawable(context, R.drawable.round), color)
        ViewCompat.setBackground(mOval, drawable)
        ViewCompat.setBackground(mMessages, drawable)
    }

    fun setMessageNumberColor(@ColorInt color: Int) {
        mMessages.setTextColor(color)
    }

    //如果消息数是个位数 字体大小用12 两位数则用10
    var messageNumber: Int
        get() = mMessageNumber
        set(number) {
            mMessageNumber = number
            if (mMessageNumber > 0) {
                mOval.visibility = INVISIBLE
                mMessages.visibility = VISIBLE

                //如果消息数是个位数 字体大小用12 两位数则用10
                val msgLength = 10
                if (mMessageNumber < msgLength) {
                    mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                } else {
                    mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
                }
                val maxNumber = 99
                if (mMessageNumber <= maxNumber) {
                    mMessages.text = MessageFormat.format("{0}", mMessageNumber)
                } else {
                    mMessages.text = MessageFormat.format("{0}+", maxNumber)
                }
            } else {
                mMessages.visibility = INVISIBLE
                if (mHasMessage) {
                    mOval.visibility = VISIBLE
                }
            }
        }

    fun hasMessage(): Boolean {
        return mHasMessage
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_round_message_view, this, true)
        mOval = findViewById(R.id.oval)
        mMessages = findViewById(R.id.msg)
        mMessages.typeface = Typeface.DEFAULT_BOLD
        mMessages.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
    }
}