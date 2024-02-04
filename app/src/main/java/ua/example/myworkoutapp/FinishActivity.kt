package ua.example.myworkoutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import ua.example.myworkoutapp.HistoryDao
import ua.example.myworkoutapp.HistoryEntity
import ua.example.myworkoutapp.WorkOutApp
import kotlinx.coroutines.launch
import ua.example.myworkoutapp.databinding.ActivityFinishBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FinishActivity : AppCompatActivity() {
    //Todo 1: Create a binding variable
    private var binding: ActivityFinishBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//Todo 2: inflate the layout
        binding = ActivityFinishBinding.inflate(layoutInflater)
//Todo 3: bind the layout to this Activity
        setContentView(binding?.root)
//Todo 4: attach the layout to this activity
        setSupportActionBar(binding?.toolbarFinishActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarFinishActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
        //END

        //TODO(Step 6 : Adding a click event to the Finish Button.)
        //START
        binding?.btnFinish?.setOnClickListener {
            finish()
        }

        val dao = (application as WorkOutApp).db.historyDao()
        addDateToDatabase(dao)
    }

    private fun addDateToDatabase(historyDao: HistoryDao){
        //val c = Calendar.getInstance()
        val time = LocalDateTime.now(ZoneId.systemDefault())
        //time.plusHours(3)
        //val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date = time.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.getDefault()))
        lifecycleScope.launch {
            historyDao.insert(HistoryEntity(date))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}