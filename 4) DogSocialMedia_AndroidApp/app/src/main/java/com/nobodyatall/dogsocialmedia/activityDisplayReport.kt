package com.nobodyatall.dogsocialmedia

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class activityDisplayReport : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_report)

        var actionBar = getSupportActionBar()
        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        //get from prev interface
        val LOGIN_USER: String? = intent.getStringExtra("USER")
        val VIEW_REPORT: String? = intent.getStringExtra("VIEW_REPORT")

        val reportNameTV: TextView = findViewById(R.id.reportName_displayReport)
        reportNameTV.text = VIEW_REPORT

        //get report details
        getReport(VIEW_REPORT!!)
    }

    private fun getReport(reportname: String){
        runBlocking {
            val route = ("http://%s:%s/getReport?reportname=" + reportname).format(getString(R.string.api_ip_address), getString(R.string.api_port_address))
            val (request, response, result) = Fuel.get(route)
                .response()

            if (response.statusCode in 200..299) {
                //status code: 200-299 OK
                val msg: String = response.data.decodeToString()

                Log.i("debug", msg)
                if("[!] ERROR in" in msg){
                    snackbarPopup(msg)
                }else if ("[*] You've no report" in msg){
                    //no query
                    snackbarPopup(msg)
                }else{
                    //query success
                    val retJson: JSONObject = JSONObject(msg)
                    val query = retJson.getJSONArray("data")

                    //fillup table
                    plotGraphs(query)
                    //snackbarPopup(msg)
                }
            }else{
                snackbarPopup("[!] Something went wrong")
            }
        }
    }

    private fun plotGraphs(query: JSONArray){
        plotBarChart(query)
        plotLineChart(query)
    }

    private fun plotBarChart(query: JSONArray){
        //dog predicted label
        var happy = 0
        var angry = 0
        var sick = 0

        //initialization
        val barChart: BarChart = findViewById(R.id.barchart_displayReport)

        //count dog predicted label
        for (k in 0 until query.length()) {
            val iterJson: JSONObject = JSONObject(query[k].toString())

            if(iterJson["predicted_label"].toString().replace("\n", "") == "happy")
                happy++
            else if (iterJson["predicted_label"].toString().replace("\n", "") == "angry")
                angry++
            else
                sick++
        }

        //plotting bar chart
        val xvalue: ArrayList<String> = ArrayList()
        xvalue.add("happy")
        xvalue.add("angry")
        xvalue.add("sick")

        val barEntry = ArrayList<BarEntry>()
        barEntry.add(BarEntry(happy.toFloat(), 0))
        barEntry.add(BarEntry(angry.toFloat(), 1))
        barEntry.add(BarEntry(sick.toFloat(), 2))

        val barDataset = BarDataSet(barEntry, "Dog Emotion Predicted")
        barDataset.setColors(ColorTemplate.JOYFUL_COLORS)
        barDataset.setValueTextSize(14f)

        val data = BarData(xvalue, barDataset)
        barChart.data = data
        barChart.setBackgroundColor(ColorTemplate.rgb("FFFFFF"))
        barChart.animateXY(2000, 2000)
        barChart.setDescription("Dog Emotion Label Counting")

        //update description of dog status
        data class Dog(var label: String, var count: Int)
        val dogLabelList = listOf(Dog("happy", happy), Dog("angry", angry), Dog("sick", sick))

        val maxLabel = dogLabelList.maxWithOrNull(Comparator.comparingInt {it.count})

        val descTV: TextView = findViewById(R.id.desc_displayReport)
        if(maxLabel!!.label == "happy")
            descTV.text = "[*] Dog is healthy & happy."
        else if (maxLabel!!.label == "angry")
            descTV.text = "[*] Better check what makes your dog frustrated. It might be the friend?"
        else
            descTV.text = "[!] Dog is unhealthy & depressed. Take the dog to the nearest vet ASAP!"
    }

    private fun plotLineChart(query: JSONArray){
        //initialization
        val lineChart: LineChart = findViewById(R.id.linechart_displayReport)

        val xvalue: ArrayList<String> = ArrayList()
        val lineEntry = ArrayList<Entry>()
        val pattern = "yyyy-MM-dd HH:mm:ss.SSS"
        val newpattern = "HH:mm:ss"
        val sdf = SimpleDateFormat(pattern)
        val sdf2 = SimpleDateFormat(newpattern)

        //plotting line chart
        for (k in 0 until query.length()) {
            val iterJson: JSONObject = JSONObject(query[k].toString())

            var stringDate = iterJson["predict_date"].toString()
            stringDate = stringDate.replace("T", " ")
            stringDate = stringDate.replace("Z", " ")

            val convDate: Date = sdf.parse(stringDate)

            xvalue.add(sdf2.format(convDate).toString())

            if(iterJson["predicted_label"].toString().replace("\n", "") == "happy")
                lineEntry.add(Entry(1.0f, k))
            else if (iterJson["predicted_label"].toString().replace("\n", "") == "angry")
                lineEntry.add(Entry(2.0f, k))
            else
                lineEntry.add(Entry(3.0f, k))
        }

        val linedataset = LineDataSet(lineEntry, "Dog Emotion Predicted")
        linedataset.color = resources.getColor(R.color.purple_500)
        linedataset.setDrawValues(false)

        val data = LineData(xvalue, linedataset)
        lineChart.data = data
        lineChart.setBackgroundColor(resources.getColor(R.color.white))
        lineChart.animateXY(2000,2000)
        lineChart.setDescription("Dog Emotion Timeline")

    }
    private fun snackbarPopup(msg: String){
        val snackBar = Snackbar.make(
            this.findViewById(R.id.desc_displayReport), msg,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackBar.show()
    }

    //preserving previous activity variable values
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this@activityDisplayReport?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}