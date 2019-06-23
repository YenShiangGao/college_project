package com.ggfood.test.ggfood_api;

import ai.api.AIConfiguration;

/**
 * Created by Wei on 2017/7/25.
 */

public class Config {
    public static final String ACCESS_TOKEN="db54e357667946b491380c3ee74dec0b";
    public static String HOSTURL = "wei18963.ddns.net";
    public static final String SHARED_PREF_NAME = "ggfood_login";
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";
    public static float rate = 1.3f;
    public static float pitch = 1.5f;
    //是否關閉語音
    public static boolean isOpen = true;

    public static class AI{
        public static final AIConfiguration.SupportedLanguages LANGUAGES = AIConfiguration.SupportedLanguages.ChineseTaiwan;
    }

    public static class ITRI{
        //取樣率
        public static final int RATE = 16000;
        public static final String AUTHORIZATION_ID = "8befe2c1be9a24fcf1b7320d6e8917a8";
        public static String locName = "useUserDefineSTT";
        public static String taskName = "GGfood";
    }
}
