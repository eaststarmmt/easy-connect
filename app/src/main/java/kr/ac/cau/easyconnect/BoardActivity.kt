package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class BoardActivity : AppCompatActivity() {

   lateinit var jsonView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        jsonView = findViewById(R.id.jsonObj)
        findViewById<Button>(R.id.create).setOnClickListener {
            var intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }


        findViewById<Button>(R.id.btn_json).setOnClickListener{
            JSonTask().execute("http://192.168.0.10:7878/")
        }
    }
    inner class JSonTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg urls: String?): String {
            val url : URL = URL(urls[0])
            var con : HttpURLConnection = url.openConnection() as HttpURLConnection
            con.connect()
            val stream : InputStream = con.inputStream
            var reader: BufferedReader = BufferedReader(InputStreamReader(stream))

            try {
                var buffer = StringBuffer()
                var line : String = " "
                do {
                    line = reader.readLine()
                    buffer.append(line)
                } while(line != null)
                return buffer.toString()
            } catch (e : MalformedURLException) {
                e.printStackTrace()
            } catch (e : IOException) {
                e.printStackTrace()
            } finally {
                con.disconnect()
                try {
                    if(reader != null) reader.close()
                } catch (e : IOException) {
                    e.printStackTrace()
                }
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            jsonView.setText(result)
        }
    }
}

