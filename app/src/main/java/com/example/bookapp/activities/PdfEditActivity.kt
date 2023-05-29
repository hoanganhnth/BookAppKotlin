package com.example.bookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookapp.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPdfEditBinding

    private lateinit var progressDialog: ProgressDialog

    private var bookId = ""

    private lateinit var categoryTitleArrayList: ArrayList<String>
    private lateinit var categoryIdArrayList:ArrayList<String>

    private var selectedCategoryTitle = ""
    private var selectedCategoryId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        bookId = intent.getStringExtra("bookId").toString()

        Log.d("GETDATA","Book Id=$bookId")
        loadCategories()
        loadBookInfor()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }


    }

    private fun loadBookInfor() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    binding.desEt.setText(snapshot.child("description").value.toString())
                    binding.titleEt.setText(snapshot.child("title").value.toString())
                    val refCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                binding.categoryTv.text = snapshot.child("category").value.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var title=""
    private var descrition = ""
    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        descrition = binding.desEt.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
        }
        else if (descrition.isEmpty()) {
            Toast.makeText(this, "Please enter descreption", Toast.LENGTH_LONG).show()
        }
        else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Please pick category", Toast.LENGTH_LONG).show()
        }
        else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        progressDialog.setMessage("Updating book info")
        progressDialog.show()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["title"] = title
        hashMap["description"]  = descrition
        hashMap["categoryId"] = selectedCategoryId

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Updating book info", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadCategories() {
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()
                for (ds in snapshot.children) {
                    categoryTitleArrayList.add(ds.child("category").value.toString())
                    categoryIdArrayList.add(ds.child("id").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun categoryPickDialog() {
        val categoryArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoryArray[i] = categoryTitleArrayList[i]
        }
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoryArray){dialog,which->
                binding.categoryTv.text = categoryTitleArrayList[which]
                selectedCategoryTitle = categoryTitleArrayList[which]
                selectedCategoryId = categoryIdArrayList[which]
            }
            .show()
    }
}