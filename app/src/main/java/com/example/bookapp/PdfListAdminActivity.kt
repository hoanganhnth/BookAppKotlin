package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private lateinit var pdfAdapter:PdfAdminAdapter

    private lateinit var pdfArrayList: ArrayList<PdfModel>
    private var category = ""
    private var categoryId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        category = intent.getStringExtra("category")!!
        categoryId = intent.getStringExtra("categoryId")!!

        binding.subTitleTv.text = category

        loadPdfList()
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    pdfAdapter.filter.filter(s)
                } catch (e:Exception) {
                    Log.d("FILTER","on text changed:${e.message}")
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()
        binding.booksRv.layoutManager = LinearLayoutManager(this)

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(PdfModel::class.java)
                        pdfArrayList.add(model!!)
                    }
                    pdfAdapter = PdfAdminAdapter(pdfArrayList)
                    binding.booksRv.adapter = pdfAdapter
                    Log.d("GETDATABASE", "success")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}