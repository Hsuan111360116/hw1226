package com.example.lab16_2

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
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
    private val uri = Uri.parse("content://com.example.lab16")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        setListeners()
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

                val values = ContentValues().apply {
                    put("book", name)
                    put("price", priceValue)
                }
                val contentUri = contentResolver.insert(uri, values)
                if (contentUri != null) {
                    showToast("Inserted: $name, Price: $price")
                    cleanEditTexts()
                } else {
                    showToast("Insertion failed.")
                }
            } catch (e: Exception) {
                showToast("Error during insertion: ${e.message}")
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

                val values = ContentValues().apply {
                    put("price", priceValue)
                }
                val count = contentResolver.update(uri, values, "book=?", arrayOf(name))
                if (count > 0) {
                    showToast("Updated: $name, Price: $price")
                    cleanEditTexts()
                } else {
                    showToast("Update failed: No matching record.")
                }
            } catch (e: Exception) {
                showToast("Error during update: ${e.message}")
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val name = edBook.text.toString().trim()

            if (name.isBlank()) {
                showToast("Please enter a book name.")
                return@setOnClickListener
            }

            try {
                val count = contentResolver.delete(uri, "book=?", arrayOf(name))
                if (count > 0) {
                    showToast("Deleted: $name")
                    cleanEditTexts()
                } else {
                    showToast("Delete failed: No matching record.")
                }
            } catch (e: Exception) {
                showToast("Error during delete: ${e.message}")
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val name = edBook.text.toString().trim()
            val selection = if (name.isBlank()) null else "book=?"
            val selectionArgs = if (name.isBlank()) null else arrayOf(name)

            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(uri, null, selection, selectionArgs, null)
                if (cursor == null || cursor.count == 0) {
                    showToast("No records found.")
                    items.clear()
                } else {
                    items.clear()
                    while (cursor.moveToNext()) {
                        val book = cursor.getString(cursor.getColumnIndexOrThrow("book"))
                        val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
                        items.add("Book: $book\tPrice: $price")
                    }
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                showToast("Error during query: ${e.message}")
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
