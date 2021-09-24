package com.example.login

import java.util.regex.Matcher
import java.util.regex.Pattern

class PassValidator(var password: String) {

   private val PASSWORD_PATTERN: String = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,31}$"
   private val pattern: Pattern = Pattern.compile(PASSWORD_PATTERN)

   fun isValid(): Boolean {
      val matcher: Matcher = pattern.matcher(password)
      return matcher.matches()
   }

}