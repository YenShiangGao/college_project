package com.ggfood.test.ggfood_api;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ggfood.test.ggfood_api.tool.GGTimer;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
public class STTService extends Service {
    private static final String TAG = "STTService";//TAG
    private ArrayList<STTEvent> listeners;//紀錄連接
    //API SDK
    private AIService aiService;
    private AIListener aiListener;

    private MyBinder binder = new MyBinder();
    private Handler handler = new Handler();
    private TextToSpeech textToSpeech;
    //支援 API 21 以上
    private UtteranceProgressListener defaultTTSListener = new DefaultTTSListener();
    //支援 API 21 以下
    private TextToSpeech.OnUtteranceCompletedListener defaultTTSListener_o = new DefaultTTSListener_o();
    private GGTimer ggTimer = new GGTimer(this);//計時
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        initTTS();
        initAiService();
        listeners = new ArrayList<>(10);
        binder.startRecord();
    }
    private void initTTS() {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if (i == TextToSpeech.SUCCESS) {
                        // 指定的語系: 英文(美國)
                        Locale l = Locale.CHINESE;  // 不要用 Locale.ENGLISH, 會預設用英文(印度)
                        // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                        textToSpeech.setLanguage(l);
                        textToSpeech.setSpeechRate(Config.rate);
                        textToSpeech.setPitch(Config.pitch);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.setOnUtteranceProgressListener(defaultTTSListener);
                        } else {
                            textToSpeech.setOnUtteranceCompletedListener(defaultTTSListener_o);
                        }
                    }
                }
            });
        }
    }
    public void setSpeechRate(float rate) {
        textToSpeech.setSpeechRate(rate);
    }
    public void setPitch(float pitch) {
        textToSpeech.setPitch(pitch);
    }
    public void speak(final String text) {
        if (!text.equals("")) {
            aiService.cancel();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS");
                    } else {
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }, 200);
        }
    }
    public void stopTTS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null, "TTS");
        } else {
            textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public void speakAfter(final String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "TTS");
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }
    private void initAiService() {
        final AIConfiguration.SupportedLanguages lang = AIConfiguration.SupportedLanguages.fromLanguageTag("zh-TW");
        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN, lang, AIConfiguration.RecognitionEngine.System);
        if (aiService != null) {
            aiService.pause();
        }
        aiListener = new AIListener() {
            @Override
            public void onResult(AIResponse response) {
                Log.e(TAG, "onResult");
                Log.i(TAG, "Received success response");
                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());


                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Speech", result.getFulfillment().getSpeech());
                    jsonObject.put("action", result.getAction());
                    jsonObject.put("Parameters", result.getParameters());
                    jsonObject.put("contexts", result.getContexts());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (STTEvent event : listeners) {
                    event.onSTTResult(jsonObject);
                }
                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);


                if (speech == null || speech.length() <= 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binder.startRecord();
                        }
                    }, 300);
                } else if (!speech.equals("nospeak")) {
                    speak(speech);
                }
            }

            @Override
            public void onError(AIError error) {
                Log.e(TAG, "onError" + error);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binder.startRecord();
                    }
                }, 100);
            }

            @Override
            public void onAudioLevel(float level) {
            }
            @Override
            public void onListeningStarted() {
                Log.e(TAG, "onListeningStarted");
            }
            @Override
            public void onListeningCanceled() {
                Log.e(TAG, "onListeningCanceled");
            }
            @Override
            public void onListeningFinished() {
                Log.e(TAG, "onListeningFinished");
            }
        };
        aiService = AIService.getService(this, config);
        aiService.setListener(aiListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        textToSpeech.stop();
    }
    public class MyBinder extends Binder {
        public void startSpeak(final String str) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    speak(str);
                }
            });
        }

        public void stopTTS() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    STTService.this.stopTTS();
                }
            });
        }

        public void startRecord() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    aiService.startListening();
                }
            });
        }

        public void setVoice() {
            setPitch(Config.pitch);
            setSpeechRate(Config.rate);
        }

        public boolean isSpeaking() {
            return textToSpeech.isSpeaking();
        }

        public void stopRecord() {
            aiService.stopListening();
        }

        public boolean registerListener(STTEvent listener) {
            if (listeners.indexOf(listener) == -1) {
                listeners.add(listener);
                Log.e(TAG, "register success");
                return true;
            } else {
                Log.e(TAG, "register fail");
                return false;
            }
        }

        public boolean unregisterListener(STTEvent listener) {
            return listeners.remove(listener);
        }

        public GGTimer getTimer() {
            return ggTimer;
        }

        public void textRequest(String textRequest) {
            new AiTextRequest(textRequest, null, null).execute();
        }
    }
    public interface STTEvent {
        void onSTTResult(JSONObject result);

        void onStartRecord();

        void onStopRecord();
    }
    public UtteranceProgressListener getDefaultTTSListener() {
        return defaultTTSListener;
    }
    public class DefaultTTSListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {
            Log.e(TAG, "onStart");
        }
        @Override
        public void onDone(String utteranceId) {
            Log.e(TAG, "onDone");
            binder.startRecord();
        }
        @Override
        public void onError(String utteranceId) {
            Log.e(TAG, "onError");
        }
    }
    public class DefaultTTSListener_o implements TextToSpeech.OnUtteranceCompletedListener {

        @Override
        public void onUtteranceCompleted(String utteranceId) {
            Log.e(TAG, "onDone");
            binder.startRecord();
        }
    }
    private class AiTextRequest extends AsyncTask<String, Void, AIResponse> {
        private AIError aiError;
        private String query;
        private String event;
        private String contextString;

        public AiTextRequest(String query, String event, String contextString) {
            this.query = query;
            this.event = event;
            this.contextString = contextString;
        }

        @Override
        protected AIResponse doInBackground(final String... params) {
            final AIRequest request = new AIRequest();

            if (!TextUtils.isEmpty(query))
                request.setQuery(query);
            if (!TextUtils.isEmpty(event))
                request.setEvent(new AIEvent(event));

            RequestExtras requestExtras = null;
            if (!TextUtils.isEmpty(contextString)) {
                final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                requestExtras = new RequestExtras(contexts, null);
            }

            try {
                return aiService.textRequest(query, requestExtras);
            } catch (final AIServiceException e) {
                aiError = new AIError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final AIResponse response) {
            if (response != null) {
                onAiTextResult(response);
            } else {
                onAiTextError(aiError);
            }
        }
    }
    public void onAiTextResult(AIResponse response) {
    }
    public void onAiTextError(AIError aiError) {

    }
}
