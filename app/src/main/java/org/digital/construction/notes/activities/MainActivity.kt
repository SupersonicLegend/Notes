package org.digital.construction.notes.activities

import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import org.digital.construction.notes.R
import org.digital.construction.notes.fragments.*

private const val TAG = "MainActivity"
private const val REQUEST_NOTE_ID = "note_id_request"

const val ACTION_OPEN_SETTINGS = "org.digital.construction.notes.open_settings"
const val ACTION_OPEN_ABOUT = "org.digital.construction.notes.open_about"

class MainActivity : AppCompatActivity(), FragmentResultListener {

    private lateinit var sharedPreferences: SharedPreferences

    private val openSettings: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(TAG, "Received request to open settings")

                openFragmentWithFadeAnim(SettingsFragment())
            }
        }
    }
    private val openAbout: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(TAG, "Received request to open about")

                openFragmentWithFadeAnim(AboutFragment())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        supportFragmentManager.setFragmentResultListener(
            REQUEST_NOTE_ID,
            this,
            this
        )

        registerReceiver(openSettings, IntentFilter(ACTION_OPEN_SETTINGS))
        registerReceiver(openAbout, IntentFilter(ACTION_OPEN_ABOUT))

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)

        if (currentFragment == null) {
            val notesListFragment = NotesListFragment.newInstance(REQUEST_NOTE_ID)
            supportFragmentManager.commit {
                add(R.id.fragment_container_view, notesListFragment)
            }
        }

        sharedPreferences.getBoolean(INTRODUCTION_SEEN_KEY, false).let { introductionSeen ->
            if (!introductionSeen) {
                startActivity(Intent(this, MainIntroActivity::class.java))
                sharedPreferences.edit {
                    putBoolean(INTRODUCTION_SEEN_KEY, true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(openSettings)
        unregisterReceiver(openAbout)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            REQUEST_NOTE_ID -> {
                val noteId = result.getLong(REQUEST_NOTE_ID)
                Log.i(TAG, "Opening note (id: $noteId)")

                val noteFragment = NoteFragment.newInstance(noteId)
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    replace(R.id.fragment_container_view, noteFragment)
                    addToBackStack(null)
                }
            }
        }
    }

    private fun openFragmentWithFadeAnim(fragment: Fragment) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            replace(R.id.fragment_container_view, fragment)
            addToBackStack(null)
        }
    }
}