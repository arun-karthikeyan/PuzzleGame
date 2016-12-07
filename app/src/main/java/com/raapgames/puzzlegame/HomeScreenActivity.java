package com.raapgames.puzzlegame;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
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
import java.util.ArrayList;
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

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

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
    Map<String, int[]> mParticipantScore = new HashMap<String, int[]>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

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
                startQuickGame();
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

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(R.id.wait_screen);
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
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

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer)
            return; // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
//        updateScoreDisplay();
        broadcastScore(false);
//        switchToScreen(R.id.screen_game);

//        findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);

        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    // Game tick -- update countdown, check if game ended.
    void gameTick() {
        if (mSecondsLeft > 0)
            --mSecondsLeft;

        // update countdown
//        ((TextView) findViewById(R.id.countdown)).setText("0:" +
//                (mSecondsLeft < 10 ? "0" : "") + String.valueOf(mSecondsLeft));

        if (mSecondsLeft <= 0) {
            // finish game
//            findViewById(R.id.button_click_me).setVisibility(View.GONE);
            broadcastScore(true);
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
//        byte[] buf = rtm.getMessageData();
//        String sender = rtm.getSenderParticipantId();
//        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);
//
//        if (buf[0] == 'F' || buf[0] == 'U') {
//            // score update.
//            int existingScore = mParticipantScore.containsKey(sender) ?
//                    mParticipantScore.get(sender) : 0;
//            int thisScore = (int) buf[1];
//            if (thisScore > existingScore) {
//                // this check is necessary because packets may arrive out of
//                // order, so we
//                // should only ever consider the highest score we received, as
//                // we know in our
//                // game there is no way to lose points. If there was a way to
//                // lose points,
//                // we'd have to add a "serial number" to the packet.
//                mParticipantScore.put(sender, thisScore);
//            }
//
//            // update the scores on the screen
//            updatePeerScoresDisplay();
//
//            // if it's a final score, mark this participant as having finished
//            // the game
//            if ((char) buf[0] == 'F') {
//                mFinishedParticipants.add(rtm.getSenderParticipantId());
//            }
//        }

        byte[] buf = realTimeMessage.getMessageData();
        String sender = realTimeMessage.getSenderParticipantId();
        Log.d(LOG_TAG,"Message received : "+(char)buf[0] +"; "+(int)buf[1]+"; "+(int)buf[2]);

//        if(buf[0]=='F'){
            //notify all users only if one of them completes the game
            int moves = (int)buf[1];
            int time = (int)buf[2];
            mParticipantScore.put(sender, new int[]{moves, time});
            //add finished participant in mFinishedParticipants
            mFinishedParticipants.add(realTimeMessage.getSenderParticipantId());
            //update status on the display screen
//            updatePeerScoresDisplay();
//        }



    }
    // updates the screen with the scores from our peers
//    void updatePeerScoresDisplay() {
//        ((TextView) findViewById(R.id.score0)).setText(formatScore(mScore) + " - Me");
//        int[] arr = {
//                R.id.score1, R.id.score2, R.id.score3
//        };
//        int i = 0;
//
//        if (mRoomId != null) {
//            for (Participant p : mParticipants) {
//                String pid = p.getParticipantId();
//                if (pid.equals(mMyId))
//                    continue;
//                if (p.getStatus() != Participant.STATUS_JOINED)
//                    continue;
//                int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;
//                ((TextView) findViewById(arr[i])).setText(formatScore(score) + " - " +
//                        p.getDisplayName());
//                ++i;
//            }
//        }
//
//        for (; i < arr.length; ++i) {
//            ((TextView) findViewById(arr[i])).setText("");
//        }
//    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
//            updatePeerScoresDisplay();
        }
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        updateRoom(room);
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(LOG_TAG, "onConnectedToRoom.");

        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mRoomId==null)
            mRoomId = room.getRoomId();

        // print out the list of participants (for debug purposes)
        Log.d(LOG_TAG, "Room ID: " + mRoomId);
        Log.d(LOG_TAG, "My ID " + mMyId);
        Log.d(LOG_TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        updateRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
    Log.d(LOG_TAG, "Entered onRoomCreated");
        Log.d(LOG_TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(LOG_TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        Log.d(LOG_TAG, "Entered onJoinedRoom");
    }

    @Override
    public void onLeftRoom(int i, String s) {
        Log.d(LOG_TAG, "Entered onLeftRoom");
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        Log.d(LOG_TAG, "Entered onRoomConnected");
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent){

        super.onActivityResult(requestCode, responseCode, intent);
        Log.d(LOG_TAG, "Reached on activity result");
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
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(LOG_TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
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