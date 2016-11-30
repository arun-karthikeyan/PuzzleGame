package com.raapgames.puzzlegame;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by anandh on 11/17/16.
 */

public class PictureSelectActivity extends AppCompatActivity
{
    private Button take_pic;
    private Button gal_pic;
    private Button next_page;
    private Spinner game_level;
    private ImageView imageView;
    private Uri imageUri;
    private final int PIC_CAPTURE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_select_layout);

        take_pic = (Button) findViewById(R.id.take_picture);
        gal_pic = (Button) findViewById(R.id.choose_gallery);
        imageView =(ImageView) findViewById(R.id.imageDisplay);
        next_page = (Button) findViewById(R.id.next_page);
        next_page.setVisibility(View.INVISIBLE);
        game_level = (Spinner) findViewById(R.id.spinner);
        game_level.setVisibility(View.INVISIBLE);

        take_pic.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                File picsFolder = new File(Constants.EXTERNAL_PATH +Constants.APPLICATION_PATH);
                String file = "pic_"+new Long(System.currentTimeMillis()).toString()+".jpg";
                File newfile = new File(picsFolder,file);
                Log.d(Constants.LOG_TAG,newfile.toString());
                try
                {
                    newfile.createNewFile();
                }
                catch (IOException e)
                {
                    Log.e(Constants.LOG_TAG, "exception", e);
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
        Log.d(Constants.LOG_TAG,new Integer(RESULT_OK).toString());
        Log.d(Constants.LOG_TAG,"Inside onActivityResult");
        Log.d(Constants.LOG_TAG,new Integer(resultCode).toString());
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Constants.LOG_TAG,"After super call");
        InputStream inputStream = null;
        if (resultCode == RESULT_OK)
        {
            try
            {
                switch(requestCode)
                {
                    case 0:
                    {
                        Log.d(Constants.LOG_TAG,"Inside Picture Captured");
                        inputStream = getContentResolver().openInputStream(imageUri);
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        break;
                    }
                    case 1:
                    {
                        Log.d(Constants.LOG_TAG,"Inside Picture Selected");
                        inputStream = getContentResolver().openInputStream(data.getData());
                        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                        /*
                            Image split logic
                         */

                        break;
                    }
                }
                next_page.setVisibility(View.VISIBLE);
                game_level.setVisibility(View.VISIBLE);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void onPlay(View view){

        String[] glevel = game_level.getSelectedItem().toString().trim().split("\\s+");
        int level = Integer.parseInt(glevel[0]);

        if(level==3){
            Intent intent = new Intent(getBaseContext(), Game_small.class);
            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            int newHeight = (bitmap.getHeight()%level)==0?bitmap.getHeight():((bitmap.getHeight()/level)*level);
            int newWidth = (bitmap.getWidth()%level)==0?bitmap.getWidth():((bitmap.getWidth()/level)*level);
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Log.d(Constants.LOG_TAG,"OldHeight: "+bitmap.getHeight()+", OldWidth: "+bitmap.getWidth()+" ; NewHeight: "+croppedBitmap.getHeight()+", NewWidth: "+croppedBitmap.getWidth());
            byte[] byteArray = stream.toByteArray();
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
            Log.d(Constants.LOG_TAG,"OldHeight: "+bitmap.getHeight()+", OldWidth: "+bitmap.getWidth()+" ; NewHeight: "+croppedBitmap.getHeight()+", NewWidth: "+croppedBitmap.getWidth());
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("image",byteArray);
            startActivity(intent);
        }
    }
}