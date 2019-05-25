/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.GRAVITY_FILL
import kotlinx.android.synthetic.main.activity_main.*
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.adapters.TabAdapter

/**
 * Главный экран приложения, на котором расположены Tab'ы
 * @see [AppCompatActivity]
 * @author shadowsparky
 * @since v1.0.0
 */
class MainView : AppCompatActivity() {

    /**
     * Система вызывает этот метод, когда создает активити.
     * В своей реализации разработчик должен инициализировать ключевые компоненты активити,
     * которые требуется сохранить, когда активити находится в состоянии паузы или возобновлен после остановки.
     *
     * @see [AppCompatActivity]
     * @since v1.0.0
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tab_layout.addTab(tab_layout.newTab().setText("Главное меню"))
        tab_layout.addTab(tab_layout.newTab().setText("Настройки"))
        tab_layout.tabGravity = GRAVITY_FILL
        val adapter = TabAdapter(supportFragmentManager, tab_layout.tabCount)
        pager.adapter = adapter
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab) {
            }

            override fun onTabSelected(p0: TabLayout.Tab) {
                pager.currentItem = p0.position
            }
        })
    }
}
