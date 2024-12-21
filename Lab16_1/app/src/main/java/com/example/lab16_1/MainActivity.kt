package com.example.lab16_1

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
            if (edBook.text.isBlank() || edPrice.text.isBlank()) {
                showToast("Please fill in both book name and price.")
            } else {
                try {
                    dbrw.execSQL(
                        "INSERT INTO myTable(book, price) VALUES(?, ?)",
                        arrayOf(edBook.text.toString(), edPrice.text.toString().toInt())
                    )
                    showToast("Added: ${edBook.text}, Price: ${edPrice.text}")
                    cleanEditTexts()
                } catch (e: Exception) {
                    showToast("Failed to add record: ${e.message}")
                }
            }
        }

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            if (edBook.text.isBlank() || edPrice.text.isBlank()) {
                showToast("Please fill in both book name and price.")
            } else {
                try {
                    dbrw.execSQL(
                        "UPDATE myTable SET price = ? WHERE book = ?",
                        arrayOf(edPrice.text.toString().toInt(), edBook.text.toString())
                    )
                    showToast("Updated: ${edBook.text}, Price: ${edPrice.text}")
                    cleanEditTexts()
                } catch (e: Exception) {
                    showToast("Failed to update record: ${e.message}")
                }
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (edBook.text.isBlank()) {
                showToast("Please enter a book name to delete.")
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete '${edBook.text}'?")
                    .setPositiveButton("Yes") { _, _ ->
                        try {
                            dbrw.execSQL(
                                "DELETE FROM myTable WHERE book = ?",
                                arrayOf(edBook.text.toString())
                            )
                            showToast("Deleted: ${edBook.text}")
                            cleanEditTexts()
                        } catch (e: Exception) {
                            showToast("Failed to delete record: ${e.message}")
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val query = if (edBook.text.isBlank()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE book = ?"
            }

            val args = if (edBook.text.isBlank()) null else arrayOf(edBook.text.toString())

            val cursor = dbrw.rawQuery(query, args)
            try {
                items.clear()
                if (cursor.count == 0) {
                    showToast("No records found.")
                } else {
                    showToast("Found ${cursor.count} record(s).")
                    while (cursor.moveToNext()) {
                        items.add("Book: ${cursor.getString(0)}\tPrice: ${cursor.getInt(1)}")
                    }
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                showToast("Failed to fetch records: ${e.message}")
            } finally {
                cursor.close()
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
