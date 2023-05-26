package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.bookapp.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewBinding
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    private var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId").toString()
        loadBookDetail()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetail() {
        Log.d(TAG,"loadBookDetail:Get Pdf from db")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookUrl = snapshot.child("url").value.toString()
                    Log.d(TAG, "onDataChange: PDF_URL: $bookUrl")
                    loadBookFromUrl(bookUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadBookFromUrl(url: String) {
        Log.d(TAG,"loadBookFromUrl: Get pdf from firebase storage using URL")
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        reference.getBytes(Constains.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                    Log.d(TAG,"loadBookFromUrl: pdf got from url due to ")
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page, pageCount->
                        val currentPage = page + 1
                        binding.toolbarSubTitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError{t->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener {e->
                Log.d(TAG,"loadBookFromUrl: Failed to get pdf due to ${e.message}")
                Toast.makeText(this, "Failed to get pdf due to ${e.message}",Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
    }
}