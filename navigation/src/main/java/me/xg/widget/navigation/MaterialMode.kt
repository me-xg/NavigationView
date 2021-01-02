package me.xg.widget.navigation

/**
 * @author Lyle
 * des ：
 *
 * 模式选择。
 *
 * 采用组合的形式，比如要两种效果可以这样做：
 * MaterialMode.HIDE_TEXT | MaterialMode.CHANGE_BACKGROUND_COLOR
 */
object MaterialMode {
    /**
     * 隐藏文字内容，只在选中时显示文字
     */
    const val HIDE_TEXT = 0X1

    /**
     * 开启导航栏背景变换。点击不同项切换不同背景颜色
     */
    const val CHANGE_BACKGROUND_COLOR = 0X2
}