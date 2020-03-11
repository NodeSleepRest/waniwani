package com.example.wani.waniwani

import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    // ワニの各種情報
    private var wani: Array<Wani> = Array(4, { Wani() })

    // 被捕食者
    private lateinit var enemy: ImageView

    // タイマー
    private var handler: Handler = Handler()
    private var timer: Timer = Timer()

    // ワニの初期Y位置
    private var posY: Float = 0f

    // ライフポイント表示テキスト
    private lateinit var txtLife: TextView
    private lateinit var txtFightResult: TextView

    // ライフポイント初期値
    private val intLifePointMax = 10
    private var intLifePoint: Int = intLifePointMax

    // 効果音
    private lateinit var audioAttrivutes: AudioAttributes
    private lateinit var soundPool: SoundPool
    private var attackSoundBusinessMan: Int = 0
    private var attackSoundWani: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wani[0].waniImage = findViewById(R.id.img1)
        wani[1].waniImage = findViewById(R.id.img2)
        wani[2].waniImage = findViewById(R.id.img3)
        wani[3].waniImage = findViewById(R.id.img4)

        wani[0].attackImage = findViewById(R.id.attackImg1)
        wani[1].attackImage = findViewById(R.id.attackImg2)
        wani[2].attackImage = findViewById(R.id.attackImg3)
        wani[3].attackImage = findViewById(R.id.attackImg4)

        // 各ワニの体力最大値をセット
        setPhysicalValue()
        // 各ワニの耐久力最大値をセット
        setStrongValue()
        // ワニの耐久力を画面に表示
        setTextwaniLife()
        // ワニの耐久力最大値を画面に表示
        setTextwaniStrong()

        enemy = findViewById(R.id.enemyImgView)

        txtLife = findViewById(R.id.lifePoint)
        txtLife.text = intLifePoint.toString()

        txtFightResult = findViewById(R.id.txtWinLose)

        posY = wani[0].waniImage.y + wani[0].waniImage.marginTop

        audioAttrivutes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        soundPool = SoundPool.Builder().setAudioAttributes(audioAttrivutes).setMaxStreams(2).build()
        attackSoundBusinessMan = soundPool.load(this, R.raw.punch_real1, 1)
        attackSoundWani = soundPool.load(this, R.raw.bite1, 1)

        // 各ワニクリック時の処理を設定
        for (x in wani) {
            x.waniImage.setOnClickListener {
                if (x.waniPhysical > 0) {
                    x.activeFlagWani = !x.activeFlagWani
                }
            }
        }

        btnTraining.setOnClickListener {
            val (intWaniWhich, intGrowParam, intTrainingParam) = trainingJudge()
            val txtPassString: String =
                generateTrainingResultText(intWaniWhich, intGrowParam, intTrainingParam)

            AlertDialog.Builder(this)
                .setTitle("特訓成果")
                .setMessage(txtPassString)
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    // OKをタップしたときの処理
                    setPhysicalValue()
                    setStrongValue()
                    setTextwaniLife()
                    setTextwaniStrong()
                    intLifePoint = intLifePointMax
                    txtLife.text = intLifePoint.toString()
                    enemy = findViewById(R.id.enemyImgView)
                    btnTraining.visibility = View.GONE
                })
                .show()
        }

        // タイマーでワニの移動
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(Runnable() {
                    screenFlip()
                })
            }
        }, 0, 20)
    }

    public fun screenFlip() {
        for ((index, x) in wani.withIndex()) {
            // ワニが進む距離の上限まで達すると攻撃フラグオフ
            if (x.waniImage.y > 500.0f) {
                x.activeFlagWani = false
                x.attackFlagBusinessManTime = 0
                x.attackImage.visibility = View.INVISIBLE
                x.waniImage.y = posY
                lifeDecreaseProcess(--intLifePoint)
            }
            if (x.activeFlagWani) {
                // 攻撃フラグオンの場合、前進
                x.waniImage.y += 5.0f
                attackBusinessMan(index)
            } else {
                if (--x.attackFlagBusinessManTime < 0) {
                    x.attackImage.visibility = View.INVISIBLE
                }
            }
        }
    }

    // ビジネスマンのHPが減った時に行う処理
    private fun lifeDecreaseProcess(intLife: Int) {
        // 噛みつき音再生
        soundPool.play(attackSoundWani, 1.0f, 1.0f, 0, 0, 1.0f)

        txtLife.text = intLife.toString()
        if (intLife <= 0) {
            txtFightResult.text = getString(R.string.win)
            enemy.setImageResource(R.drawable.business_man3)
            val intent = Intent(applicationContext, SubActivity::class.java)
            startActivity(intent)
        } else if (intLife > 0 && intLife < 5) {
            enemy.setImageResource(R.drawable.business_man2)
        }
    }

    // ビジネスマンからの攻撃処理
    private fun attackBusinessMan(index: Int) {
        // 攻撃エフェクトを表示する時間を減らす
        wani[index].attackFlagBusinessManTime--

        if (wani[index].attackFlagBusinessManTime == 0) { // 攻撃中⇒攻撃終了時処理
            wani[index].attackImage.visibility = View.INVISIBLE
        } else if (wani[index].attackFlagBusinessManTime < 0) { // 攻撃終了時処理
            // 乱数で攻撃判定、攻撃エフェクト可視化
            if (Math.random() * 100 > 95) {
                // 殴打音再生
                soundPool.play(attackSoundBusinessMan, 1.0f, 1.0f, 0, 0, 1.0f)
                // 攻撃されたワニの耐久力を減らす
                wani[index].waniToughness--

                // 攻撃エフェクト表示時間設定
                wani[index].attackFlagBusinessManTime = 5
                wani[index].attackImage.visibility = View.VISIBLE

                // 耐久力が0以下の場合
                if (wani[index].waniToughness <= 0) {
                    // 攻撃されたワニの攻撃フラグをオフにする
                    wani[index].activeFlagWani = false
                    // 攻撃されたワニの位置を戻す
                    wani[index].waniImage.y = posY
                    // 攻撃されたワニの残機を減らす
                    wani[index].waniPhysical--
                    // ワニの耐久力を最大値に戻す
                    wani[index].waniToughness = wani[index].waniToughnessMax
                    // 全滅チェック
                    if (checkAllKilled()) {
                        btnTraining.visibility = View.VISIBLE
                    }
                }
                setTextwaniLife()
                setTextwaniStrong()
            }
        }
    }

    // ワニの耐久力を画面に表示
    private fun setTextwaniLife() {
        txtLife1.text = wani[0].waniPhysical.toString()
        txtLife2.text = wani[1].waniPhysical.toString()
        txtLife3.text = wani[2].waniPhysical.toString()
        txtLife4.text = wani[3].waniPhysical.toString()
    }

    // ワニの耐久力最大値を画面に表示
    private fun setTextwaniStrong() {
        txtStrong1.text = wani[0].waniToughness.toString()
        txtStrong2.text = wani[1].waniToughness.toString()
        txtStrong3.text = wani[2].waniToughness.toString()
        txtStrong4.text = wani[3].waniToughness.toString()
    }

    // 各ワニの体力最大値をセット
    private fun setPhysicalValue() {
        for (x in wani) {
            x.waniPhysical = x.waniPhysicalMax
        }
    }

    // 各ワニの耐久力最大値をセット
    private fun setStrongValue() {
        for (x in wani) {
            x.waniToughness = x.waniToughnessMax
        }
    }

    // 全滅チェック
    private fun checkAllKilled(): Boolean {
        for (x in wani) {
            if (x.waniPhysical > 0) {
                return false
            }
        }
        return true
    }

    // 特訓判定
    private fun trainingJudge(): Triple<Int, Int, Int> {
        // どのワニを特訓するか
        val intWaniWhich: Int = (Math.random() * 4).toInt()
        // 体力、耐久力のどちらを鍛えるか
        val intGrowParam: Int = (Math.random() * 2).toInt()
        // 特訓した結果の値
        var intTrainingParam: Int = 0

        // 今後体力、耐久力以外のパラメーターを創設することを見越してifを使わない
        when (intGrowParam) {
            0 -> {
                intTrainingParam = ++wani[intWaniWhich].waniPhysicalMax
            }
            1 -> {
                intTrainingParam = ++wani[intWaniWhich].waniToughnessMax
            }
        }

        return Triple(intWaniWhich, intGrowParam, intTrainingParam)
    }

    // 特訓画面テキスト生成
    private fun generateTrainingResultText(
        intWaniWhich: Int,
        intGrowParam: Int,
        intTrainingParam: Int
    ): String {
        val builder: java.lang.StringBuilder = StringBuilder()

        builder.append("ワニ" + intWaniWhich.toString())
        builder.append("の")

        when (intGrowParam) {
            0 -> {
                builder.append("残機")
            }
            1 -> {
                builder.append("耐久力")
            }
        }
        builder.append("が")

        builder.append((intTrainingParam - 1))
        builder.append("から")
        builder.append(intTrainingParam)
        builder.append("に上がった！")

        return builder.toString()
    }
}
