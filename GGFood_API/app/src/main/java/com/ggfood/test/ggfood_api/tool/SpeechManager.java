package com.ggfood.test.ggfood_api.tool;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

/**
 * Created by Wei on 2017/7/25.
 */

public class SpeechManager {
    private final static String TAG = "SpeechRecognizerManager";
//    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    public RecognitionListener mListener;

    public SpeechManager(Context context , RecognitionListener listener) {
        mListener = listener;

//        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = initSpeechRecognizer(context,listener);
        mSpeechRecognizerIntent =initSpeechRecognizerIntent(context);
    }
    //初始化錄音物件，設定聆聽器
    private SpeechRecognizer initSpeechRecognizer(Context context,RecognitionListener listener){
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(mListener);
        return speechRecognizer;
    }
    //錄製的參數設定
    private Intent initSpeechRecognizerIntent(Context context){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        return intent;
    }

    public void startListening() {
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }
    public void stopListening(){
        mSpeechRecognizer.stopListening();
    }
}
