package com.example.bookapp

import android.widget.Filter

class PdfUserFilter: Filter {

    private var filterList: ArrayList<PdfModel>
    private var adapter: PdfUserAdapter

    constructor(filterList: ArrayList<PdfModel>, adapter: PdfUserAdapter) : super() {
        this.filterList = filterList
        this.adapter = adapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        var results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel: ArrayList<PdfModel> = ArrayList()
            for (i in filterList.indices) {
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