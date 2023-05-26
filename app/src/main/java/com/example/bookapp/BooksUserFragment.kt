package com.example.bookapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookapp.databinding.FragmentBooksUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding
    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): Fragment {
            val fragment = BooksUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""
    private lateinit var pdfArrayList: ArrayList<PdfModel>
    private lateinit var pdfUserAdapter: PdfUserAdapter


    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            category = args.getString("category").toString()
            categoryId = args.getString("categoryId").toString()
            uid = args.getString("uid").toString()

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context),container,false)
        // Inflate the layout for this fragment

        if (category == "All") {
            loadAllBook()
        }
        else if (category == "Most Viewed") {
            loadMostViewedBooks("viewsCount")
        }
        else if (category == "Most Downloaded") {
            loadMostDownloadedBooks("downloadsCount")
        }
        else  {
            loadCategorizedBooks()
        }
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    pdfUserAdapter.filter.filter(s)

                } catch (e:Exception) {
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        return binding.root
    }

    private fun loadAllBook() {
        Log.d(TAG,"loadAllBook: Loading all book")
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(PdfModel::class.java)
                    pdfArrayList.add(model!!)
                    pdfUserAdapter = PdfUserAdapter(pdfArrayList)
                    binding.booksRv.adapter = pdfUserAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun loadMostViewedBooks(viewsCount: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(viewsCount).limitToLast(10)
            .addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(PdfModel::class.java)
                    pdfArrayList.add(model!!)
                    pdfUserAdapter = PdfUserAdapter(pdfArrayList)
                    binding.booksRv.adapter = pdfUserAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun loadMostDownloadedBooks(s: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(s).limitToLast(10)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(PdfModel::class.java)
                        pdfArrayList.add(model!!)
                        pdfUserAdapter = PdfUserAdapter(pdfArrayList)
                        binding.booksRv.adapter = pdfUserAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(PdfModel::class.java)
                        pdfArrayList.add(model!!)
                        pdfUserAdapter = PdfUserAdapter(pdfArrayList)
                        binding.booksRv.adapter = pdfUserAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}