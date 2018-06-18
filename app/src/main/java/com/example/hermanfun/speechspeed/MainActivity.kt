package com.example.hermanfun.speechspeed

import android.media.AudioManager
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.iflytek.cloud.*
import com.maxwell.speechrecognition.OnSpeechRecognitionListener
import com.maxwell.speechrecognition.OnSpeechRecognitionPermissionListener
import com.maxwell.speechrecognition.SpeechRecognition

enum class BEEP_TYPE {
    START,
    STOP
}

class MainActivity : AppCompatActivity(), OnSpeechRecognitionPermissionListener, OnSpeechRecognitionListener {

    private val TAG = MainActivity::class.java.simpleName

    var recognitionResult: TextView? = null
    var speechEvaluationResult: TextView? = null
    var startBtn: Button? = null
    var stopBtn: Button? = null
    var speakBtn: Button? = null
    var averageSpeed: TextView? = null
    var currentSpeed: TextView? = null

    private var recognizer: SpeechRecognition? = null
    private var speechStartTimeInMilliSeconds: Long? = null

    private val speechSynthesizer:SpeechSynthesizer
    get() {
        val _tts = SpeechSynthesizer.createSynthesizer(this, synthesizerInitListener)

        _tts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan")
        _tts.setParameter(SpeechConstant.SPEED, "50")
        _tts.setParameter(SpeechConstant.VOLUME, "80")
        _tts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)

        return _tts
    }

    private val speechEvaluator:SpeechEvaluator
    get() {
        val _evaluator = SpeechEvaluator.createEvaluator(this, evaluatorInitListener)

        _evaluator.setParameter(SpeechConstant.LANGUAGE, "en_us")
        _evaluator.setParameter(SpeechConstant.ISE_CATEGORY, "read_chapter")
        _evaluator.setParameter(SpeechConstant.RESULT_LEVEL, "complete")
        _evaluator.setParameter(SpeechConstant.AUDIO_SOURCE, "MediaRecorder.AudioSource.MIC")

        return _evaluator
    }

    private val evaluatorInitListener = object:InitListener {
        override fun onInit(p0: Int) {
            Log.d(TAG, "error in Speech Evaluator init.")
        }
    }
    private val synthesizerInitListener = object:InitListener{
        override fun onInit(p0: Int) {
            Log.d(TAG, "error in Speech Synthesizer init.")
        }
    }

    private val evaluatorListener = object : EvaluatorListener{
        override fun onBeginOfSpeech() {
        }

        override fun onEndOfSpeech() {
        }

        override fun onError(p0: SpeechError?) {
        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
        }

        override fun onResult(p0: EvaluatorResult?, p1: Boolean) {
        }

        override fun onVolumeChanged(p0: Int, p1: ByteArray?) {
        }
    }

    private val synthesizerListener = object:SynthesizerListener{

        override fun onSpeakBegin() {
        }

        override fun onSpeakPaused() {
        }

        override fun onSpeakResumed() {
        }

        override fun onSpeakProgress(p0: Int, p1: Int, p2: Int) {
        }

        override fun onBufferProgress(p0: Int, p1: Int, p2: Int, p3: String?) {
        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
        }

        override fun onCompleted(p0: SpeechError?) {
        }
    }

    private fun initSpeechRecognizer() {
        recognizer = SpeechRecognition(this)
        recognizer?.setSpeechRecognitionPermissionListener(this)
        recognizer?.setSpeechRecognitionListener(this)
    }

    private fun initXunFeiSpeechUtil(){
        SpeechUtility.createUtility(this,SpeechConstant.APPID+"=5b261ec8")
    }

    private fun textToVoice(str2Voice:String){
        speechSynthesizer.startSpeaking(str2Voice,synthesizerListener)
    }

    private fun speechEvaluation(text:String){
        val ret  = speechEvaluator.startEvaluating(text, null,evaluatorListener)
        when(ret){
            ErrorCode.SUCCESS -> {Log.d(TAG, "Speech Evaluation Ok.")}
            else -> {Log.d(TAG, "Speech Evaluation failed with code: ${ret}")}
        }
    }


    private fun initView() {

        recognitionResult = findViewById<TextView>(R.id.main_result_text)
        speechEvaluationResult = findViewById<TextView>(R.id.main_grammar_check_result)
        startBtn = findViewById<Button>(R.id.main_start_btn)
        stopBtn = findViewById<Button>(R.id.main_start_stop)
        speakBtn = findViewById<Button>(R.id.main_speak_btn)
        averageSpeed = findViewById<TextView>(R.id.main_speech_speed_result_text)
        currentSpeed = findViewById<TextView>(R.id.main_speech_current_result_text)

        startBtn?.setOnClickListener {

            recognitionResult?.text = ""
            averageSpeed?.text = ""
            currentSpeed?.text = ""

            recognizer?.startSpeechRecognition()
            speechStartTimeInMilliSeconds = System.currentTimeMillis()

            beep(BEEP_TYPE.START)
        }

        stopBtn?.setOnClickListener {

            recognizer?.stopSpeechRecognition()
            beep(BEEP_TYPE.STOP)
            val interval: Long = ((System.currentTimeMillis() - speechStartTimeInMilliSeconds!!) / 1000)
            averageSpeed?.text = "your are speaking at an average ${calculateSpeed(interval).format(2)} words per second"

            checkGrammar()
        }

        speakBtn?.setOnClickListener{
            textToVoice(recognitionResult?.text.toString())
        }

    }


    private fun beep(type: BEEP_TYPE) {
        var toneGenerator: ToneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        when (type) {
            BEEP_TYPE.START -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 150)
            BEEP_TYPE.STOP -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150)
        }
    }

    private fun calculateSpeed(interval: Long): Float {
        val numOfWords = recognitionResult?.text?.split(" ")?.size
        return numOfWords!!.toFloat() / interval
    }

    private fun Float.format(decimalpoints: Int) = java.lang.String.format("%.${decimalpoints}f", this)

    private fun checkGrammar() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSpeechRecognizer()
        initXunFeiSpeechUtil()
        initView()
    }

    override fun onPermissionDenied() {
        Log.d(TAG, "Permission Denied")
    }

    override fun onPermissionGranted() {
        Log.d(TAG, "Permission Granted")
    }

    override fun OnSpeechRecognitionCurrentResult(p0: String?) {
        Log.d(TAG, p0)
        recognitionResult?.text = p0

        val interval: Long = (System.currentTimeMillis() - speechStartTimeInMilliSeconds!!) / 1000

        currentSpeed?.text = "your are speaking at ${calculateSpeed(interval).format(2)} words per second"
    }

    override fun OnSpeechRecognitionError(p0: Int, p1: String?) {
        Log.d(TAG, "OnSpeechRecognitionError")
        Log.d(TAG, "error_code: ${p0}, error_message: ${p1}")
        recognizer?.startSpeechRecognition()
    }

    override fun OnSpeechRecognitionFinalResult(p0: String?) {
        Log.d(TAG, p0)
        //result?.text = p0
    }

    override fun OnSpeechRecognitionStarted() {
        Log.d(TAG, "OnSpeechRecognitionStarted")
    }

    override fun OnSpeechRecognitionStopped() {
        Log.d(TAG, "OnSpeechRecognitionStopped")
    }
}
