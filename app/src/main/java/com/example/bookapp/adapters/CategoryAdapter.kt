package com.example.bookapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.filters.CategoryFilter
import com.example.bookapp.models.CategoryModel
import com.example.bookapp.activities.PdfListAdminActivity
import com.example.bookapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase


class CategoryAdapter(var categoryList: ArrayList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.ViewHolder>(), Filterable {

    private var filter: CategoryFilter? = null
    private lateinit var filterList:ArrayList<CategoryModel>
    inner class ViewHolder(val binding: RowCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowCategoryBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var mcontext = holder.itemView.context
        var model = categoryList[position]
        with(holder) {
            binding.deleteCategoryBtn.setOnClickListener {
                val builder = AlertDialog.Builder(mcontext)
                builder.setTitle("Delete")
                    .setMessage("Are you sure want to delete this category?")
                    .setPositiveButton("Confirm"){a,d->
                        Toast.makeText(mcontext,"Deleting ...", Toast.LENGTH_LONG).show()
                        deleteCategory(model,holder,mcontext)
                    }
                    .setNegativeButton("Cancel") {a,d->
                        a.dismiss()
                    }
                    .show()
            }
            binding.categoryName.setOnClickListener {
                val intent = Intent(mcontext, PdfListAdminActivity::class.java)
                intent.putExtra("categoryId", model.id)
                intent.putExtra("category", model.category)
                mcontext.startActivity(intent)
            }
            binding.categoryName.text = model.category
        }

    }

    private fun deleteCategory(model: CategoryModel, holder: ViewHolder, mcontext: Context,) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(mcontext, "Deleted...", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener {e->
                Toast.makeText(mcontext, "Unable to delete due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun getFilter(): Filter {
        filterList = categoryList
        if (filter == null) {
            filter = CategoryFilter(filterList,this)
        }
        return filter as CategoryFilter
    }
}