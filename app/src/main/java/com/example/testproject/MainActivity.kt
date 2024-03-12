package com.example.testproject

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.adapter.ProductAdapter
import com.example.testproject.databinding.ActivityMainBinding
import com.example.testproject.helpers.Constants
import com.example.testproject.helpers.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mainAdapter: ProductAdapter
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding
    private var totalCount = 0
    private var currentPage = 0
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRv()
        if (totalCount == 0)
            getProductsCount()

        loadProducts(currentPage)

        binding.reView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleProductsCount = viewManager.childCount
                val totalProductCount = viewManager.itemCount
                val firstVisibleProduct = viewManager.findFirstVisibleItemPosition()
                if (!isLoading) {
                    if (visibleProductsCount + firstVisibleProduct >= totalProductCount
                    ) {
                        if ((totalCount / Constants.ITEMS_LIMIT) - 1 != currentPage)
                            binding.nextButton.visibility = View.VISIBLE
                        if (currentPage != 0)
                            binding.previousButton.visibility = View.VISIBLE
                        buttonClick()
                    } else {
                        binding.nextButton.visibility = View.GONE
                        binding.previousButton.visibility = View.GONE
                    }

                }
            }
        })


    }

    private fun buttonClick() {
        binding.nextButton.setOnClickListener {
            currentPage++
            loadProducts(currentPage)
            binding.progressBar.visibility = View.VISIBLE
        }

        binding.previousButton.setOnClickListener {
            currentPage--
            loadProducts(currentPage)
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun loadProducts(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val skip = page * Constants.ITEMS_LIMIT
            try{
            val response = RetrofitInstance.api.getAllProducts(skip, Constants.ITEMS_LIMIT)

            if (response.isSuccessful) {
                val list = response.body()
                if (list != null) {
                    if (list.products.isNotEmpty()) {
                        isLoading = false
                        runOnUiThread {
                            mainAdapter.submitList(response.body()?.products)
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.nextButton.visibility = View.GONE
                            binding.previousButton.visibility = View.GONE
                        }
                    } else {
                        isLoading = true
                        runOnUiThread {
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.nextButton.visibility = View.GONE
                        }
                    }

                }
            }

            } catch (e: IOException){
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@runOnUiThread
                }
            }
        }
    }

        private fun getProductsCount() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.getProductsCount()
                    totalCount = response.body()!!.total
                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Check your internet connection.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@runOnUiThread
                    }
                } catch (e: HttpException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Couldn't make a request",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        return@runOnUiThread
                    }
                }
            }
        }

        private fun initRv() = binding.reView.apply {
            mainAdapter = ProductAdapter()
            viewManager = LinearLayoutManager(this@MainActivity)
            layoutManager = viewManager
            adapter = mainAdapter
        }

    }