package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        name = binding.nameEt.text.toString()
        email = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()
        val cpassword = binding.passwordCfEt.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_LONG).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern...", Toast.LENGTH_LONG).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show()
        }
        else if (cpassword.isEmpty()) {
            Toast.makeText(this, "Confirm password", Toast.LENGTH_LONG).show()
        }
        else if (password != cpassword) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_LONG).show()
        }
        else {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        progressDialog.setMessage("Creating Account")
        progressDialog.show()


        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setTitle("Saving user info")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid.toString()

        val hashMap:HashMap<String, Any> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] =  email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Account created ...", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving account due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}