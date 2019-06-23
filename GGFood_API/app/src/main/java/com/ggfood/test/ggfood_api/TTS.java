package com.ggfood.test.ggfood_api;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2016 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

public class TTS {
    private TextToSpeech textToSpeech;

    public void init(final Context context) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if( i == TextToSpeech.SUCCESS ){
                        // 指定的語系: 英文(美國)
                        Locale l = Locale.CHINESE;  // 不要用 Locale.ENGLISH, 會預設用英文(印度)
                        // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                        textToSpeech.setLanguage( l );
                        textToSpeech.setSpeechRate(2.0f);
                        textToSpeech.setPitch(2.0f);
                    }
                }
            });
        }
    }

    public void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}
