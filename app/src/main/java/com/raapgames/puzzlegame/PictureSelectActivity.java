package com.raapgames.puzzlegame;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

/**
 * Created by anandh on 11/17/16.
 */

public class PictureSelectActivity extends AppCompatActivity
{
    private GridView gridview;
    private ImageButton take_pic;
    private ImageButton gal_pic;
    private Button next_page;
    private Button reselect;
    private ImageView imageView;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Uri imageUri;
    private final int PIC_CAPTURE_ID = 0;
    final static int[] SCREENS = {
            R.id.image_grid,R.id.pic
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.pic_select_layout);
        setContentView(R.layout.fragment_gallery_grid);

        take_pic = (ImageButton) findViewById(R.id.take_picture);
        gal_pic = (ImageButton) findViewById(R.id.choose_gallery);
        imageView =(ImageView) findViewById(R.id.imageDisplay);
        //imageView.setVisibility(View.INVISIBLE);
        next_page = (Button) findViewById(R.id.next_page);
        //next_page.setVisibility(View.INVISIBLE);
        reselect = (Button) findViewById(R.id.another_pic);
        //reselect.setVisibility(View.INVISIBLE);
        radioGroup = (RadioGroup) findViewById(R.id.level_select);
        //radioGroup.setVisibility(View.INVISIBLE);
        switchToScreen(R.id.image_grid);

        gridview = (GridView) findViewById(R.id.grid);
        final ImageAdapter imageAdapter = new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                switchToScreen(R.id.pic);

                int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels-100;
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(width,width);
                rp.topMargin = 50;
                rp.leftMargin = 50;
                imageView.setLayoutParams(rp);
                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),(imageAdapter.getmThumbIds())[position]));
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
                        Log.d(LOG_TAG,"Inside Picture Captured");
                        inputStream = getContentResolver().openInputStream(imageUri);
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        break;
                    }
                    case 1:
                    {
                        Log.d(LOG_TAG,"Inside Picture Selected");
                        inputStream = getContentResolver().openInputStream(data.getData());
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        /*
                            Image split logic
                         */

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

        String[] glevel = radioButton.getText().toString().trim().split("\\s+");
        int level = Integer.parseInt(glevel[0]);

        if(level==3){
            Intent intent = new Intent(getBaseContext(), Game_small.class);
            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//            Log.d(LOG_TAG, String.valueOf(bitmap.getAllocationByteCount()));
            int newHeight = (bitmap.getHeight()%level)==0?bitmap.getHeight():((bitmap.getHeight()/level)*level);
            int newWidth = (bitmap.getWidth()%level)==0?bitmap.getWidth():((bitmap.getWidth()/level)*level);
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Log.d(LOG_TAG,"OldHeight: "+bitmap.getHeight()+", OldWidth: "+bitmap.getWidth()+" ; NewHeight: "+croppedBitmap.getHeight()+", NewWidth: "+croppedBitmap.getWidth());
            byte[] byteArray = stream.toByteArray();
            Log.d(LOG_TAG,"Image byte array Length : "+byteArray.length);
            intent.putExtra("image",byteArray);
            startActivity(intent);
        }
        if(level==4){
            Intent intent = new Intent(getBaseContext(), Game_medium.class);
            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            int newHeight = (bitmap.getHeight()%level)==0?bitmap.getHeight():((bitmap.getHeight()/level)*level);
            int newWidth = (bitmap.getWidth()%level)==0?bitmap.getWidth():((bitmap.getWidth()/level)*level);
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Log.d(LOG_TAG,"OldHeight: "+bitmap.getHeight()+", OldWidth: "+bitmap.getWidth()+" ; NewHeight: "+croppedBitmap.getHeight()+", NewWidth: "+croppedBitmap.getWidth());
            byte[] byteArray = stream.toByteArray();
            Log.d(LOG_TAG,"Image byte array Length : "+byteArray.length);
            intent.putExtra("image",byteArray);
            startActivity(intent);
        }
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
}