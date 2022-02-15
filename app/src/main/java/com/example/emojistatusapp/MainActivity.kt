package com.example.emojistatusapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

data class users(
    val displayName: String = "",
    val status: String = ""
)
class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        val query = db.collection("users")
        val options = FirestoreRecyclerOptions.Builder<users>().setQuery(query, users::class.java).setLifecycleOwner(this).build()
        val adapter = object: FirestoreRecyclerAdapter<users , UsersViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
                val view = LayoutInflater.from(this@MainActivity).inflate(android.R.layout.simple_expandable_list_item_2,parent,false)
                return UsersViewHolder(view)
            }

            override fun onBindViewHolder(holder: UsersViewHolder, position: Int, model: users) {
                val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
                val tvEmoji: TextView = holder.itemView.findViewById(android.R.id.text2)

                tvName.text = model.displayName
                tvEmoji.text = model.status
            }
        }
        rv_users.adapter = adapter
        rv_users.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.miLogout){
            auth.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }
        else if(item.itemId == R.id.miEdit){
            showAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class EmojiFilter : InputFilter {
        override fun filter(
            source: CharSequence?,
            p1: Int,
            p2: Int,
            p3: Spanned?,
            p4: Int,
            p5: Int
        ): CharSequence {
            if(source == null || source.isBlank()){
                return ""
            }
            val VALID_CHAR_TYPES = listOf(
                Character.NON_SPACING_MARK, // 6
                Character.DECIMAL_DIGIT_NUMBER, // 9
                Character.LETTER_NUMBER, // 10
                Character.OTHER_NUMBER, // 11
                Character.SPACE_SEPARATOR, // 12
                Character.FORMAT, // 16
                Character.SURROGATE, // 19
                Character.DASH_PUNCTUATION, // 20
                Character.START_PUNCTUATION, // 21
                Character.END_PUNCTUATION, // 22
                Character.CONNECTOR_PUNCTUATION, // 23
                Character.OTHER_PUNCTUATION, // 24
                Character.MATH_SYMBOL, // 25
                Character.CURRENCY_SYMBOL, //26
                Character.MODIFIER_SYMBOL, // 27
                Character.OTHER_SYMBOL // 28
            ).map { it.toInt() }.toSet()

            for(inputChar in source) {
                val type = Character.getType(inputChar)
                if(!VALID_CHAR_TYPES.contains(type)) {
                    Toast.makeText(this@MainActivity, "Please enter Emoji only",Toast.LENGTH_LONG).show()
                    return ""
                }
            }
            return source
            }
        }



    private fun showAlertDialog() {
        val editText = EditText(this)
        val emojiFilter = EmojiFilter()
        val lengthFilter = InputFilter.LengthFilter(9)
        editText.filters = arrayOf(lengthFilter,emojiFilter)
        val dialog = AlertDialog.Builder(this)
            .setTitle("update your emoji")
            .setView(editText)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("OK",null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val emojiEntered = editText.text.toString()
            if(emojiEntered.isBlank()){
                Toast.makeText(this, "Please enter something", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val currentuser = auth.currentUser
            if(currentuser == null){
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            db.collection("users").document(currentuser.uid)
                .update("status",emojiEntered)
            dialog.dismiss()
        }
    }
}

