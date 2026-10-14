# PocketSpend — Expense Tracker App

PocketSpend is a simple Android expense tracker built with Kotlin and standard Android views. It is an alternative final app project and does **not** use StudyOrganizer.

## Features
- Dashboard showing total balance, income, and expenses
- Add income or expense entries
- Categories for Food, Transport, Shopping, Bills, Health, Salary, Education, and Other
- Filter transaction history by all, expenses, or income
- Long-press a transaction to delete it
- Saves transaction data locally on the device using SharedPreferences
- Indian rupee (₹) currency display
- Input validation for description and amount

## Requirements
- Android Studio (a recent version)
- Android SDK 35 (Android Studio can install it for you)
- JDK 17 or the JDK bundled with Android Studio

## Run the app
1. Download and unzip `ExpenseTrackerApp.zip`.
2. Open Android Studio.
3. Choose **Open** and select the extracted `ExpenseTrackerApp` folder (the folder containing `settings.gradle.kts`).
4. Allow Gradle sync to finish. If prompted, install Android SDK 35.
5. Connect an Android phone with USB debugging enabled, or create an Android emulator.
6. Press **Run ▶** and select the device.

## How to use
1. Enter a description, such as `Lunch`.
2. Enter an amount, such as `250`.
3. Choose a category and choose Expense or Income.
4. Tap **Add transaction**.
5. Check the balance, income, and expense cards at the top.
6. Use the history filter to show only income or expenses.
7. Long-press an entry and confirm Delete to remove it.

## Data and privacy
Transactions are stored locally in the app's private SharedPreferences. No account, server, or internet connection is required. Clearing app storage or uninstalling the app removes saved transactions. This is a learning project and is not a replacement for financial/accounting software.

## Project structure
```
ExpenseTrackerApp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/expensetracker/MainActivity.kt
│       └── res/
│           ├── drawable/
│           ├── layout/activity_main.xml
│           └── values/styles.xml
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## Demo note / LinkedIn explanation
"I built PocketSpend, an Android expense tracker that helps users record income and expenses, organize transactions by category, and view their current balance. It includes transaction filters, local data storage, input validation, and a simple dashboard. This project helped me practice Kotlin, Android UI design, event handling, and local persistence."

## Suggested screenshots for submission
Capture these from the running app:
1. Dashboard with the balance, income, and expense summary.
2. Add-transaction form with category and type selections.
3. Transaction history with a few sample entries.
4. History filtered to Expenses or Income.

Use your own screenshots from the running app for the final submission; don't present mockups as screenshots of a working build.
