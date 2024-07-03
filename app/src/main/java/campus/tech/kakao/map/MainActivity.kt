package campus.tech.kakao.map

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var db: DataDbHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputText: EditText
    private lateinit var cancelBtn: Button
    private lateinit var resultView: TextView
    private lateinit var searchView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var searchAdapter: LocationAdapter
    private var locationList = ArrayList<LocationData>()
    private var searchList = ArrayList<LocationData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createQuery()
        initialize()
        setRecyclerView()
        setSearchView()
        setSearchListener()
    }

    private fun createQuery() {
        db = DataDbHelper(context = this)

        try {
            Log.d("MainActivity", "Opening database")
            val writeDB = db.writableDatabase

            writeDB.execSQL("DELETE FROM LOCATION") //시작하기 전 이 쿼리문을 추가한다.

            Log.d("MainActivity", "Inserting data")
            for (i in 1..10) {
                val values = ContentValues().apply {
                    put("name", "cafe$i")
                    put("location", "광주 북구 용봉동$i")
                    put("category", "coffee")
                }
                writeDB.insert("LOCATION", null, values)
            }
            for (i in 1..10) {
                val values = ContentValues().apply {
                    put("name", "pharmacy$i")
                    put("location", "전주 완산구 효자동$i")
                    put("category", "약국")
                }
                writeDB.insert("LOCATION", null, values)
            }


            Log.d("MainActivity", "Reading data")
            val readDB = db.readableDatabase

            val cursor = readDB.query(
                "LOCATION",
                null,
                null,
                null,
                null,
                null,
                null
            )

            cursor.use { cur ->
                while (cur.moveToNext()) {
                    val name = cur.getString(cur.getColumnIndexOrThrow("name"))
                    val location = cur.getString(cur.getColumnIndexOrThrow("location"))
                    val category = cur.getString(cur.getColumnIndexOrThrow("category"))
                    Log.d("MainActivity", "Name: $name, Location: $location, Category: $category")
                }
            }
            Log.d("MainActivity", "Database operations completed")
        } catch (e: Exception) {
            Log.e("MainActivity", "Database error: ${e.message}", e)
        }
    }

    private fun initialize() {
        recyclerView = findViewById(R.id.recyclerView)
        inputText = findViewById(R.id.inputText)
        cancelBtn = findViewById(R.id.cancelBtn)
        resultView = findViewById(R.id.resultView)
        searchView = findViewById(R.id.searchView)
    }

    private fun setRecyclerView() {
        recyclerView.adapter = LocationAdapter(locationList, LayoutInflater.from(this))
        recyclerView.layoutManager = LinearLayoutManager(this)
        Log.d("recyclerView", "recyclerView Adapter")
    }

    private fun setSearchView() {
        searchAdapter = LocationAdapter(searchList, LayoutInflater.from(this), this)
        searchView.adapter = searchAdapter
        searchView.layoutManager = LinearLayoutManager(this)

    }

    private fun setSearchListener() {
        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                searchLocations(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) { }
        })
    }

    private fun searchLocations(key: String) {
        val readDB = db.readableDatabase
        locationList.clear()

        val cursor = readDB.query(
            "LOCATION",
            null,
            "name LIKE ? OR location LIKE ? OR category LIKE ?",
            arrayOf("%$key%", "%$key%", "%$key%"),
            null,
            null,
            null
        )

        cursor.use { cur ->
            while (cur.moveToNext()) {
                val name = cur.getString(cur.getColumnIndexOrThrow("name"))
                val location = cur.getString(cur.getColumnIndexOrThrow("location"))
                val category = cur.getString(cur.getColumnIndexOrThrow("category"))
                locationList.add(LocationData(name, location, category))
            }
        }

        updateRecyclerView()
        isShowText()
    }

    private fun updateRecyclerView() {
        (recyclerView.adapter as LocationAdapter).notifyDataSetChanged()
    }


    private fun isShowText() {
        if (locationList.isEmpty()) {
            resultView.visibility = View.VISIBLE
        }
        else {
            resultView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
        Log.d("MainActivity", "Database closed")
    }
}