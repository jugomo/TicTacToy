package com.site11.jugomo.tictactoy

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
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

    //
    //
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btReset.isEnabled = false
    }

    fun resetAction(view: View) {

        binding.bt1.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt2.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt3.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt4.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt5.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt6.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt7.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt8.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.bt9.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        binding.bt1.text = ""
        binding.bt2.text = ""
        binding.bt3.text = ""
        binding.bt4.text = ""
        binding.bt5.text = ""
        binding.bt6.text = ""
        binding.bt7.text = ""
        binding.bt8.text = ""
        binding.bt9.text = ""


        binding.bt1.isEnabled = true
        binding.bt2.isEnabled = true
        binding.bt3.isEnabled = true
        binding.bt4.isEnabled = true
        binding.bt5.isEnabled = true
        binding.bt6.isEnabled = true
        binding.bt7.isEnabled = true
        binding.bt8.isEnabled = true
        binding.bt9.isEnabled = true

        binding.btReset.isEnabled = false
        finished = false
        player1.clear()
        player2.clear()

        binding.tvMessage.text = "Welcome!"
        binding.tvMessage.setTextColor(Color.BLACK)
    }

    fun btClick(view: View) {
        if (!finished) {
            val btSel = view as Button

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

            //Toast.makeText(this, "ID: " + cellId, Toast.LENGTH_LONG).show()

            playGame(cellId, btSel)
        }
    }

    private fun playGame(cellID: Int, sel: Button) {
        if (active == 1) {
            sel.text = "X"
            sel.setBackgroundColor(Color.GREEN)
            player1.add(cellID)
            active = 2

            autoPlay()
        } else {
            sel.text = "O"
            sel.setBackgroundColor(Color.BLUE)
            player2.add(cellID)
            active = 1

        }

        sel.isEnabled = false
        checkWinner()
    }

    private fun checkWinner() {
        var winner = -1

        // row 1
        if (player1.contains(1) && player1.contains(2) && player1.contains(3)) {
            winner = 1
        }
        if (player2.contains(1) && player2.contains(2) && player2.contains(3)) {
            winner = 2
        }

        // row 2
        if (player1.contains(4) && player1.contains(5) && player1.contains(6)) {
            winner = 1
        }
        if (player2.contains(4) && player2.contains(5) && player2.contains(6)) {
            winner = 2
        }

        // row 3
        if (player1.contains(7) && player1.contains(8) && player1.contains(9)) {
            winner = 1
        }
        if (player2.contains(7) && player2.contains(8) && player2.contains(9)) {
            winner = 2
        }


        // col 1
        if (player1.contains(1) && player1.contains(4) && player1.contains(7)) {
            winner = 1
        }
        if (player2.contains(1) && player2.contains(4) && player2.contains(7)) {
            winner = 2
        }

        // col 2
        if (player1.contains(2) && player1.contains(5) && player1.contains(8)) {
            winner = 1
        }
        if (player2.contains(2) && player2.contains(5) && player2.contains(8)) {
            winner = 2
        }

        // col 3
        if (player1.contains(3) && player1.contains(6) && player1.contains(9)) {
            winner = 1
        }
        if (player2.contains(3) && player2.contains(6) && player2.contains(9)) {
            winner = 2
        }

        // there is a winner
        if (winner != -1) {
            if (winner == 1) {
                binding.tvMessage.text = "YOU WON!"
                binding.tvMessage.setTextColor(Color.GREEN)
            } else {
                binding.tvMessage.text = "PLAYER 2 WON!"
                binding.tvMessage.setTextColor(Color.RED)
            }

            binding.btReset.isEnabled = true
            finished = true
        } else {
            if (emptyCells().size == 0) {
                gameOver()
            }
        }
    }

    private fun autoPlay() {
        val num = emptyCells()

        if (num.size != 0) {
            val r = Random()
            val randIndex = r.nextInt(num.size - 0) + 0
            val cellID = num[randIndex]

            val btSelect: Button = when (cellID) {
                1 -> binding.bt1
                2 -> binding.bt2
                3 -> binding.bt3
                4 -> binding.bt4
                5 -> binding.bt5
                6 -> binding.bt6
                7 -> binding.bt7
                8 -> binding.bt8
                9 -> binding.bt9
                else -> {
                    binding.bt1
                }
            }

            playGame(cellID, btSelect)
        } else {
            gameOver()
        }
    }

    private fun emptyCells(): ArrayList<Int> {
        val emptyCells = ArrayList<Int>()

        for (cellID in 1..9) {
            if (!(player1.contains(cellID) || player2.contains(cellID))) {
                emptyCells.add(cellID)
            }
        }

        return emptyCells
    }

    private fun gameOver() {
        binding.tvMessage.text = "GAME OUT! - NOBODY WON"
        binding.tvMessage.setTextColor(Color.RED)
        binding.btReset.isEnabled = true
        finished = true
    }
}
