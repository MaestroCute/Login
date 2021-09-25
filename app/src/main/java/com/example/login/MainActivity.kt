package com.example.login

import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private const val TAG = "TextWatcherTag"


class MainActivity : AppCompatActivity() {

    private companion object {
        const val INITIAL = 0
        const val PROGRESS = 1
        const val SUCCESS = 2
        const val FAILED = 3
    }

    private var state = INITIAL

    private lateinit var loginInputLayout: TextInputLayout
    private lateinit var loginInputEditText: TextInputEditText
    private lateinit var passInputLayout: TextInputLayout
    private lateinit var passInputEditText: TextInputEditText

    private val textWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            Log.d(TAG, "change ${s.toString()}")
            loginInputLayout.isErrorEnabled = false
            passInputLayout.isErrorEnabled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("screenState", state)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        loginInputEditText.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        loginInputEditText.removeTextChangedListener(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate ${savedInstanceState == null}")
        savedInstanceState?.let {
            state = it.getInt("screenState")
        }
        Log.d(TAG, "state is $state")

        val agreementTextView: TextView = findViewById(R.id.agreementTextView)
        val fulltext = getString(R.string.agreement_full_text)
        val confidential = getString(R.string.terms_of_use)
        val policy = getString(R.string.privacy_policy)
        val spannableString = SpannableString(fulltext)

        class MyClickableSpan(private val lambda: (view: View) -> Unit) : ClickableSpan() {
            override fun onClick(widget: View) = lambda.invoke(widget)

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.BLUE
            }
        }

        val confidentialClickable = MyClickableSpan {
            Snackbar.make(it, "Go to link1", Snackbar.LENGTH_SHORT).show()
        }
        val policyClickable = MyClickableSpan {
            Snackbar.make(it, "Go to link2", Snackbar.LENGTH_SHORT).show()
        }

        spannableString.setSpan(
            confidentialClickable,
            fulltext.indexOf(confidential),
            fulltext.indexOf(confidential) + confidential.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            policyClickable,
            fulltext.indexOf(policy),
            fulltext.indexOf(policy) + policy.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        agreementTextView.run {
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }

        loginInputLayout = findViewById(R.id.loginInputLayout)
        loginInputEditText = loginInputLayout.editText as TextInputEditText

        val loginButton = findViewById<View>(R.id.loginButton)
        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        loginButton.isEnabled = false
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            loginButton.isEnabled = isChecked
        }
        passInputLayout = findViewById(R.id.passInputLayout)
        passInputEditText = passInputLayout.editText as TextInputEditText


        val contentLayout = findViewById<LinearLayout>(R.id.contentLayout)
        val progressBar = findViewById<View>(R.id.progressBar)
        loginButton.setOnClickListener {
            if (EMAIL_ADDRESS.matcher(loginInputEditText.text.toString()).matches()) {
                if (PassValidator(passInputEditText.text.toString()).isValid()) {
                    hideKeyboard(loginInputEditText)
                    loginButton.isEnabled = false
                    contentLayout.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    state = PROGRESS
                    Handler(Looper.myLooper()!!).postDelayed({
                        state = FAILED
                        progressBar.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        showDialog(contentLayout)
                    }, 3000)
                } else {
                    passInputLayout.isErrorEnabled = true
                    passInputLayout.error = getString(R.string.invalid_password)
                }
            } else {
                loginInputLayout.isErrorEnabled = true
                loginInputLayout.error = getString(R.string.invalid_email_message)
            }
        }
        when (state) {
            FAILED -> showDialog(contentLayout)
            SUCCESS -> {
                Snackbar.make(contentLayout, "Success", Snackbar.LENGTH_LONG).show()
                state = INITIAL
            }
        }
    }

    private fun showDialog(viewGroup: ViewGroup) {
        val dialog = Dialog(this)
        val view =
            LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false)
        dialog.setCancelable(false)
        view.findViewById<View>(R.id.closeButton).setOnClickListener {
            state = INITIAL
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }
}

fun AppCompatActivity.hideKeyboard(view: View) {
    val imm = this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

