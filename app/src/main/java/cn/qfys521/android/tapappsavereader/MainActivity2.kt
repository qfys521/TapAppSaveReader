package cn.qfys521.android.tapappsavereader

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity2() : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)

        // Request storage permission
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_OPEN_DOCUMENT)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFile()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access files.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT)
        Toast.makeText(this, "Please select the .userdata file you want to open.", Toast.LENGTH_LONG).show()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK) {
            val uri = data.data
            try {
                val content = readTextFromUri(uri)
                if (isUserDataFile(content)) {
                    val username = parseUsernameFromJSON(content)
                    val objectId = parseObjectIdFromJSON(content)
                    val sessionToken = parseSessionTokenFromJSON(content)
                    val editText = findViewById<EditText>(R.id.editText)
                    editText.setText(
                        "username: " + username + "\n" +
                                "objectId: " + objectId + "\n" +
                                "sessionToken: " + sessionToken
                    )
                } else {
                    Toast.makeText(this, "This is not a .userdata file.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Read file exception: " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri?): String {
        val stringBuilder = StringBuilder()
        BufferedReader(InputStreamReader(contentResolver.openInputStream((uri)!!))).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                stringBuilder.append(line).append("\n")
            }
        }
        return stringBuilder.toString()
    }

    private fun isUserDataFile(content: String): Boolean {
        return content.contains("className\":\"_User\"")
    }

    @Throws(JSONException::class)
    private fun parseUsernameFromJSON(content: String): String {
        val jsonObject = JSONObject(content)
        return jsonObject.getString("username")
    }

    @Throws(JSONException::class)
    private fun parseObjectIdFromJSON(content: String): String {
        val jsonObject = JSONObject(content)
        return jsonObject.getString("objectId")
    }

    @Throws(JSONException::class)
    private fun parseSessionTokenFromJSON(content: String): String {
        val jsonObject = JSONObject(content)
        return jsonObject.getString("sessionToken")
    }

    companion object {
        private val REQUEST_CODE_OPEN_DOCUMENT = 1
    }
}