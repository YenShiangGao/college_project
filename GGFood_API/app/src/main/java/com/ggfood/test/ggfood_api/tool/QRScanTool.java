package com.ggfood.test.ggfood_api.tool;

import android.app.Activity;

import com.google.zxing.integration.android.IntentIntegrator;


public class QRScanTool {
    public static IntentIntegrator createIntentIntegratorFactory(Activity activity){
        IntentIntegrator integrator;
        integrator = new IntentIntegrator(activity);
        integrator.setCaptureActivity(CapturedActivityImpl.class)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                    .setPrompt("請將中心對準條碼")
                    .setCameraId(0)  // Use a specific camera of the device
                    .setBeepEnabled(false)
                    .setBarcodeImageEnabled(true)
                    .setOrientationLocked(true);
        return integrator;
    }

}
