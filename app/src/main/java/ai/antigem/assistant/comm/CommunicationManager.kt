package ai.antigem.assistant.comm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

// ============================================================================
// Communication Engine - Calls, SMS, Contacts
// ============================================================================

data class ContactInfo(
    val id: String,
    val name: String,
    val phoneNumbers: List<String> = emptyList(),
    val emailAddresses: List<String> = emptyList()
)

data class CallResult(
    val success: Boolean,
    val message: String
)

data class SmsResult(
    val success: Boolean,
    val message: String
)

class CommunicationManager(private val context: Context) {

    // ---------- Contacts ----------
    fun getContacts(): List<ContactInfo> {
        val contacts = mutableListOf<ContactInfo>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use { cur ->
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhone = cur.getInt(cur.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0

                val phones = mutableListOf<String>()
                val emails = mutableListOf<String>()

                if (hasPhone) {
                    val phonesCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    phonesCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            val number = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            phones.add(number)
                        }
                    }
                }

                val emailsCursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )
                emailsCursor?.use { ec ->
                    while (ec.moveToNext()) {
                        val email = ec.getString(ec.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        emails.add(email)
                    }
                }

                contacts.add(ContactInfo(id = id, name = name, phoneNumbers = phones, emailAddresses = emails))
            }
        }
        return contacts
    }

    fun searchContacts(query: String): List<ContactInfo> {
        return getContacts().filter { c ->
            c.name.contains(query, ignoreCase = true) ||
            c.phoneNumbers.any { it.contains(query, ignoreCase = true) }
        }
    }

    // ---------- Phone Calls ----------
    fun canMakeCalls(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun requestCallPermission(activity: android.app.Activity) {
        activity.requestPermissions(
            arrayOf(android.Manifest.permission.CALL_PHONE),
            1001
        )
    }

    fun makeCall(phoneNumber: String, requireConfirmation: Boolean = true): CallResult {
        if (!canMakeCalls()) {
            return CallResult(false, "Call permission not granted")
        }
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_CALL, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            return CallResult(true, "Calling $phoneNumber")
        } catch (e: Exception) {
            return CallResult(false, "Failed to call: ${e.message}")
        }
    }

    fun dialNumber(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // ---------- SMS ----------
    fun canSendSms(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    fun requestSmsPermission(activity: android.app.Activity) {
        activity.requestPermissions(
            arrayOf(android.Manifest.permission.SEND_SMS),
            1002
        )
    }

    fun sendSms(phoneNumber: String, message: String): SmsResult {
        if (!canSendSms()) {
            return SmsResult(false, "SMS permission not granted")
        }
        try {
            val sentIntent = Intent("SMS_SENT")
            val deliveredIntent = Intent("SMS_DELIVERED")
            val piSent = android.app.PendingIntent.getBroadcast(
                context, 0, sentIntent,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val piDelivered = android.app.PendingIntent.getBroadcast(
                context, 0, deliveredIntent,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            android.telephony.SmsManager.getDefault().sendTextMessage(
                phoneNumber, null, message, piSent, piDelivered
            )
            return SmsResult(true, "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            return SmsResult(false, "Failed to send SMS: ${e.message}")
        }
    }

    fun composeSms(phoneNumber: String, message: String) {
        val uri = Uri.parse("smsto:$phoneNumber")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // ---------- Permission checks ----------
    fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: android.app.Activity, permission: String, requestCode: Int) {
        activity.requestPermissions(arrayOf(permission), requestCode)
    }
}
