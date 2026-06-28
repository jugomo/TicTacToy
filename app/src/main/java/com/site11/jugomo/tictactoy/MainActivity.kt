package com.site11.jugomo.tictactoy

import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.site11.jugomo.tictactoy.databinding.ActivityMainBinding
import java.util.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player1 = ArrayList<Int>()
    private var player2 = ArrayList<Int>()
    private var active = 1
    private var finished = false
    private var difficulty = 0 // 0=Fácil, 1=Medio, 2=Difícil

    private var gameMode = 0 // 0 = vs IA, 1 = P1 vs P2
    private var thinking = false
    private var thinkingAnimator: ObjectAnimator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingAutoPlay: Runnable? = null
    private val winAnimators = mutableListOf<ObjectAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                difficulty = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        updateModeButton()
        updateResetButton()
    }

    private fun updateResetButton() {
        val hasMove = player1.isNotEmpty() || player2.isNotEmpty()
        binding.btReset.isEnabled = hasMove
        binding.btReset.alpha = if (hasMove) 1f else 0.35f
    }

    fun modeToggleClick(view: View) {
        gameMode = if (gameMode == 0) 1 else 0
        updateModeButton()
        resetAction(binding.btReset)
    }

    private fun updateModeButton() {
        binding.btMode.text = getString(if (gameMode == 0) R.string.mode_vs_ia else R.string.mode_vs_p2)
        binding.difficultyLayout.visibility = if (gameMode == 0) View.VISIBLE else View.GONE
    }

    fun resetAction(view: View) {
        pendingAutoPlay?.let { handler.removeCallbacks(it) }
        pendingAutoPlay = null
        hideThinking()

        val cells = listOf(
            binding.bt1, binding.bt2, binding.bt3,
            binding.bt4, binding.bt5, binding.bt6,
            binding.bt7, binding.bt8, binding.bt9
        )

        val playedCells = cells.filterIndexed { i, _ -> (i + 1) in player1 || (i + 1) in player2 }

        if (playedCells.isEmpty()) {
            doReset(cells)
            return
        }

        view.isEnabled = false

        val dist = resources.displayMetrics.run { maxOf(widthPixels, heightPixels) }.toFloat() * 1.5f
        // Direction each cell flies: row-major order, pointing outward from center
        val dirX = floatArrayOf(-1f,  0f, 1f, -1f, 0f, 1f, -1f,  0f, 1f)
        val dirY = floatArrayOf(-1f, -1f, -1f, 0f, 0f, 0f,  1f,  1f, 1f)
        val rng = Random()
        var completed = 0

        cells.forEachIndexed { i, cell ->
            if ((i + 1) !in player1 && (i + 1) !in player2) return@forEachIndexed

            val dx = if (i == 4) (if (rng.nextBoolean()) -1f else 1f) else dirX[i]
            val dy = if (i == 4) (if (rng.nextBoolean()) -1f else 1f) else dirY[i]

            cell.animate()
                .translationX(dx * dist)
                .translationY(dy * dist)
                .alpha(0f)
                .scaleX(0.2f)
                .scaleY(0.2f)
                .rotation(dx * 540f)
                .setDuration(500)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    completed++
                    if (completed == playedCells.size) {
                        doReset(cells)
                        view.isEnabled = true
                    }
                }
                .start()
        }
    }

    private fun doReset(cells: List<ImageButton>) {
        winAnimators.forEach { it.cancel() }
        winAnimators.clear()
        cells.forEach { cell ->
            cell.setImageDrawable(null)
            (cell.parent as? FrameLayout)?.setBackgroundResource(R.drawable.cell_button_bg)
            cell.isEnabled = true
            cell.translationX = 0f
            cell.translationY = 0f
            cell.alpha = 1f
            cell.rotation = 0f
            cell.rotationY = 0f
            cell.scaleX = 1f
            cell.scaleY = 1f
        }
        active = 1
        finished = false
        player1.clear()
        player2.clear()
        binding.tvMessage.text = getString(R.string.msg_welcome)
        binding.tvMessage.setTextColor(Color.WHITE)
        updateTurnMessage()
        updateResetButton()
    }

    private fun updateTurnMessage() {
        if (gameMode != 1 || finished) return
        binding.tvMessage.text = getString(if (active == 1) R.string.msg_turn_p1 else R.string.msg_turn_p2)
        binding.tvMessage.setTextColor(Color.WHITE)
    }

    fun btClick(view: View) {
        if (!finished && !thinking) {
            val btSel = view as ImageButton
            var cellId = 0
            when (btSel.id) {
                R.id.bt1 -> cellId = 1
                R.id.bt2 -> cellId = 2
                R.id.bt3 -> cellId = 3
                R.id.bt4 -> cellId = 4
                R.id.bt5 -> cellId = 5
                R.id.bt6 -> cellId = 6
                R.id.bt7 -> cellId = 7
                R.id.bt8 -> cellId = 8
                R.id.bt9 -> cellId = 9
            }
            playGame(cellId, btSel)
        }
    }

    private fun playGame(cellID: Int, sel: ImageButton) {
        if (active == 1) {
            sel.setImageResource(R.drawable.x_icon)
            (sel.parent as FrameLayout).background = blackCellBg()
            animatePiece(sel)
            player1.add(cellID)
            updateResetButton()
            sel.isEnabled = false
            checkWinner()
            if (!finished) {
                active = 2
                updateTurnMessage()
                if (gameMode == 0) {
                    showThinking()
                    pendingAutoPlay = Runnable {
                        pendingAutoPlay = null
                        hideThinking()
                        autoPlay()
                    }
                    handler.postDelayed(pendingAutoPlay!!, 800)
                }
            }
        } else {
            sel.setImageResource(R.drawable.o_icon)
            (sel.parent as FrameLayout).background = blackCellBg()
            animatePiece(sel)
            player2.add(cellID)
            active = 1
            sel.isEnabled = false
            checkWinner()
            updateTurnMessage()
        }
    }

    private fun blackCellBg(): GradientDrawable {
        val density = resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.BLACK)
            cornerRadius = 10f * density
            setStroke((2f * density).toInt(), Color.argb(170, 255, 255, 255))
        }
    }

    private fun showThinking() {
        thinking = true
        binding.thinkingLayout.visibility = View.VISIBLE
        thinkingAnimator = ObjectAnimator.ofFloat(binding.tvHourglass, "rotationY", 0f, 360f).apply {
            duration = 900
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun hideThinking() {
        thinking = false
        thinkingAnimator?.cancel()
        thinkingAnimator = null
        binding.thinkingLayout.visibility = View.GONE
        binding.tvHourglass.rotationY = 0f
    }

    private fun animatePiece(cell: ImageButton) {
        cell.scaleX = 0f
        cell.scaleY = 0f
        cell.alpha = 0f
        cell.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    // --- AI ---

    private fun autoPlay() {
        val empty = emptyCells()
        if (empty.isEmpty()) { gameOver(); return }

        val cellID = when (difficulty) {
            2 -> bestMoveHard()
            1 -> bestMoveMedium(empty)
            else -> empty[Random().nextInt(empty.size)]
        }

        val btSelect: ImageButton = when (cellID) {
            1 -> binding.bt1
            2 -> binding.bt2
            3 -> binding.bt3
            4 -> binding.bt4
            5 -> binding.bt5
            6 -> binding.bt6
            7 -> binding.bt7
            8 -> binding.bt8
            else -> binding.bt9
        }
        playGame(cellID, btSelect)
    }

    // Gana si puede, bloquea si el humano va a ganar, si no aleatorio
    private fun bestMoveMedium(empty: ArrayList<Int>): Int {
        return findWinningMove(player2)
            ?: findWinningMove(player1)
            ?: empty[Random().nextInt(empty.size)]
    }

    // Devuelve la celda ganadora para `positions` si existe
    private fun findWinningMove(positions: ArrayList<Int>): Int? {
        return emptyCells().firstOrNull { cell ->
            checkWinList(ArrayList(positions).also { it.add(cell) })
        }
    }

    // Minimax: devuelve la mejor celda para la IA (player2)
    private fun bestMoveHard(): Int {
        var bestVal = Int.MIN_VALUE
        var bestMove = emptyCells()[0]
        for (move in emptyCells()) {
            val score = minimax(player1, ArrayList(player2).also { it.add(move) }, false)
            if (score > bestVal) {
                bestVal = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimax(p1: List<Int>, p2: List<Int>, isMaximizing: Boolean): Int {
        if (checkWinList(p2)) return 10
        if (checkWinList(p1)) return -10
        val available = (1..9).filter { !p1.contains(it) && !p2.contains(it) }
        if (available.isEmpty()) return 0

        return if (isMaximizing) {
            available.maxOf { minimax(p1, p2 + it, false) }
        } else {
            available.minOf { minimax(p1 + it, p2, true) }
        }
    }

    private fun animateWin() {
        val allCells = listOf(
            binding.bt1, binding.bt2, binding.bt3,
            binding.bt4, binding.bt5, binding.bt6,
            binding.bt7, binding.bt8, binding.bt9
        )
        player1.forEach { id ->
            ObjectAnimator.ofFloat(allCells[id - 1], "rotation", 0f, 360f).apply {
                duration = 700
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }.also { winAnimators.add(it) }
        }
        player2.forEach { id ->
            ObjectAnimator.ofFloat(allCells[id - 1], "rotationY", 0f, 360f).apply {
                duration = 700
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }.also { winAnimators.add(it) }
        }
    }

    // --- Win check ---

    private val winCombos = listOf(
        listOf(1,2,3), listOf(4,5,6), listOf(7,8,9),
        listOf(1,4,7), listOf(2,5,8), listOf(3,6,9),
        listOf(1,5,9), listOf(3,5,7)
    )

    private fun checkWinList(positions: List<Int>) =
        winCombos.any { positions.containsAll(it) }

    private fun checkWinner() {
        when {
            checkWinList(player1) -> {
                binding.tvMessage.text = getString(if (gameMode == 0) R.string.msg_you_won else R.string.msg_p1_won)
                binding.tvMessage.setTextColor(Color.GREEN)
                finished = true
                animateWin()
            }
            checkWinList(player2) -> {
                binding.tvMessage.text = getString(if (gameMode == 0) R.string.msg_ai_won else R.string.msg_p2_won)
                binding.tvMessage.setTextColor(Color.RED)
                finished = true
                animateWin()
            }
            emptyCells().isEmpty() -> gameOver()
        }
    }

    private fun emptyCells(): ArrayList<Int> {
        return ArrayList((1..9).filter { !player1.contains(it) && !player2.contains(it) })
    }

    private fun gameOver() {
        binding.tvMessage.text = getString(R.string.msg_draw)
        binding.tvMessage.setTextColor(Color.RED)
        finished = true
    }
}
