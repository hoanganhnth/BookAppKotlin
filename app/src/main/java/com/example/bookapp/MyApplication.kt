package com.example.bookapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class MyApplication: Application() {

    companion object{
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy",cal).toString()
        }
        fun loadPdfSize(pdfUrl:String, pdfTitle:String,sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            Log.d(TAG,"Load size pdf $pdfTitle")
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    val byte = storageMetaData.sizeBytes.toDouble()
                    val kb = byte/1024
                    val mb = kb /1024
                    if (mb > 1) {
                        sizeTv.text = String.format("%.2f", mb) + "MB"
                    }
                    else if (kb > 1) {
                        sizeTv.text = String.format("%.2f", kb) + "KB"
                    }
                    else {
                        sizeTv.text = String.format("%.2f", byte) + "bytes"
                    }
                }
                .addOnFailureListener {e->
                    Log.d(TAG,"Error due to ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl:String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar:ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNNAIL_TAG"
            Log.d(TAG, "Load thumbnail $pdfTitle")
            pdfView.recycle()
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constains.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes ->
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "Error due to ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "Error due to ${t.message}")
                        }
                        .onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE
                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener {e->
                    Log.d(TAG,"Error due to ${e.message}")
                }
        }

        fun loadCategory(categoryId:String,categoryTv: TextView){
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = snapshot.child("category").value
                        categoryTv.text = category.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        fun deleteBook(context:Context, bookUrl:String,bookTitle: String, bookId: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storage = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storage.delete()
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted",Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e->
                            progressDialog.dismiss()
                            Toast.makeText(context, "Failed to delete due to ${e.message}",Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed to delete due to ${e.message}",Toast.LENGTH_LONG).show()
                }
        }

        fun incrementBookViewCount(bookId:String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewsCount = snapshot.child("viewsCount").value as Long
                        viewsCount = viewsCount + 1
                        snapshot.ref.child("viewsCount").setValue(viewsCount)
//                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
//                        val hashMap:HashMap<String,Any> = HashMap()
//                        hashMap["viewsCount"] = viewsCount
//                        dbRef.child(bookId).updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        fun incrementDownloadCount(bookId:String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var count = snapshot.child("downloadsCount").value as Long
                        count += 1
                        snapshot.ref.child("downloadsCount").setValue(count)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}