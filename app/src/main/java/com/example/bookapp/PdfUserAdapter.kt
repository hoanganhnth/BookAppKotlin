package com.example.bookapp

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowPdfUserBinding

class PdfUserAdapter(var pdfList:ArrayList<PdfModel>): RecyclerView.Adapter<PdfUserAdapter.ViewHolder>() ,Filterable{

    private var filter: PdfUserFilter?=null
    private lateinit var filterArrayList: ArrayList<PdfModel>
    class ViewHolder(var binging: RowPdfUserBinding):RecyclerView.ViewHolder(binging.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowPdfUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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

        MyApplication.loadCategory(categoryId,holder.binging.categoryTv)
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl,title,holder.binging.pdfView, holder.binging.progressBar,null)
        MyApplication.loadPdfSize(pdfUrl,title,holder.binging.sizeTv)

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
            filter = PdfUserFilter(filterArrayList,this)
        }
        return filter as PdfUserFilter
    }
}