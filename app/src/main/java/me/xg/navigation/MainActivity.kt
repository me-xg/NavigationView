package me.xg.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.xg.navigation.databinding.ActivityMainBinding
import me.xg.widget.navigation.MaterialMode
import me.xg.widget.navigation.listener.OnTabItemSelectedListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)
        val utils = mBinding.navigationView.Builder()
            .addItem(R.drawable.ic_favorite_gray_24dp, R.drawable.ic_favorite_teal_24dp, "1")
            .addItem(R.drawable.ic_favorite_gray_24dp, R.drawable.ic_favorite_teal_24dp, "2")
            .addItem(R.drawable.ic_favorite_gray_24dp, R.drawable.ic_favorite_teal_24dp, "3")
            //.setMode(MaterialMode.CHANGE_BACKGROUND_COLOR or MaterialMode.HIDE_TEXT)
            .doNotTintIcon()
            .build()
        utils.setHasMessage(1, true)
        mBinding.viewPager.adapter = A(supportFragmentManager, utils.getItemCount())
        // utils.setupWithViewPager(mBinding.viewPager)
        utils.addTabItemSelectedListener(object : OnTabItemSelectedListener {
            /**
             * 选中导航栏的某一项
             *
             * @param index 索引导航按钮，按添加顺序排序
             * @param old   前一个选中项，如果没有就等于-1
             */
            override fun onSelected(index: Int, old: Int) {
                Log.e("--->", "onSelected$index")
            }

            /**
             * 重复选中
             *
             * @param index 索引导航按钮，按添加顺序排序
             */
            override fun onRepeat(index: Int) {
                Log.e("--->", "onRepeat$index")
            }

        })

    }

    class A constructor(fm: FragmentManager, private val size: Int) :
        FragmentPagerAdapter(fm, size) {


        /**
         * Return the number of views available.
         */
        override fun getCount(): Int {
            return size
        }

        /**
         * Return the Fragment associated with a specified position.
         */
        override fun getItem(position: Int): Fragment {
            return AFragment.newInstance(position.toString() + "")
        }

    }
}