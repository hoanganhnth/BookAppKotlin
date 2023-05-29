package com.example.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapp.models.CategoryModel
import com.example.bookapp.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList:ArrayList<CategoryModel>

    private lateinit var firebaseAuth: FirebaseAuth

    private val pdf: Int = 0
    private var pdfUri: Uri? = null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        firebaseAuth = FirebaseAuth.getInstance()

        loadPdfCategory()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTil.setOnClickListener {
            categoryPickDialog()
        }

        binding.attachBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var des = ""
    private var category = ""
    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        des = binding.desEt.text.toString().trim()
        category = binding.categoryTv.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
        }
        else if (des.isEmpty()) {
            Toast.makeText(this, "Please enter descreption", Toast.LENGTH_LONG).show()
        }
        else if (category.isEmpty()) {
            Toast.makeText(this, "Please pick category", Toast.LENGTH_LONG).show()
        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Please pick pdf", Toast.LENGTH_LONG).show()
        }
        else {
            uploadStorage()
        }
    }

    private fun uploadStorage() {
        progressDialog.setMessage("Uploadingc PDF...")
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Book/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG,"uploadPdfToStorage: PDF uploaded now getting url...")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"
                uploadPdfInfoToDb(uploadPdfUrl,timestamp)

            }
            .addOnFailureListener {e->
                Log.d(TAG,"uploadPdfToStorage: fail to  upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to  upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timestamp: Long) {
        Log.d(TAG,"uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info")

        val uid = firebaseAuth.uid
        val hashMap:HashMap<String,Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"]="$timestamp"
        hashMap["title"] = title
        hashMap["description"] = des
        hashMap["categoryId"] = selectedCategoryId
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "uploadPdfToStorage: Uploaded to db")
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_LONG).show()
                pdfUri = null
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()

            }
    }

    private fun pdfPickIntent() {
        Log.d(TAG,"Starting pdf pick intent")
        val intent= Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLaucher.launch(intent)

    }
    val pdfActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { r ->
        if (r.resultCode == RESULT_OK) {
            Log.d(TAG,"PDF Picked")
            pdfUri = r.data?.data!!
            Toast.makeText(this, "Picked 1 pdf", Toast.LENGTH_LONG).show()
        }
        else {
            Log.d(TAG,"PDF Pick cancelled")
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        }

    }
    private fun loadPdfCategory() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (cat in snapshot.children) {
                    var model = cat.getValue(CategoryModel::class.java)
                    categoryArrayList.add(model!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private var selectedCategoryId = ""
    private fun categoryPickDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){dialog,which->
                binding.categoryTv.text = categoriesArray[which]
                selectedCategoryId = categoryArrayList[which].id
            }
            .show()
    }
}