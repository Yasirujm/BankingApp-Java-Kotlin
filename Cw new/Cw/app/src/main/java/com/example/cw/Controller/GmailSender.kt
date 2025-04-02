package com.example.cw.Controller

import java.util.*
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

class GmailSender(private val email: String, private val password: String) {

    private val props = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }

    private val session: Session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(email, password)
        }
    })

    fun sendEmail(toEmail: String, subject: String, message: String): Boolean {
        return try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                this.subject = subject
                setText(message)
            }

            Transport.send(mimeMessage)
            true
        } catch (e: MessagingException) {
            e.printStackTrace()
            false
        }
    }
}
