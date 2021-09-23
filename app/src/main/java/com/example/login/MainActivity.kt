package com.example.login

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
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private const val TAG = "TextWatcherTag"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val loginInputLayout = findViewById<TextInputLayout>(R.id.loginInputLayout)
        loginInputEditText = loginInputLayout.editText as TextInputEditText
        loginInputEditText.addTextChangedListener(textWatcher)

        val loginButton = findViewById<View>(R.id.loginButton)
        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        loginButton.isEnabled = false
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            loginButton.isEnabled = isChecked
        }
        val passInputLayout = findViewById<TextInputLayout>(R.id.passInputLayout)
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
                    Handler(Looper.myLooper()!!).postDelayed({
                        progressBar.visibility = View.GONE
                        contentLayout.visibility = View.VISIBLE
                        val dialog = BottomSheetDialog(this)
                        val view =
                            LayoutInflater.from(this).inflate(R.layout.dialog, contentLayout, false)
                        dialog.setCancelable(false)
                        view.findViewById<View>(R.id.closeButton).setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.setContentView(view)
                        dialog.show()

                    }, 3000)
                    Snackbar.make(loginButton, "Go to postLogin", Snackbar.LENGTH_LONG).show()
                } else {
                    passInputLayout.isErrorEnabled = true
                    passInputLayout.error = getString(R.string.invalid_password)
                }
            } else {
                loginInputLayout.isErrorEnabled = true
                loginInputLayout.error = getString(R.string.invalid_email_message)
            }
        }
        loginInputEditText.listenChanges { loginInputLayout.isErrorEnabled = false }
    }

    private lateinit var loginInputEditText: TextInputEditText
    private lateinit var passInputEditText: TextInputEditText

    private val textWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            Log.d(TAG, "afterTextChanged$s")
            val input = s.toString()
            if (input.endsWith("@g")) {
                Log.d(TAG, "programmatically set text")
                setText("${input}mail.com")
            }
        }
    }

    private fun setText(text: String) {
        loginInputEditText.removeTextChangedListener(textWatcher)
        loginInputEditText.setTextCorrectly(text)
        loginInputEditText.addTextChangedListener(textWatcher)
    }

}

fun AppCompatActivity.hideKeyboard(view: View) {
    val imm = this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun TextInputEditText.listenChanges(block: (text: String) -> Unit) {
    addTextChangedListener(object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable?) {
            block.invoke(s.toString())
        }
    })
}

fun TextInputEditText.setTextCorrectly(text: CharSequence) {
    setText(text)
    setSelection(text.length)
}
