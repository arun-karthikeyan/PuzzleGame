package com.raapgames.puzzlegame;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.example.games.basegameutils.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

/**
 * Created by anandh on 11/17/16.
 */

public class PictureSelectActivity extends AppCompatActivity
{
    private InstagramApp mApp;
    private GridView gridview,instGridView;
    private ImageButton take_pic;
    private ImageButton gal_pic;
    private ImageButton instagram;
    private Button next_page;
    private Button reselect;
    private ImageView imageView;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ArrayList<String> imageThumbList = new ArrayList<String>();
    private Uri imageUri;
    private ImageLoader imageLoader;
    private final int PIC_CAPTURE_ID = 0;
    private int imageRes = 0;
    final static int[] SCREENS = {
            R.id.image_grid,R.id.pic,R.id.instagram_grid
    };
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(PictureSelectActivity.this, "Check your network.",
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.pic_select_layout);
        setContentView(R.layout.fragment_gallery_grid);

        take_pic = (ImageButton) findViewById(R.id.take_picture);
        gal_pic = (ImageButton) findViewById(R.id.choose_gallery);
        instagram = (ImageButton) findViewById(R.id.instagram);
        imageView =(ImageView) findViewById(R.id.imageDisplay);
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels-100;
        imageLoader = new ImageLoader(this);
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(width,width);
        rp.topMargin = 50;
        rp.leftMargin = 50;
        imageView.setLayoutParams(rp);
        //imageView.setVisibility(View.INVISIBLE);
        next_page = (Button) findViewById(R.id.next_page);
        //next_page.setVisibility(View.INVISIBLE);
        reselect = (Button) findViewById(R.id.another_pic);
        //reselect.setVisibility(View.INVISIBLE);
        radioGroup = (RadioGroup) findViewById(R.id.level_select);
        //radioGroup.setVisibility(View.INVISIBLE);
        switchToScreen(R.id.image_grid);

        /*
        *Instagram Integration Start
        *  Check for login
         */
        mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
                ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                mApp.fetchUserName(handler);
                switchToScreen(R.id.instagram_grid);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(PictureSelectActivity.this, error, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        if (mApp.hasAccessToken()) {
            mApp.fetchUserName(handler);
        }

        instGridView = (GridView) findViewById(R.id.inst_grid);

        /*
        * Instagram Integration End
        */

        gridview = (GridView) findViewById(R.id.grid);
        final ImageAdapter imageAdapter = new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                switchToScreen(R.id.pic);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),(imageAdapter.getmThumbIds())[position]);
                imageView.setImageBitmap(bitmap);
                imageRes = 0;
                imageView.setTag((imageAdapter.getmThumbIds())[position]);
            }
        });

        take_pic.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                File picsFolder = new File(Constants.EXTERNAL_PATH +Constants.APPLICATION_PATH);
                String file = "pic_"+new Long(System.currentTimeMillis()).toString()+".jpg";
                File newfile = new File(picsFolder,file);
                Log.d(LOG_TAG,newfile.toString());
                try
                {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG, "exception", e);
                }
                imageUri = Uri.fromFile(newfile);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, PIC_CAPTURE_ID);
            }
        });
        gal_pic.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent pickPhoto = Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),"Pick an image");
                startActivityForResult(pickPhoto, 1);
            }
        });
    }


    public void connectOrDisconnectUser(View v) {
        if (mApp.hasAccessToken()) {
            AllMediaFiles allMediaFiles = new AllMediaFiles(userInfoHashmap,instGridView);
            imageThumbList = allMediaFiles.setContext(this);

            switchToScreen(R.id.instagram_grid);
            instGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                    switchToScreen(R.id.pic);
                    Holder holder = new Holder();
                    holder.ivPhoto = imageView;
                    imageLoader.DisplayImage(imageThumbList.get(position), holder.ivPhoto);
                    imageRes = 3;
                    imageView.setTag(imageThumbList.get(position));
                }
            });
        } else {
            mApp.authorize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(LOG_TAG,new Integer(RESULT_OK).toString());
        Log.d(LOG_TAG,"Inside onActivityResult");
        Log.d(LOG_TAG,new Integer(resultCode).toString());
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG,"After super call");
        InputStream inputStream = null;

        if (resultCode == RESULT_OK)
        {
            try
            {
                switch(requestCode)
                {
                    case 0:
                    {
                        Log.d(LOG_TAG,"Inside Picture Captured::" + imageUri);
                        inputStream = getContentResolver().openInputStream(imageUri);
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        imageRes = 1;
                        imageView.setTag(imageUri);
                        break;
                    }
                    case 1:
                    {
                        Log.d(LOG_TAG,"Inside Picture Selected::"+data.getData());
                        inputStream = getContentResolver().openInputStream(data.getData());
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        imageRes = 2;
                        imageView.setTag(data.getData());
                        break;
                    }
                }
                //next_page.setVisibility(View.VISIBLE);
                switchToScreen(R.id.pic);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(19)
    public void onPlay(View view){
        int selected_id = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(selected_id);
        Intent intent = null;

        String[] glevel = radioButton.getText().toString().trim().split("\\s+");
        int level = Integer.parseInt(glevel[0]);

        if(level==3){
            intent = new Intent(getBaseContext(), Game_small.class);
        }
        if(level==4){
            intent = new Intent(getBaseContext(), Game_medium.class);
        }

        intent.putExtra("from",imageRes==0?"grid":imageRes==1?"camera":imageRes==2?"gallery":"inst");
        intent.putExtra("resource",imageView.getTag().toString());
        startActivity(intent);
    }

    public void onReselect(View view){
        switchToScreen(R.id.image_grid);
    }

    void switchToScreen(int screenId) {
        //to switch between screens in home-screen-activity

        for(int id: SCREENS){
            findViewById(id).setVisibility(screenId==id?View.VISIBLE:View.GONE);
        }
    }

    private class Holder {
        private ImageView ivPhoto;
    }
}