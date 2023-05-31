package com.example.bookapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.MyApplication
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.databinding.RowPdfFavoriteBinding
import com.example.bookapp.models.PdfModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfFavoriteAdapter(var pdfList: ArrayList<PdfModel>): RecyclerView.Adapter<PdfFavoriteAdapter.ViewHolder>(){
    class ViewHolder(var binding: RowPdfFavoriteBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = pdfList[position]
        val bookId = model.id
        val mcontext = holder.itemView.context

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val categoryId = snapshot.child("categoryId").value.toString()
                    val des = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    val timestamp = snapshot.child("timestamp").value
                    val pdfUrl = snapshot.child("url").value.toString()
                    val date = MyApplication.formatTimeStamp(timestamp as Long)
                    holder.binding.titleTv.text = title
                    holder.binding.subTitleTv.text = des
                    holder.binding.dateTv.text = date
                    MyApplication.loadCategory(categoryId, holder.binding.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage(
                        pdfUrl,
                        title,
                        holder.binding.pdfView,
                        holder.binding.progressBar,
                        null
                    )
                    MyApplication.loadPdfSize(pdfUrl, title, holder.binding.sizeTv)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        holder.itemView.setOnClickListener {
            val intent = Intent(mcontext, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            mcontext.startActivity(intent)
        }
        holder.binding.favoriteBtn.setOnClickListener {
            //delete favorite books
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            val firebaseAuth = FirebaseAuth.getInstance()
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(mcontext, "Removed...", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {e->
                    Toast.makeText(mcontext, "Unable to removed due to ${e.message}", Toast.LENGTH_LONG).show()

                }
        }
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

}