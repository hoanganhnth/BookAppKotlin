package com.example.bookapp.adapters

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.MyApplication
import com.example.bookapp.R
import com.example.bookapp.databinding.RowCommentBinding
import com.example.bookapp.models.CommentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter(var commentList: ArrayList<CommentModel>): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    class ViewHolder(var binding: RowCommentBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uid = commentList[position].uid
        val timestamp = commentList[position].timestamp
        val comment = commentList[position].comment
        val bookId = commentList[position].bookId
        val mcontext = holder.itemView.context

        val firebaseAuth = FirebaseAuth.getInstance()
        holder.binding.commentTv.text = comment
        holder.binding.dateTv.text = MyApplication.formatTimeStamp(timestamp)

        //load info user
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value.toString()
                    val profileImg = snapshot.child("profileImage").value.toString()

                    holder.binding.nameTv.text = name
                    try {
                        Glide.with(mcontext)
                            .load(profileImg)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.binding.profileIv)
                    }catch (e:Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        //delete comment
        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                val builder = AlertDialog.Builder(mcontext)
                builder.setCancelable(false)
                builder.setTitle("Delete Commit")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("DELETE") { d,e->
                        val ref = FirebaseDatabase.getInstance().getReference("Books")
                        ref.child(bookId).child("Comments").child("$timestamp")
                            .removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(mcontext, "Deleted comment", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {e->
                                Toast.makeText(mcontext, "Unable to delete due to ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        d.dismiss()
                    }
                    .setNegativeButton("CANCEL") { d,e->
                        d.dismiss()
                    }
                    .show()
            }
        }

    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}