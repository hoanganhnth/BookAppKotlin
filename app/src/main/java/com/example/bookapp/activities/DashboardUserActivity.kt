package com.example.bookapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookapp.fragments.BooksUserFragment
import com.example.bookapp.models.CategoryModel
import com.example.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<CategoryModel>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPaper)
        binding.tabLayout.setupWithViewPager(binding.viewPaper)

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
            finish()
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    class ViewPagerAdapter(fm :FragmentManager, behavior: Int, context:Context): FragmentPagerAdapter(fm,behavior){
        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentsList.add(fragment)
            fragmentTitleList.add(title)
        }
    }
    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this)
        categoryArrayList= ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                val modelAll = CategoryModel("01", "All", 1, "")
                val modelMostViewed = CategoryModel("01","Most Viewed", 1, "")
                val modelMostDownloaded = CategoryModel("01","Most Downloaded", 1, "")
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}}"
                    ) as BooksUserFragment,modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}}"
                    ) as BooksUserFragment,modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}}"
                    ) as BooksUserFragment,modelMostDownloaded.category
                )
                viewPagerAdapter.notifyDataSetChanged()
                for (ds in snapshot.children) {
                    val model = ds.getValue(CategoryModel::class.java)
                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}}"
                        ) as BooksUserFragment,model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        viewPager.adapter = viewPagerAdapter
    }
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.subTitleTv.text ="Not logged in"
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }
        else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email

        }
    }
}