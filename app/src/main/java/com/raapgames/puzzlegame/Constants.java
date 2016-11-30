package com.raapgames.puzzlegame;


import android.os.Environment;

/**
 * Created by anandh on 11/17/16.
 */

public class Constants
{
    public static final String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String APPLICATION_PATH = "/Pictures/PhotoPuzzle/";
    public static final String LOG_TAG = "PhotoPuzzle";
    public static final String PREF_NAME = "UserPref";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_FB_ID = "facebook_id";
    public static final String PREF_FB_NAME = "facebook_name";
    public static final String PREF_INST_NAME = "inst_name";
    public static final String PREF_INST_ID = "inst_id";
    public static final String IS_DATA_SYNCED = "is_data_synced";
    public static final String REGISTRATION_URL = "http://photopuzzle-aquizaday.rhcloud.com/PhotoPuzzle/register/";
    
}