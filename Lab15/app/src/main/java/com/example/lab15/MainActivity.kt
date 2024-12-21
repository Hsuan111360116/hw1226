package com.example.lab15

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val items: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbrw = MyDBHelper(this).writableDatabase
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

    private fun setListeners() {
        val edBook = findViewById<EditText>(R.id.edBook)
        val edPrice = findViewById<EditText>(R.id.edPrice)

        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            val name = edBook.text.toString().trim()
            val price = edPrice.text.toString().trim()

            if (name.isBlank() || price.isBlank()) {
                showToast("Please fill in both fields.")
                return@setOnClickListener
            }

            try {
                val priceValue = price.toIntOrNull()
                if (priceValue == null) {
                    showToast("Price must be a valid number.")
                    return@setOnClickListener
                }

                dbrw.execSQL("INSERT INTO myTable(book, price) VALUES(?, ?)", arrayOf(name, priceValue))
                showToast("Inserted: $name, Price: $priceValue")
                cleanEditTexts()
            } catch (e: Exception) {
                showToast("Insertion failed: ${e.message}")
            }
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val name = edBook.text.toString().trim()
            val price = edPrice.text.toString().trim()

            if (name.isBlank() || price.isBlank()) {
                showToast("Please fill in both fields.")
                return@setOnClickListener
            }

            try {
                val priceValue = price.toIntOrNull()
                if (priceValue == null) {
                    showToast("Price must be a valid number.")
                    return@setOnClickListener
                }

                dbrw.execSQL("UPDATE myTable SET price = ? WHERE book = ?", arrayOf(priceValue, name))
                showToast("Updated: $name, Price: $priceValue")
                cleanEditTexts()
            } catch (e: Exception) {
                showToast("Update failed: ${e.message}")
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val name = edBook.text.toString().trim()

            if (name.isBlank()) {
                showToast("Please enter a book name.")
                return@setOnClickListener
            }

            try {
                dbrw.execSQL("DELETE FROM myTable WHERE book = ?", arrayOf(name))
                showToast("Deleted: $name")
                cleanEditTexts()
            } catch (e: Exception) {
                showToast("Delete failed: ${e.message}")
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val name = edBook.text.toString().trim()
            val query = if (name.isBlank()) "SELECT * FROM myTable" else "SELECT * FROM myTable WHERE book = ?"
            val args = if (name.isBlank()) null else arrayOf(name)

            var cursor: android.database.Cursor? = null
            try {
                cursor = dbrw.rawQuery(query, args)
                items.clear()

                if (cursor.moveToFirst()) {
                    do {
                        val book = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                        val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
                        items.add("Book: $book\tPrice: $price")
                    } while (cursor.moveToNext())
                }

                adapter.notifyDataSetChanged()
                showToast("Found ${cursor.count} record(s).")
            } catch (e: Exception) {
                showToast("Query failed: ${e.message}")
            } finally {
                cursor?.close()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun cleanEditTexts() {
        findViewById<EditText>(R.id.edBook).text.clear()
        findViewById<EditText>(R.id.edPrice).text.clear()
    }
}
