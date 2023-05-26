package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Output
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.bookapp.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.io.OutputStream

class PdfDetailActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "BOOK DETAILS_TAG"
    }
    private lateinit var binding: ActivityPdfDetailBinding
    private lateinit var progressDialog: ProgressDialog

    private var bookId = ""
    private var bookUrl = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId").toString()
        progressDialog = ProgressDialog(this)

        MyApplication.incrementBookViewCount(bookId)
        loadInfoPdf()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBtn.setOnClickListener {
            val intent = Intent(this,PdfViewActivity::class.java)
            intent.putExtra("bookId",bookId)
            startActivity(intent)
        }

        binding.downloadsBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            }
            else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is not granted")
                requestStoragePermissionLaucher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    val requestStoragePermissionLaucher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted:Boolean->
        if (isGranted) {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
            downloadBook()
        }
        else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadBook() {
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()
        Log.d(TAG, bookUrl)
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constains.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"dowloadBook: Book downloaded...")
                saveToDownLoadFolder(bytes)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                Toast.makeText(this, "Failed to download book due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveToDownLoadFolder(bytes: ByteArray) {
        Log.d(TAG, "saveToDownLoadFolder: Saving download book")
        val nameWithExtention = "$title.pdf"
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()
            val filePath = downloadsFolder.path + "/" + nameWithExtention
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()
            progressDialog.dismiss()
            Toast.makeText(this, "Saved to download folder", Toast.LENGTH_LONG).show()
            MyApplication.incrementDownloadCount(bookId)
        } catch (e:Exception) {
            Log.d(TAG, "saveToDownLoadFolder: ${e.message}")
            Toast.makeText(this, "Failed to save    due to ${e.message}", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        }
    }

    private fun loadInfoPdf() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val categoryId = snapshot.child("categoryId").value.toString()
                    bookUrl = snapshot.child("url").value.toString()
                    val date = MyApplication.formatTimeStamp(snapshot.child("timestamp").value as Long)
                    val size = ""
                    title = snapshot.child("title").value.toString()

                    binding.titleTv.text = title
                    binding.desTv.text = snapshot.child("description").value.toString()
                    binding.dateTv.text = date
                    binding.sizeTv.text = size
                    binding.viewsTv.text = snapshot.child("viewsCount").value.toString()
                    binding.downloadsTv.text = snapshot.child("downloadsCount").value.toString()

                    MyApplication.loadCategory(categoryId,binding.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage(bookUrl,title,binding.pdfView,binding.progressBar,binding.pagesTv)
                    MyApplication.loadPdfSize(bookUrl,title,binding.sizeTv)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}