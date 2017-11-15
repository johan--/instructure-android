/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.androidfoosball.activities

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.FoosRankLeaderboardAdapter
import com.instructure.androidfoosball.adapters.LeaderboardAdapter
import com.instructure.androidfoosball.adapters.TeamLeaderboardAdapter
import com.instructure.androidfoosball.adapters.TimeWasterLeaderboardAdapter
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.*
import kotlinx.android.synthetic.tablet.activity_leaderboard.*
import org.jetbrains.anko.sdk21.listeners.onClick


class LeaderboardActivity : AppCompatActivity() {

    companion object {
        const val MIN_GAMES_FOR_RANKING = 9
        const val MIN_GAMES_FOR_TEAM_RANKING = 5
    }

    private val accentColor by lazy { this.resources.getColor(R.color.colorAccent) }
    private val grayColor by lazy { this.resources.getColor(R.color.lightGray) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        leaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(MIN_GAMES_FOR_RANKING)

        teamLeaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(LeaderboardActivity.MIN_GAMES_FOR_TEAM_RANKING)

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()

        loadIndividualLeaderboard()

        setupListeners()
    }

    private fun loadIndividualLeaderboard() {

        unselectAll()
        selectIndividual()

        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedUsers = App.realm.where(User::class.java).equalTo("guest", false).findAll().sortByWinRatio(MIN_GAMES_FOR_RANKING)
                recyclerView.adapter = LeaderboardAdapter(this@LeaderboardActivity, sortedUsers)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun selectIndividual() {
        leaderboardSelected.setBackgroundColor(accentColor)
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)

    }

    private fun unselectIndividual() {
        leaderboardSelected.setBackgroundColor(grayColor)
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectTeam() {
        teamLeaderboardSelected.setBackgroundColor(accentColor)
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectTeam() {
        teamLeaderboardSelected.setBackgroundColor(grayColor)
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectFoosRank() {
        foosRankSelected.setBackgroundColor(accentColor)
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectFoosRank() {
        foosRankSelected.setBackgroundColor(grayColor)
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectTimeWaster() {
        timeWasterSelected.setBackgroundColor(accentColor)
        timeWasterText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        timeWasterSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectTimeWaster() {
        timeWasterSelected.setBackgroundColor(grayColor)
        timeWasterText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        timeWasterSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun unselectAll() {
        unselectFoosRank()
        unselectIndividual()
        unselectTeam()
        unselectTimeWaster()
    }

    private fun loadTeamLeaderboard() {

        unselectAll()
        selectTeam()

        FirebaseDatabase.getInstance().reference.child("customTeams").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val teams = dataSnapshot.children.mapNotNull { it.getValue(CustomTeam::class.java) }

                FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users = dataSnapshot.children.mapNotNull { it.getValue(User::class.java) }.associateBy { it.id }
                        recyclerView.adapter = TeamLeaderboardAdapter(this@LeaderboardActivity, teams.sortCustomTeamByWinRatio(MIN_GAMES_FOR_TEAM_RANKING), users)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })


            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun loadFoosRankLeaderboard() {
        unselectAll()
        selectFoosRank()

        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedUsers = dataSnapshot.children.mapNotNull { it.getValue(User::class.java) }.filter { !it.guest }.sortByFoosRanking()
                recyclerView.adapter = FoosRankLeaderboardAdapter(this@LeaderboardActivity, sortedUsers) {
                    startActivity(EloDialogActivity.createIntent(this@LeaderboardActivity, it.foosRankMap))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    private fun loadTimeWasterLeaderboard() {
        unselectAll()
        selectTimeWaster()

        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedUsers = dataSnapshot.children.mapNotNull { it.getValue(User::class.java) }.filter { !it.guest }.sortedByDescending { it.wins + it.losses }
                recyclerView.adapter = TimeWasterLeaderboardAdapter(this@LeaderboardActivity, sortedUsers)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    private fun setupListeners() {
        leaderboardWrapper.setOnClickListener { loadIndividualLeaderboard() }
        teamLeaderboardWrapper.setOnClickListener { loadTeamLeaderboard() }
        foosRankWrapper.onClick { loadFoosRankLeaderboard() }
        timeWasterWrapper.onClick { loadTimeWasterLeaderboard() }
        hiddenButton.onDoubleTap { timeWasterWrapper.setVisible().performClick() }
    }
}
