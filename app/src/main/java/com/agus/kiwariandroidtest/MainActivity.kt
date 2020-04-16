package com.agus.kiwariandroidtest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val DEFAULT_MSG_LENGTH_LIMIT = 1000
    private val DATE_FORMAT_1 = "dd-MM-yyyy (hh:mm:ss)"
    private val RC_SIGN_IN = 1
    private var mUsername = "anonymous"
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMessagesDatabaseReference: DatabaseReference
    private var mChildEventListener: ChildEventListener? = null
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var mMessageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()

        mMessagesDatabaseReference = mFirebaseDatabase.reference.child("messages")

        // Initialize message ListView and its adapter
        initRecycler()

        progressBar.visibility = ProgressBar.INVISIBLE
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                sendButton.isEnabled = charSequence.toString().trim().isNotEmpty()
            }

        })
        messageEditText.filters = arrayOf<InputFilter>(LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))

        sendButton.setOnClickListener {
            val friendlyMessage = FriendlyMessage(messageEditText.text.toString(), mUsername, getCurrentTime())
            mMessagesDatabaseReference.push().setValue(friendlyMessage)

            messageEditText.setText("")
        }

        mAuthStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                onSignedInInitialize(user.displayName!!)
                attachDatabaseReadListener()
            } else {
                onSignedOutCleanup()
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                            listOf(
                                EmailBuilder().build()
                            )
                        )
                        .build(),
                    RC_SIGN_IN
                )
            }
        }
    }

    private fun initRecycler(){
        mMessageAdapter = MessageAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
        messageListView.adapter = mMessageAdapter
        messageListView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if(bottom< oldBottom){
                messageListView.postDelayed(Runnable { messageListView.scrollToPosition(mMessageAdapter.itemCount-1) }, 100)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener!!)
    }

    override fun onPause() {
        super.onPause()
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener!!)
        }
        mMessageAdapter.removeAll()
        detachDatabaseReadListener()
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_1)
        val today = Calendar.getInstance().time
        return dateFormat.format(today)
    }

    private fun onSignedInInitialize(username: String) {
        setToolbarTitle(username)
        mUsername = username
        attachDatabaseReadListener()
    }

    private fun onSignedOutCleanup() {
        mUsername = "anonymous"
        mMessageAdapter.removeAll()
        detachDatabaseReadListener()
    }

    private fun attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val friendlyMessage = dataSnapshot.getValue(
                        FriendlyMessage::class.java
                    )
                    mMessageAdapter.add(friendlyMessage!!)
                    messageListView.scrollToPosition(mMessageAdapter.itemCount-1)
                }

                override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    s: String?
                ) {
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener as ChildEventListener)
        }
    }

    private fun detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener!!)
            mChildEventListener = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Signed in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setToolbarTitle(username: String){
        var text: String = if(username == "Jarjit Singh"){
            "Ismail bin Mail"
        } else{
            "Jarjit Singh"
        }
        supportActionBar!!.title = text
    }
}
