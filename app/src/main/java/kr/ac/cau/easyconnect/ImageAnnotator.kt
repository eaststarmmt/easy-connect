package kr.ac.cau.easyconnect

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.awaitility.Awaitility.await
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.ArrayList

object ImageAnnotator {
    @RequiresApi(Build.VERSION_CODES.O)
    fun batchAnnotateImages(context: Context, uris: List<Uri?>): List<String> {
        val labels: MutableList<String> = ArrayList()
        val labels_ko: MutableList<String> = ArrayList()
        var callsOnboard = 0

        for (uri in uris) {
            if (uri == null) break
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            val base64ImageString = Base64.getEncoder().encodeToString(bytes)

            val url = "https://vision.googleapis.com/v1/images:annotate?key=${context.resources.getString(R.string.VISION_API_KEY)}"
            val client = OkHttpClient()
            val JSON = "application/json; charset=utf-8".toMediaType()
            val body = """{
                            "requests": [
                              {
                                "image": {
                                  "content": "$base64ImageString"
                                },
                                "features": [
                                  {
                                    "maxResults": 10,
                                    "type": "WEB_DETECTION"
                                  },
                                ]
                              }
                            ]
                          }""".toRequestBody(JSON)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            client.newCall(request).let {
                callsOnboard++
                it.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            for ((name, value) in response.headers) {
                                println("$name: $value")
                            }
                            val pathMatcher = object : PathMatcher {
                                override fun pathMatches(path: String) = Pattern.matches(
                                    ".*responses.*webDetection.*webEntities.*description.*",
                                    path
                                )

                                override fun onMatch(path: String, value: Any) {
                                    println("Adding $path = $value")
                                    labels.add(value as String)
                                }
                            }
                            val resBody = response.body!!.string()
                            Log.d("annot", resBody)
                            Klaxon()
                                .pathMatcher(pathMatcher)
                                .parseJsonObject(resBody.reader())
                            callsOnboard--
                        }
                    }
                })
            }
        }
        await().atMost(10, TimeUnit.SECONDS).until { callsOnboard == 0 }
        callsOnboard = 0


        val url = "https://translation.googleapis.com/language/translate/v2?key=${context.resources.getString(R.string.VISION_API_KEY)}"
        val client = OkHttpClient()
        val JSON = "application/json; charset=utf-8".toMediaType()
        val body = """{
                        "q": ["${labels.joinToString("\", \"")}"],
                        "target": "ko",
                      }""".toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).let {
            callsOnboard++
            it.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }
                        val pathMatcher = object : PathMatcher {
                            override fun pathMatches(path: String) = Pattern.matches(
                                ".*data.*translations.*translatedText.*",
                                path
                            )

                            override fun onMatch(path: String, value: Any) {
                                println("Adding $path = $value")
                                labels_ko.add(value as String)
                            }
                        }
                        val resBody = response.body!!.string()
                        Log.d("annot", resBody)
                        Klaxon()
                            .pathMatcher(pathMatcher)
                            .parseJsonObject(resBody.reader())
                        callsOnboard--
                    }
                }
            })
        }
        await().atMost(10, TimeUnit.SECONDS).until { callsOnboard == 0 }
        return labels_ko.map { it.replace(" ", "_") }
    }
}