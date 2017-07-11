package com.site11.jugomo.tictactoy

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var active = 1

    //
    //
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun btClick(view: View) {
        val btSel = view as Button

        var cellId = 0
        when(btSel.id) {
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

    fun playGame(cellID: Int, sel: Button) {
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

    fun checkWinner() {
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

        if (winner != -1) {
            if (winner == 1) {
                Toast.makeText(this, "PLAYER 1 WON!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "PLAYER 2 WON!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun autoPlay() {
        var emptyCells = ArrayList<Int>()

        for (cellID in 1..9) {
            if (!(player1.contains(cellID) || player2.contains(cellID))) {
                emptyCells.add(cellID)
            }
        }

        val r = Random()
        val randIndex = r.nextInt(emptyCells.size - 0) + 0
        val cellID = emptyCells.get(randIndex)

        var btSelect: Button?
        when(cellID) {
            1 -> btSelect = bt1
            2 -> btSelect = bt2
            3 -> btSelect = bt3
            4 -> btSelect = bt4
            5 -> btSelect = bt5
            6 -> btSelect = bt6
            7 -> btSelect = bt7
            8 -> btSelect = bt8
            9 -> btSelect = bt9
            else -> {
                btSelect = bt1
            }
        }

        playGame(cellID, btSelect)
    }
}
