package com.example.bookapp.filters

import android.widget.Filter
import com.example.bookapp.models.PdfModel
import com.example.bookapp.adapters.PdfAdminAdapter

class PdfAdminFilter: Filter {

    var filterList: ArrayList<PdfModel>
    var adapter: PdfAdminAdapter

    constructor(filterList: ArrayList<PdfModel>, adapter: PdfAdminAdapter) : super() {
        this.filterList = filterList
        this.adapter = adapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        var results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel: ArrayList<PdfModel> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].title.uppercase().contains(constraint)) {
                    filterModel.add(filterList[i])
                }
            }
            results.values = filterModel
            results.count = filterModel.size
        }
        else {
            results.values = filterList
            results.count = filterList.size
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.pdfList = results!!.values as ArrayList<PdfModel>
        adapter.notifyDataSetChanged()
    }
}