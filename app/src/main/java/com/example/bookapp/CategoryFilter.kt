package com.example.bookapp

import android.widget.Filter

class CategoryFilter: Filter {

    private var filterList: ArrayList<CategoryModel>
    private var categoryAdapter: CategoryAdapter

    constructor(filterList: ArrayList<CategoryModel>, categoryAdapter: CategoryAdapter): super() {
        this.filterList = filterList
        this.categoryAdapter = categoryAdapter
    }
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModel: ArrayList<CategoryModel> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(constraint)) {
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        }
        else {
            results.count = filterList.size
            results.values =  filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        if (results != null) {
            categoryAdapter.categoryList = results.values as ArrayList<CategoryModel>
            categoryAdapter.notifyDataSetChanged()
        }
    }
}