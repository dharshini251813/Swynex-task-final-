package com.example.expensetracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.app.Activity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    data class Transaction(val id: Long, val title: String, val amount: Double, val category: String, val type: String, val date: String)

    private val transactions = mutableListOf<Transaction>()
    private val prefs by lazy { getSharedPreferences("pocketspend_data", MODE_PRIVATE) }
    private lateinit var balanceText: TextView
    private lateinit var incomeText: TextView
    private lateinit var expenseText: TextView
    private lateinit var list: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var filterSpinner: Spinner
    private lateinit var titleInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        balanceText = findViewById(R.id.balanceText)
        incomeText = findViewById(R.id.incomeText)
        expenseText = findViewById(R.id.expenseText)
        list = findViewById(R.id.transactionList)
        emptyText = findViewById(R.id.emptyText)
        filterSpinner = findViewById(R.id.filterSpinner)
        titleInput = findViewById(R.id.titleInput)
        amountInput = findViewById(R.id.amountInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        typeSpinner = findViewById(R.id.typeSpinner)

        setupSpinner(categorySpinner, listOf("Food", "Transport", "Shopping", "Bills", "Health", "Salary", "Education", "Other"))
        setupSpinner(typeSpinner, listOf("Expense", "Income"))
        setupSpinner(filterSpinner, listOf("All", "Expenses", "Income"))
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = renderTransactions()
        }
        findViewById<Button>(R.id.addButton).setOnClickListener { addTransaction() }
        loadTransactions()
        updateDashboard()
        renderTransactions()
    }

    private fun setupSpinner(spinner: Spinner, values: List<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
    }

    private fun addTransaction() {
        val title = titleInput.text.toString().trim()
        val amount = amountInput.text.toString().trim().toDoubleOrNull()
        if (title.isBlank()) { titleInput.error = "Enter a description"; titleInput.requestFocus(); return }
        if (amount == null || amount <= 0) { amountInput.error = "Enter an amount greater than zero"; amountInput.requestFocus(); return }
        val category = categorySpinner.selectedItem.toString()
        val type = typeSpinner.selectedItem.toString()
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        transactions.add(0, Transaction(System.currentTimeMillis(), title, amount, category, type, date))
        saveTransactions()
        titleInput.text.clear()
        amountInput.text.clear()
        typeSpinner.setSelection(0)
        updateDashboard()
        renderTransactions()
        Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
    }

    private fun updateDashboard() {
        val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        balanceText.text = money(income - expenses)
        incomeText.text = money(income)
        expenseText.text = money(expenses)
    }

    private fun renderTransactions() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val filter = if (::filterSpinner.isInitialized && filterSpinner.selectedItem != null) filterSpinner.selectedItem.toString() else "All"
        val shown = transactions.filter { when (filter) { "Expenses" -> it.type == "Expense"; "Income" -> it.type == "Income"; else -> true } }
        emptyText.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
        shown.forEach { tx ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(14), dp(10), dp(14))
                setBackgroundResource(R.drawable.input_bg)
                val params = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }
                layoutParams = params
            }
            val icon = TextView(this).apply {
                text = categoryIcon(tx.category)
                textSize = 20f
                gravity = Gravity.CENTER
                setBackgroundColor(0xFFEDEEFF.toInt())
                val size = dp(44)
                layoutParams = LinearLayout.LayoutParams(size, size)
            }
            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, dp(4), 0)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                addView(TextView(this@MainActivity).apply { text = tx.title; textSize = 15f; setTextColor(0xFF171A2C.toInt()); setTypeface(null, android.graphics.Typeface.BOLD) })
                addView(TextView(this@MainActivity).apply { text = "${tx.category} • ${tx.date}"; textSize = 12f; setTextColor(0xFF777B8D.toInt()); setPadding(0, dp(4), 0, 0) })
            }
            val amount = TextView(this).apply {
                text = (if (tx.type == "Income") "+ " else "− ") + money(tx.amount)
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(if (tx.type == "Income") 0xFF13865C.toInt() else 0xFFD64B62.toInt())
            }
            row.addView(icon); row.addView(details); row.addView(amount)
            row.setOnLongClickListener {
                AlertDialog.Builder(this).setTitle("Delete transaction?").setMessage("Remove ‘${tx.title}’ from your history?")
                    .setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ ->
                        transactions.removeAll { it.id == tx.id }; saveTransactions(); updateDashboard(); renderTransactions()
                    }.show()
                true
            }
            list.addView(row)
        }
    }

    private fun categoryIcon(category: String) = when (category) {
        "Food" -> "🍜"; "Transport" -> "🚌"; "Shopping" -> "🛍️"; "Bills" -> "🧾"; "Health" -> "💊"; "Salary" -> "💼"; "Education" -> "📚"; else -> "💳"
    }
    private fun money(value: Double) = "₹ " + String.format(Locale.getDefault(), "%,.2f", value)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun saveTransactions() {
        val array = JSONArray()
        transactions.forEach { tx -> array.put(JSONObject().apply {
            put("id", tx.id); put("title", tx.title); put("amount", tx.amount); put("category", tx.category); put("type", tx.type); put("date", tx.date)
        }) }
        prefs.edit().putString("transactions", array.toString()).apply()
    }

    private fun loadTransactions() {
        transactions.clear()
        val raw = prefs.getString("transactions", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                transactions.add(Transaction(item.getLong("id"), item.getString("title"), item.getDouble("amount"), item.getString("category"), item.getString("type"), item.getString("date")))
            }
        } catch (_: Exception) { transactions.clear() }
    }
}
