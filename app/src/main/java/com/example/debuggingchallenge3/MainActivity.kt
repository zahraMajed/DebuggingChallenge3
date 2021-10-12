package com.example.debuggingchallenge3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val definitions = arrayListOf<ArrayList<String>>()

    private lateinit var rvMain: RecyclerView
    private lateinit var rvAdapter: RVAdapter
    private lateinit var etWord: EditText
    private lateinit var btSearch: Button

    var word=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMain = findViewById(R.id.rvMain)

        rvAdapter = RVAdapter(definitions)
        rvMain.adapter = rvAdapter
        rvMain.layoutManager = LinearLayoutManager(this)

        etWord = findViewById(R.id.etWord)
        btSearch = findViewById(R.id.btSearch)

        btSearch.setOnClickListener {
            word=etWord.text.toString()
            if(word.isNotEmpty()) {
                requestAPI()
            } else {
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_LONG).show()
            }
        }//end btnSearch lis

    }

    private fun requestAPI(){
        CoroutineScope(Dispatchers.IO).launch {

            val data = async{
                getDefinition(word)
            }.await()

            if(data.isNotEmpty()){
                updateRV(data)
            }else{
                withContext(Main){
                    Toast.makeText(applicationContext, "No data", Toast.LENGTH_LONG).show()
                }
            }

        }
    }//end requestAPI()

    private fun getDefinition(word: String): String{
        var response = ""
        try {
            response = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$word").readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error: $e")
            Toast.makeText(this, "Unable to get data", Toast.LENGTH_LONG).show()
        }
        return response
    }//end

    private suspend fun updateRV(result: String){
        withContext(Dispatchers.Main){

            try {
                Log.d("MAIN", "DATA: $result")

                val jsonArray = JSONArray(result)
                val main = jsonArray[0]

                val word = JSONObject(main.toString()).getString("word")
                val inside = JSONObject(main.toString()).getJSONArray("meanings")
                    .getJSONObject(0)
                val insid2 = JSONObject(inside.toString()).getJSONArray("definitions")
                val defInd= insid2[0]
                val definition = JSONObject(defInd.toString()).getString("definition")

                Log.d("MAIN", "WORD: $word $definition")
                definitions.add(arrayListOf(word, definition))
                rvAdapter.update()
                etWord.text.clear()
                etWord.clearFocus()
                rvMain.scrollToPosition(definitions.size - 1)
                rvMain.adapter?.notifyDataSetChanged()
            } catch (e:Exception){
                println("Error: $e")
                Toast.makeText(applicationContext, "Unable to get data", Toast.LENGTH_LONG).show()
            }

        }
    }
}