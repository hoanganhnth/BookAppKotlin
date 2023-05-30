package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private val TAG = "PROFILE_EDIT_TAG"

    private var imgUri: Uri? = null

    private var uploadUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfor()
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun validateData() {
        if (binding.nameEt.text.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_LONG).show()
        }
        else {
            progressDialog.show()
            if (imgUri != null) {
                uploadToStorage()
            }
            else {
                uploadInfoToDb()
            }
        }
    }
    
    private fun uploadToStorage() {
        progressDialog.setMessage("Uploading Image...")
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "ProfileImages/" + firebaseAuth.uid

        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)
        ref.putFile(imgUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG,"uploadToStorage: IMG uploaded now getting url...")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                uploadUrl = "${uriTask.result}"
                uploadInfoToDb()
            }
            .addOnFailureListener {e->
                Log.d(TAG,"uploadToStorage: fail to  upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to  upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun uploadInfoToDb() {
        Log.d(TAG,"uploadInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading profile")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = binding.nameEt.text.toString()
        if (imgUri != null) {
            hashMap["profileImage"] = uploadUrl
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "uploadInfoToDb: Uploaded to db")
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_LONG).show()
                imgUri = null
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showImageAttachMenu() {
        Log.d(TAG,"Starting img pick intent")
        val intent= Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        imgActivityResultLauncher.launch(intent)
    }

    val imgActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {r ->
        if (r.resultCode == RESULT_OK) {
            Log.d(TAG,"IMG Picked")
            imgUri = r.data?.data!!
            Toast.makeText(this, "Picked img", Toast.LENGTH_LONG).show()
            showImg()
        }
        else {
            Log.d(TAG,"IMG Pick cancelled")
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImg() {
        try {
            Glide.with(this)
                .load(imgUri)
                .placeholder(R.drawable.ic_person_gray)
                .into(binding.profileIv)
        } catch (e: Exception) {

        }
    }

    private fun loadUserInfor() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value
                    val profileImg = snapshot.child("profileImage").value
                    binding.nameEt.setText(name.toString())
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImg)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.d(TAG,"Failed to load profile img due to ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}