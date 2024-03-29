package com.raapgames.puzzlegame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.example.games.basegameutils.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

public class Game_small extends AppCompatActivity {

    private Button[] buttons;
    private TextView moveCounter;
    private Boolean bad_move=false;
    private static final Integer[] goal = new Integer[] {0,1,2,3,4,5,6,7,8};
    private ArrayList<Integer> cells = new ArrayList<Integer>();
    private Bitmap puzzleImage;
    private final int gridWidth = 3;
    private final int gridHeight = 3;
    private ImageView imageView;
    private ImageLoader imageLoader;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_small);

        this.imageView = (ImageView) findViewById(R.id.tempImage);
        this.imageLoader = new ImageLoader(this.getApplicationContext());

        try {
            String from = this.getIntent().getStringExtra("from");
            String resource = this.getIntent().getStringExtra("resource");
            if(from.equalsIgnoreCase("inst")){
                new DownloadImageTask(imageView)
                        .execute(resource);
            }else {
                this.puzzleImage = getByteArray();
                initialize();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initialize(){
        buttons = findButtons();

        for(int i=0;i<9;i++){
            this.cells.add(i);
        }
//        Collections.shuffle(this.cells);
        customShuffle();

        fill_grid();

        moveCounter = (TextView) findViewById(R.id.MoveCounter);
        for (int i = 0; i < 8; i++) {
            buttons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    makeMove((Button) v);
                }
            });
        }
    }

    private Bitmap getByteArray() throws Exception{
        Bitmap bitmap = null;
        InputStream inputStream = null;
        String from = this.getIntent().getStringExtra("from");
        String resource = this.getIntent().getStringExtra("resource");
        int level = 3;

        if(from.equalsIgnoreCase("grid")){
            bitmap = BitmapFactory.decodeResource(getResources(),Integer.parseInt(resource));
        }
        else if(from.equalsIgnoreCase("inst")){
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        }
        else{
            inputStream = getContentResolver().openInputStream(Uri.parse(resource));
            bitmap = BitmapFactory.decodeStream(inputStream);
        }

        int newHeight = (bitmap.getHeight()%level)==0?bitmap.getHeight():((bitmap.getHeight()/level)*level);
        int newWidth = (bitmap.getWidth()%level)==0?bitmap.getWidth():((bitmap.getWidth()/level)*level);
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Log.d(LOG_TAG,"OldHeight: "+bitmap.getHeight()+", OldWidth: "+bitmap.getWidth()+" ; NewHeight: "+croppedBitmap.getHeight()+", NewWidth: "+croppedBitmap.getWidth());
        return croppedBitmap;
    }

    private void customShuffle(){
        ArrayList<Integer> tempCells = new ArrayList<Integer>();
//        0 8 2 6 1 4 7 3 5
        tempCells.add(this.cells.get(0));
        tempCells.add(this.cells.get(8));
        tempCells.add(this.cells.get(2));
        tempCells.add(this.cells.get(6));
        tempCells.add(this.cells.get(1));
        tempCells.add(this.cells.get(4));
        tempCells.add(this.cells.get(7));
        tempCells.add(this.cells.get(3));
        tempCells.add(this.cells.get(5));
        this.cells = tempCells;
    }
    private BitmapDrawable[] splitPuzzleImage(){

        BitmapDrawable[] imgs = new BitmapDrawable[gridWidth*gridHeight];
        int height = this.puzzleImage.getHeight()/gridHeight;
        int width = this.puzzleImage.getWidth()/gridWidth;

        for(int i=0, imageNo = 0; i<gridHeight; ++i){
            for(int j=0; j<gridWidth; ++j, imageNo++){
                int x = j*(this.puzzleImage.getWidth()/gridWidth);
                int y = i*(this.puzzleImage.getHeight()/gridHeight);
                Log.d(LOG_TAG,"x: "+x+", y: "+y);
                imgs[imageNo] = new BitmapDrawable(this.getResources(), Bitmap.createBitmap(this.puzzleImage,x,y,width,height));
            }
        }
        return imgs;
    }

    public Button[] findButtons(){
        Button[] b = new Button[gridWidth*gridHeight];
        BitmapDrawable[] imageParts = splitPuzzleImage();
        b[0] = (Button) findViewById(R.id.Button01);
        b[0].setBackground(imageParts[0]);

        b[1] = (Button) findViewById(R.id.Button02);
        b[1].setBackground(imageParts[1]);

        b[2] = (Button) findViewById(R.id.Button03);
        b[2].setBackground(imageParts[2]);

        b[3] = (Button) findViewById(R.id.Button04);
        b[3].setBackground(imageParts[3]);

        b[4] = (Button) findViewById(R.id.Button05);
        b[4].setBackground(imageParts[4]);

        b[5] = (Button) findViewById(R.id.Button06);
        b[5].setBackground(imageParts[5]);

        b[6] = (Button) findViewById(R.id.Button07);
        b[6].setBackground(imageParts[6]);

        b[7] = (Button) findViewById(R.id.Button08);
        b[7].setBackground(imageParts[7]);

        b[8] = (Button) findViewById(R.id.Button09);
//        b[8].setBackground(imageParts[8]);


        return b;
    }


    public void fill_grid(){
        for(int i=0;i<9;i++)
        {
            int text=cells.get(i);
            RelativeLayout.LayoutParams absParams = (RelativeLayout.LayoutParams)buttons[text].getLayoutParams();
            switch(i)
            {
                case(0):
                    absParams.leftMargin = 255;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(1):
                    absParams.leftMargin = 555;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(2):
                    absParams.leftMargin = 855;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(3):
                    absParams.leftMargin = 255;
                    absParams.topMargin = 305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(4):
                    absParams.leftMargin =555;
                    absParams.topMargin =305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(5):
                    absParams.leftMargin = 855;
                    absParams.topMargin =305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(6):
                    absParams.leftMargin = 255;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(7):
                    absParams.leftMargin = 555;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(8):
                    absParams.leftMargin = 855;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
            }
        }
    }

    public void makeMove(final Button b){
        bad_move=true;
        int b_text,b_pos,zuk_pos;
        b_text=Integer.parseInt((String) b.getText())-1;
        b_pos=find_pos(b_text);
        zuk_pos=find_pos(8);
        switch(zuk_pos)
        {
            case(0):
                if(b_pos==1||b_pos==3)
                    bad_move=false;
                break;
            case(1):
                if(b_pos==0||b_pos==2||b_pos==4)
                    bad_move=false;
                break;
            case(2):
                if(b_pos==1||b_pos==5)
                    bad_move=false;
                break;
            case(3):
                if(b_pos==0||b_pos==4||b_pos==6)
                    bad_move=false;
                break;
            case(4):
                if(b_pos==1||b_pos==3||b_pos==5||b_pos==7)
                    bad_move=false;
                break;
            case(5):
                if(b_pos==2||b_pos==4||b_pos==8)
                    bad_move=false;
                break;
            case(6):
                if(b_pos==3||b_pos==7)
                    bad_move=false;
                break;
            case(7):
                if(b_pos==4||b_pos==6||b_pos==8)
                    bad_move=false;
                break;
            case(8):
                if(b_pos==5||b_pos==7)
                    bad_move=false;
                break;
        }

        if(bad_move)
        {
            return;
        }
        cells.remove(b_pos);
        cells.add(b_pos, 8);
        cells.remove(zuk_pos);
        cells.add(zuk_pos,b_text);

        fill_grid();
        moveCounter.setText(Integer.toString(Integer.parseInt(moveCounter.getText().toString())+1));

        for(int i=0;i<9;i++)
        {
            if(cells.get(i)!=goal[i])
            {
                return;
            }
        }
        Toast.makeText(Game_small.this,"we have a winner",Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this.getBaseContext(), SuccessActivity.class);
        intent.putExtra("movesMade",moveCounter.getText().toString());
        this.startActivity(intent);
    }

    public int find_pos(int element)
    {
        int i=0;
        for(i=0;i<9;i++)
        {
            if(cells.get(i)==element)
            {
                break;
            }
        }
        return i;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            try {
                puzzleImage = getByteArray();
                initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
