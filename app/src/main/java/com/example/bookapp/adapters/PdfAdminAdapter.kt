package com.example.bookapp.adapters

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.*
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.activities.PdfEditActivity
import com.example.bookapp.databinding.RowPdfAdminBinding
import com.example.bookapp.filters.PdfAdminFilter
import com.example.bookapp.models.PdfModel

class PdfAdminAdapter(var pdfList:ArrayList<PdfModel>): RecyclerView.Adapter<PdfAdminAdapter.ViewHolder>() ,Filterable{

    private var filter: PdfAdminFilter?=null
    private lateinit var filterArrayList: ArrayList<PdfModel>
    class ViewHolder(var binging:RowPdfAdminBinding):RecyclerView.ViewHolder(binging.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowPdfAdminBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = pdfList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val des = model.description
        val title = model.title
        val timestamp = model.timestamp
        val pdfUrl = model.url
        val date = MyApplication.formatTimeStamp(timestamp)
        val mcontext = holder.itemView.context
        holder.binging.titleTv.text = title
        holder.binging.subTitleTv.text = des
        holder.binging.dateTv.text = date

        MyApplication.loadCategory(categoryId, holder.binging.categoryTv)
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.binging.pdfView,
            holder.binging.progressBar,
            null
        )
        MyApplication.loadPdfSize(pdfUrl, title, holder.binging.sizeTv)

        holder.binging.moreBtn.setOnClickListener {
            val option = arrayOf("Edit", "Delete")
            val builder = AlertDialog.Builder(mcontext)
            builder.setTitle("Choose option")
                .setItems(option) {dialog, which->
                    if (which == 0) {
                        val intent = Intent(mcontext, PdfEditActivity::class.java)
                        intent.putExtra("bookId",pdfId)
                        intent.putExtra("bookUrl", pdfUrl)
                        mcontext.startActivity(intent)
                    }
                    else {
                        MyApplication.deleteBook(mcontext, pdfUrl, title, pdfId)
                    }

                }
                .show()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mcontext, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            mcontext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun getFilter(): Filter {
        filterArrayList = pdfList
        if (filter == null) {
            filter = PdfAdminFilter(filterArrayList,this)
        }
        return filter as PdfAdminFilter
    }
}