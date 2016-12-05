package com.raapgames.puzzlegame;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.util.Arrays;
import java.util.List;

/**
 * Created by anandh on 12/4/16.
 */

public class SuccessActivity extends AppCompatActivity
{
    private ShareButton shareButton;
    private CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback(){
            @Override
            public void onInitialized()
            {
                if(AccessToken.getCurrentAccessToken() != null)
                {
                    Log.d("AccessTokenPresent","here");
                }
                else
                {
                    Log.d("initializing","login");
                    List<String> permissions = Arrays.asList("publish_actions");
                    LoginManager.getInstance().logInWithPublishPermissions(SuccessActivity.this, permissions);
                }
            }
        });
        callbackManager= CallbackManager.Factory.create();
        setContentView(R.layout.success_layout);
        shareButton = (ShareButton) findViewById(R.id.shareButton);
        SharePhoto photo = new SharePhoto.Builder().setBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)).build();
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder().putString("og:title","Beat this score!").putString("og:type","games.game")
                .putString("og:description","Play this puzzle and beat this score!").putPhoto("image",photo).build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("games.game")
                .putObject("game", object)
                .build();
        final ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("game")
                .setAction(action)
                .build();

        shareButton.setShareContent(content);
        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d("Before click","here");
                ShareDialog.show(SuccessActivity.this,content);

            }
        });
        ShareDialog.show(SuccessActivity.this,content);
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
