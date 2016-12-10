package com.raapgames.puzzlegame;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareButton;

/**
 * Created by anandh on 12/4/16.
 */

public class SuccessActivity extends AppCompatActivity
{
    private ShareButton shareButton;
    private CallbackManager callbackManager;
    private TextView movesMade;
    private Button mainMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback(){
            @Override
            public void onInitialized()
            {
                if(AccessToken.getCurrentAccessToken() != null)
                {
                    Log.d("AccessTokenPresent","here");
                    Log.d("UserID:",AccessToken.getCurrentAccessToken().getUserId());
                    LoginManager.getInstance().logOut();
                }
            }
        });
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        setContentView(R.layout.success_layout);
        AppEventsLogger.activateApp(getApplication());
        callbackManager= CallbackManager.Factory.create();
        shareButton = (ShareButton) findViewById(R.id.shareButton);
        movesMade = (TextView) findViewById(R.id.success_display);
        mainMenu = (Button) findViewById(R.id.main_menu);
        String message = this.getIntent().getStringExtra("movesMade");
        movesMade.setText("You solved the puzzle in "+message+" moves");

        SharePhoto photo = new SharePhoto.Builder().setBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.feature_graphic)).build();
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder().putString("og:title","Congrats!!").putString("og:type","game.achievement").putString("fb:app_id",getResources().getString(R.string.facebook_app_id))
                .putString("og:description","Solved puzzle in "+message+" moves").putInt("game:points",1000).putPhoto("og:image",photo).build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("games.celebrate")
                .putObject("game", object)
                .build();
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("game")
                .setAction(action)
                .build();

        ShareApi.share(content,null);
        shareButton.setShareContent(content);
        mainMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),HomeScreenActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}