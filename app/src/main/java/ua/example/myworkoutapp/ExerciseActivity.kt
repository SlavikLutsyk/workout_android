package ua.example.myworkoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ua.example.myworkoutapp.databinding.ActivityExerciseBinding
import ua.example.myworkoutapp.databinding.DialogCustomBackConfirmationBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding : ActivityExerciseBinding? = null
    private var restTimer : CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimer : CountDownTimer? = null
    private var exerciseProgress = 0

    //private var restTimerDuration : Long = 10
    //private var exerciseTimerDuration : Long = 10

    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts : TextToSpeech? = null
    private var player : MediaPlayer? = null

    private var exerciseAdapter : ExerciseStatusAdapter? = null

    private var isDelayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarExercise)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        exerciseList = Constants.defaultExerciseList()
        tts = TextToSpeech(this, this)
        binding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }

        setUpRestView()
        setUpExerciseRecyclerView()
    }

    private fun customDialogForBackButton(){
        stopTimer()
        val dialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialogBinding?.btnYes?.setOnClickListener {
            this@ExerciseActivity.finish()
            dialog.dismiss()
            isDelayed = false
        }
        dialogBinding?.btnNo?.setOnClickListener {
            dialog.dismiss()
            Log.i("RestProgress", restProgress.toString())
            Log.i("ExerciseProgress", exerciseProgress.toString())
            if (restProgress != 0)
                setRestProgressBar(10000 - (restProgress * 1000).toLong())
            if (exerciseProgress != 0)
                setExerciseProgressBar(30000 - (exerciseProgress * 1000).toLong())
            isDelayed = false
        }
        dialog.show()
    }

    private fun setUpExerciseRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setRestProgressBar(time : Long){
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer( time, 1000){

            override fun onTick(p0: Long) {
                    restProgress++
                    binding?.progressBar?.progress = 10 - restProgress
                    binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
                if (!isDelayed) {
                    currentExercisePosition++
                    exerciseList!![currentExercisePosition].setIsSelected(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setUpExerciseView()
                }
            }

        }.start()
    }

    private fun stopTimer(){
        exerciseTimer?.cancel()
        restTimer?.cancel()
        isDelayed = true
    }

    private fun setExerciseProgressBar(time : Long){
        binding?.progressBarExercise?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(time, 1000){

            override fun onTick(p0: Long) {
                    exerciseProgress++
                    binding?.progressBarExercise?.progress = 30 - exerciseProgress
                    binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
                if (!isDelayed) {
                    if (currentExercisePosition < exerciseList!!.size - 1) {
                        exerciseList!![currentExercisePosition].setIsSelected(false)
                        exerciseList!![currentExercisePosition].setIsCompleted(true)
                        exerciseAdapter!!.notifyDataSetChanged()
                        setUpRestView()
                    } else {
                        finish()
                        val intent = Intent(this@ExerciseActivity, FinishActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

        }.start()
    }

    private fun setUpRestView(){

        try {
            val soundURI = Uri.parse(
                "android.resource://ua.example.myworkoutapp/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()
        }catch (e : Exception){
            e.printStackTrace()
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExercise?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE

        binding?.tvUpcomingLabel?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE
        if (restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }
        binding?.tvUpcomingExerciseName?.text =
            exerciseList!![currentExercisePosition + 1].getName()
        setRestProgressBar(10000)
    }

    private fun setUpExerciseView(){
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExercise?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcomingLabel?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE
        if (exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }
        speakOut(exerciseList!![currentExercisePosition].getName())
        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExercise?.text = exerciseList!![currentExercisePosition].getName()
        setExerciseProgressBar(30000)
    }

    override fun onDestroy() {
        super.onDestroy()
         if (restTimer != null){
             restTimer?.cancel()
             restProgress = 0
         }
        if (exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        if (player != null){
            player!!.stop()
        }
        binding = null
    }

    override fun onInit(status: Int) {

        // TODO (Step 5 - After variable initializing set the language after a "success"ful result.)
        // START
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }

        } else {
            Log.e("TTS", "Initialization Failed!")
        }
        // END
    }

    // TODO (Step 6 - Making a function to speak the text.)
    // START
    /**
     * Function is used to speak the text that we pass to it.
     */
    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    // END
}