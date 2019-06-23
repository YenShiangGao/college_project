package com.ggfood.test.ggfood_api;

import android.content.Context;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.services.GoogleRecognitionServiceImpl;
import ai.api.services.SpeaktoitRecognitionServiceImpl;

/**
 * Created by Wei on 2017/8/31.
 */

public abstract class ExAiService extends AIService{
    protected ExAiService(AIConfiguration config, Context context) {
        super(config, context);
    }
    public static AIService getService(final Context context, final AIConfiguration config) {
        if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.Google) {
            return new GoogleRecognitionServiceImpl(context, config);
        }
        if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.System) {
            return new GoogleRecognitionServiceImpl(context, config);
        }
        else if (config.getRecognitionEngine() == AIConfiguration.RecognitionEngine.Speaktoit) {
            return new SpeaktoitRecognitionServiceImpl(context, config);
        } else {
            throw new UnsupportedOperationException("This engine still not supported");
        }
    }
}
