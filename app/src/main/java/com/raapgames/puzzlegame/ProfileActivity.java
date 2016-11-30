package com.raapgames.puzzlegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.facebook.AccessToken;import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

/**
 * Created by anandh on 11/18/16.
 */

public class ProfileActivity extends AppCompatActivity
{
    private CallbackManager callbackManager;
    private LoginButton login_Button;

    private ProfilePictureView pic_view;
    private FBProfileDetailsVO detailsVO;
    static class DetailWrapper
    {
        public AccessToken accessToken;
        public Boolean isLoggedIn;
        public String userId;
        public String name;
        public void setname(String name)
        {
            this.name = name;
        }
    }

    public void setDetails(final DetailWrapper detailWrapper)
    {
        GraphRequest request = GraphRequest.newMeRequest(detailWrapper.accessToken, new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted (JSONObject object, GraphResponse response)
            {
                try
                {
                    Log.d(Constants.LOG_TAG,object.getString("name"));
                    detailWrapper.setname(object.getString("name"));
                    Toast.makeText(getApplicationContext(),object.getString("name"),Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        detailWrapper.userId=detailWrapper.accessToken.getUserId();
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final DetailWrapper wrapper = new DetailWrapper();
        Boolean isLoggedIn = false;

        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback(){
        @Override
        public void onInitialized()
        {
            if(AccessToken.getCurrentAccessToken() != null)
            {
                wrapper.isLoggedIn = true;
                wrapper.accessToken = AccessToken.getCurrentAccessToken();
            }
            else
            {
                wrapper.isLoggedIn = false;
                wrapper.accessToken = null;
            }
        }
    });
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        setContentView(R.layout.profile_layout);
        AppEventsLogger.activateApp(getApplication());
        login_Button = (LoginButton) findViewById(R.id.login_button);
        pic_view = (ProfilePictureView) findViewById(R.id.user_profile_pic);
        callbackManager= CallbackManager.Factory.create();
        login_Button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d("User ID",loginResult.getAccessToken().getUserId());
                pic_view.setProfileId(loginResult.getAccessToken().getUserId());
                wrapper.accessToken = loginResult.getAccessToken();
                wrapper.isLoggedIn = true;
                setDetails(wrapper);
                Log.d("After set details: ",wrapper.name);
                SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_NAME,MODE_PRIVATE).edit();
                editor.putString(Constants.PREF_FB_ID,wrapper.userId);
                editor.putString(Constants.PREF_FB_NAME,wrapper.name);
                editor.putBoolean(Constants.IS_DATA_SYNCED,false);
                editor.commit();
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException error) {

            }
        });
        if(wrapper.isLoggedIn)
        {
            setDetails(wrapper);
            pic_view.setProfileId(wrapper.accessToken.getUserId());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}