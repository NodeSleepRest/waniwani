package com.example.wani.waniwani

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Wani() : AppCompatActivity() {
    // 各ワニ
    lateinit var waniImage: ImageView
    // 各ワニに対する攻撃エフェクト
    lateinit var attackImage: ImageView
    // 各ワニの残機
    var waniPhysical: Int = 0
    // 各ワニの残機最大値
    var waniPhysicalMax: Int = 2
    // 各ワニの耐久力（攻撃を受けても引き下がらない値）
    var waniToughness: Int = 0
    // 各ワニの耐久力最大値
    var waniToughnessMax: Int = 1
    // 各ワニが攻撃態勢をとっているか
    var activeFlagWani: Boolean = false
    // ビジネスマンが各ワニに攻撃をしかける残りフレーム数
    var attackFlagBusinessManTime: Int = 0
}