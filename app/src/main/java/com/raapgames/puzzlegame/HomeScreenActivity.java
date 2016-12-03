package com.raapgames.puzzlegame;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {
    public static int count = 0;
    private static final int WRITE_EXTERNAL_STORAGE_ID = 1;
    private static final int READ_EXTERNAL_STORAGE_ID = 2;
    private static final int CAMERA_ID = 3;
    private static final int INTERNET = 4;
    private Button singlePlayerButton;
//    private Button viewProfile;
    private SharedPreferences userPref;
    private ProgressDialog progressDialog;

    //client for interacting with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    public String getUserId()
    {
        return UUID.randomUUID().toString();
    }
    public void pref_init()
    {
        userPref = getSharedPreferences(Constants.PREF_NAME,MODE_PRIVATE);
        Log.d(Constants.LOG_TAG,"Instance of SharedPreference obtained");
    }
    public boolean syncDetails(String user_id, String facebook_id, String facebook_name, String instId, String instName)
    {
        boolean result = false;
        URI targetUri = UriComponentsBuilder.fromUriString(Constants.REGISTRATION_URL).path(user_id).queryParam("fb_id",facebook_id).queryParam("fb_name",facebook_name)
                .queryParam("instId",instId).queryParam("instName",instName).build().toUri();
        Log.d(Constants.LOG_TAG,"URL: "+targetUri.toString());
        try
        {
            Log.d(Constants.LOG_TAG,"NEW URL: "+URLDecoder.decode(targetUri.toString(),"UTF-8"));
            RestTemplate template = new RestTemplate(true);
            String response = template.getForObject(URLDecoder.decode(targetUri.toString(),"UTF-8"),String.class);
            if(response.equalsIgnoreCase(user_id))
                result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.d("Inside SyncDetails: ",new Boolean(result).toString());
        return result;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInvitationReceived(Invitation invitation) {

    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

    }

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int i, Room room) {

    }

    @Override
    public void onJoinedRoom(int i, Room room) {

    }

    @Override
    public void onLeftRoom(int i, String s) {

    }

    @Override
    public void onRoomConnected(int i, Room room) {

    }

    class InitTasks extends AsyncTask<Integer, Integer, Integer>
    {
        @Override
        protected Integer doInBackground(Integer... input)
        {
            int count = 0;
            final File picsFolder = new File(Constants.EXTERNAL_PATH +Constants.APPLICATION_PATH);
            publishProgress(count);
            if(!picsFolder.exists())
            {
                Log.d(Constants.LOG_TAG,"Creating Picture folder");
                picsFolder.mkdirs();
            }
            Log.d(Constants.LOG_TAG,picsFolder.getAbsolutePath());
            publishProgress(++count);

            String user_id, facebook_id, facebook_name, instId, instName;
            if(!userPref.contains(Constants.IS_DATA_SYNCED) || !userPref.getBoolean(Constants.IS_DATA_SYNCED,false))
            {
                Log.d(Constants.LOG_TAG,"Sync not happened");
                user_id = userPref.getString(Constants.PREF_USER_ID,null);
                user_id = user_id == null?getUserId():user_id;
                Log.d(Constants.LOG_TAG,"userId: "+user_id);
                facebook_id = userPref.getString(Constants.PREF_FB_ID,null);
                facebook_name = userPref.getString(Constants.PREF_FB_NAME,null);
                instId = userPref.getString(Constants.PREF_INST_ID,null);
                instName = userPref.getString(Constants.PREF_INST_NAME,null);
                publishProgress(++count);
                if(syncDetails(user_id,facebook_id,facebook_name,instId,instName))
                {
                    publishProgress(++count);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString(Constants.PREF_USER_ID,user_id);
                    editor.putString(Constants.PREF_FB_ID,facebook_id);
                    editor.putString(Constants.PREF_FB_NAME,facebook_name);
                    editor.putString(Constants.PREF_INST_ID,instId);
                    editor.putString(Constants.PREF_INST_NAME,instName);
                    editor.putBoolean(Constants.IS_DATA_SYNCED,true);
                    editor.commit();
                    publishProgress(++count);
                }
                else
                {
                    count+=2;
                    publishProgress(count);
                }
            }
            else
            {
                Log.d(Constants.LOG_TAG,"Sync already happened");
                count+=3;
                publishProgress(count);
            }
            return 0;
        }
        @Override
        protected void onPreExecute() {
            showDialog(0);
        }
        @Override
        protected void onPostExecute(Integer result) {
            progressDialog.dismiss();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            Integer val = (int) (values[0]*25);
            progressDialog.setProgress(val);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestNecessaryPermissions()
    {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_ID);
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_ID);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_ID);
        }
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET);
        }
    }
    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestNecessaryPermissions();
        setContentView(R.layout.activity_home_screen);
        singlePlayerButton = (Button) findViewById(R.id.single_player);
//        viewProfile = (Button) findViewById(R.id.profile);
        pref_init();
        new InitTasks().execute(0);
        singlePlayerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(getBaseContext(),PictureSelectActivity.class));
            }
        });
//        viewProfile.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getBaseContext(),ProfileActivity.class));
//            }
//        });
    }
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(grantResults.length==0 || !(grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            Log.d(Constants.LOG_TAG,"The necessary permissions are not acquired: "+requestCode);
            this.finish();
        }
        switch (requestCode)
        {
            case WRITE_EXTERNAL_STORAGE_ID: {
                Log.d(Constants.LOG_TAG, "Write External Storage Permissions Granted");
                break;
            }
            case READ_EXTERNAL_STORAGE_ID: {
                Log.d(Constants.LOG_TAG, "Read External Storage Permissions Granted");
                break;
            }
            case CAMERA_ID: {
                Log.d(Constants.LOG_TAG, "Camera Permissions Granted");
                break;
            }
            case INTERNET:{
                Log.d(Constants.LOG_TAG,"Internet Permission Granted");
            }
            default:
                Log.d(Constants.LOG_TAG,"Unknown Permissions Granted");
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("LOADING....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

}