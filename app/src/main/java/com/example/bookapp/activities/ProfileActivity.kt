package com.example.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.adapters.PdfFavoriteAdapter
import com.example.bookapp.databinding.ActivityProfileBinding
import com.example.bookapp.models.PdfModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var pdfFavoriteList: ArrayList<PdfModel>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        loadInforUser()
        loadFavoriteList()

        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        binding.statusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified) {
                Toast.makeText(this, "Already verified...!", Toast.LENGTH_SHORT).show()
            }
            else {
                emailVerificationDialog()
            }

        }
    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send email verification instructions to your email ${firebaseUser.email}")
            .setPositiveButton("SEND") {d,e ->
                d.dismiss()
                sendEmailVerification()
            }
            .setNegativeButton("CANCEL") {d,e->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        progressDialog.setMessage("Sending email verification instructions to email ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Instructions sent! Check your email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun loadFavoriteList() {
        pdfFavoriteList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfFavoriteList.clear()
                    for (ds in snapshot.children) {
                        val bookId = ds.child("bookId").value.toString()
                        val pdfModel = PdfModel()
                        pdfModel.id = bookId
                        pdfFavoriteList.add(pdfModel)
                    }

                    binding.favoriteTv.text = pdfFavoriteList.size.toString()
                    binding.favoriteRv.adapter = PdfFavoriteAdapter(pdfFavoriteList)
                    if (pdfFavoriteList.size == 0) {
                        binding.favoriteLabelTv.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadInforUser() {
        if (firebaseUser.isEmailVerified) {
            binding.statusTv.text = "Verified"
        }
        else {
            binding.statusTv.text = "Not Verified"
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value
                    val email = snapshot.child("email").value
                    val timestamp = snapshot.child("timestamp").value
                    val userType = snapshot.child("userType").value
                    val profileImg = snapshot.child("profileImage").value
                    binding.nameTv.text = name.toString()
                    binding.gmailTv.text = email.toString()
                    binding.accountTv.text = userType.toString()

                    val member = MyApplication.formatTimeStamp(timestamp as Long)
                    binding.memberTv.text = member
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImg)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.d("PROFILE_TAG","Failed to load profile img due to ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}