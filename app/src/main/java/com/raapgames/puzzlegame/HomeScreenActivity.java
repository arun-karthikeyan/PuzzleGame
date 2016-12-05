package com.raapgames.puzzlegame;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

public class HomeScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {
    public static int count = 0;
    private static final int WRITE_EXTERNAL_STORAGE_ID = 1;
    private static final int READ_EXTERNAL_STORAGE_ID = 2;
    private static final int CAMERA_ID = 3;
    private static final int INTERNET = 4;
//    private Button singlePlayerButton;
//    private Button viewProfile;
    private SharedPreferences userPref;
    private ProgressDialog progressDialog;

    //client for interacting with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.sign_in_screen, R.id.signed_in_screen, R.id.wait_screen
    };
    int mCurScreen = -1;

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.practice_button_1, R.id.practice_button_2, R.id.sign_in_button, R.id.sign_out_button, R.id.button_invite_friends, R.id.button_quick_game,
            R.id.button_timed_challenge, R.id.button_see_invitations
    };

    // how long until the game ends (seconds)
    int mSecondsLeft = -1;

    // game duration, seconds.
    final static int GAME_DURATION = 300;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    //user's current score
    int mScore = 0;
    // Reset game variables in preparation for a new game.
    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();


    public String getUserId()
    {
        return UUID.randomUUID().toString();
    }
    public void pref_init()
    {
        userPref = getSharedPreferences(Constants.PREF_NAME,MODE_PRIVATE);
        Log.d(LOG_TAG,"Instance of SharedPreference obtained");
    }

    public boolean syncDetails(String user_id, String facebook_id, String facebook_name, String instId, String instName)
    {
        boolean result = false;
        URI targetUri = UriComponentsBuilder.fromUriString(Constants.REGISTRATION_URL).path(user_id).queryParam("fb_id",facebook_id).queryParam("fb_name",facebook_name)
                .queryParam("instId",instId).queryParam("instName",instName).build().toUri();
        Log.d(LOG_TAG,"URL: "+targetUri.toString());
        try
        {
            Log.d(LOG_TAG,"NEW URL: "+URLDecoder.decode(targetUri.toString(),"UTF-8"));
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
        switch(view.getId()){

            case R.id.sign_in_button:
            Log.d(LOG_TAG, "Sign-in button has been clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;

            case R.id.practice_button_1:
            case R.id.practice_button_2:
                //start picture select activity
                Log.d(LOG_TAG, "Practice Button Clicked -> PictureSelectActivity starting..");
                startActivity(new Intent(getBaseContext(), PictureSelectActivity.class));
                break;

            case R.id.sign_out_button:
                Log.d(LOG_TAG, "Sign-out button has been clicked");
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.sign_in_screen);
                break;

            case R.id.button_invite_friends:
                Log.d(LOG_TAG, "Invite friends button clicked");
                break;

            case R.id.button_quick_game:
                Log.d(LOG_TAG, "Quick Game button clicked");
                break;

            case R.id.button_timed_challenge:
                Log.d(LOG_TAG, "Timed Challenge button clicked");
                break;

            case R.id.button_see_invitations:
                Log.d(LOG_TAG, "See Invitations button clicked");
                break;

        }
    }

    void switchToScreen(int screenId) {
        //to switch between screens in home-screen-activity

        for(int id: SCREENS){
            findViewById(id).setVisibility(screenId==id?View.VISIBLE:View.GONE);
        }
        mCurScreen = screenId;
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.signed_in_screen);
        }
        else {
            switchToScreen(R.id.sign_in_screen);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.d(LOG_TAG, "onConnected() called. Sign in successful!");

        Log.d(LOG_TAG, "Sign-in succeeded.");
        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(LOG_TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(LOG_TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }

        switchToMainScreen();
    }
    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(LOG_TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.wait_screen);
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(LOG_TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.sign_in_screen);
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

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent){

        super.onActivityResult(requestCode, responseCode, intent);

        switch(requestCode){
            case RC_SIGN_IN:
                Log.d(LOG_TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                }
                break;
        }

    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "entered onStart");
        super.onStart();

        switchToScreen(R.id.wait_screen);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(LOG_TAG,
                    "GameHelper: client was already connected on onStart()");
            switchToMainScreen();
        } else {
            Log.d(LOG_TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }

    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void leaveRoom() {
        Log.d(LOG_TAG, "Leaving room.");
        mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            Log.d(LOG_TAG, "switching to wait-screen from leaveRoom()");
            switchToScreen(R.id.wait_screen);
        } else {
            Log.d(LOG_TAG, "switching to main-screen from leaveRoom()");
            switchToMainScreen();
        }
    }
    @Override
    public void onStop() {
        Log.d(LOG_TAG, "entered onStop");

        // need not leave room for onStop, but only on game-end
        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            Log.d(LOG_TAG, "switching to sign-in-screen from leaveRoom()");
            switchToScreen(R.id.sign_in_screen);
        }
        else {
            Log.d(LOG_TAG, "switching to wait-screen from onStop()");
            switchToScreen(R.id.wait_screen);
        }
        super.onStop();
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
                Log.d(LOG_TAG,"Creating Picture folder");
                picsFolder.mkdirs();
            }
            Log.d(LOG_TAG,picsFolder.getAbsolutePath());
            publishProgress(++count);

            String user_id, facebook_id, facebook_name, instId, instName;
            if(!userPref.contains(Constants.IS_DATA_SYNCED) || !userPref.getBoolean(Constants.IS_DATA_SYNCED,false))
            {
                Log.d(LOG_TAG,"Sync not happened");
                user_id = userPref.getString(Constants.PREF_USER_ID,null);
                user_id = user_id == null?getUserId():user_id;
                Log.d(LOG_TAG,"userId: "+user_id);
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
                Log.d(LOG_TAG,"Sync already happened");
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
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "entered onCreate");
        super.onCreate(savedInstanceState);
        this.requestNecessaryPermissions();


        setContentView(R.layout.home_screen_activity);


//        singlePlayerButton = (Button) findViewById(R.id.single_player);
//        viewProfile = (Button) findViewById(R.id.profile);
        pref_init();
        new InitTasks().execute(0);
//        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getBaseContext(), PictureSelectActivity.class));
//            }
//        });
//        viewProfile.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getBaseContext(),ProfileActivity.class));
//            }
//        });

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        //set onclick listener for google signin button
//        findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // start the sign-in flow
//                Log.d(LOG_TAG, "Sign-in button clicked");
//                mSignInClicked = true;
//                mSignOutClicked = false;
//                mGoogleApiClient.connect();
//            }
//        });

        //set up all click listeners in one go
        for(int id: CLICKABLES){
            findViewById(id).setOnClickListener(this);
        }

    }
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(grantResults.length==0 || !(grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            Log.d(LOG_TAG,"The necessary permissions are not acquired: "+requestCode);
            this.finish();
        }
        switch (requestCode)
        {
            case WRITE_EXTERNAL_STORAGE_ID: {
                Log.d(LOG_TAG, "Write External Storage Permissions Granted");
                break;
            }
            case READ_EXTERNAL_STORAGE_ID: {
                Log.d(LOG_TAG, "Read External Storage Permissions Granted");
                break;
            }
            case CAMERA_ID: {
                Log.d(LOG_TAG, "Camera Permissions Granted");
                break;
            }
            case INTERNET:{
                Log.d(LOG_TAG,"Internet Permission Granted");
            }
            default:
                Log.d(LOG_TAG,"Unknown Permissions Granted");
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