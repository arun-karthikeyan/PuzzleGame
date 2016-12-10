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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

public class Game_medium extends AppCompatActivity {

    private Button[] buttons;
    private TextView moveCounter;
    private Boolean bad_move=false;
    private static final Integer[] goal = new Integer[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    private ArrayList<Integer> cells = new ArrayList<Integer>();
    private Bitmap puzzleImage;
    private static final int gridHeight = 4, gridWidth = 4;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_medium);

        this.imageView = (ImageView) findViewById(R.id.tempImage2);

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

        for(int i=0;i<16;i++)
        {
            this.cells.add(i);
        }
//        Collections.shuffle(this.cells); //random cells array
        customShuffle();

        fill_grid();

        moveCounter = (TextView) findViewById(R.id.mMoveCounter);
        for (int i = 0; i < 15; i++) {
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
//        0 2 15 3 4 1 5 7 12 8 6 10 13 14 9 11
        ArrayList<Integer> tempCells = new ArrayList<Integer>();
        tempCells.add(this.cells.get(0));
        tempCells.add(this.cells.get(2));
        tempCells.add(this.cells.get(15));
        tempCells.add(this.cells.get(3));
        tempCells.add(this.cells.get(4));
        tempCells.add(this.cells.get(1));
        tempCells.add(this.cells.get(5));
        tempCells.add(this.cells.get(7));
        tempCells.add(this.cells.get(12));
        tempCells.add(this.cells.get(8));
        tempCells.add(this.cells.get(6));
        tempCells.add(this.cells.get(10));
        tempCells.add(this.cells.get(13));
        tempCells.add(this.cells.get(14));
        tempCells.add(this.cells.get(9));
        tempCells.add(this.cells.get(11));

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
                Log.d(Constants.LOG_TAG,"x: "+x+", y: "+y);
                imgs[imageNo] = new BitmapDrawable(this.getResources(), Bitmap.createBitmap(this.puzzleImage,x,y,width,height));
            }
        }
        return imgs;
    }

    public Button[] findButtons(){

        Button[] b = new Button[gridWidth*gridHeight];
        BitmapDrawable[] imageParts = splitPuzzleImage();


        b[0] = (Button) findViewById(R.id.mButton01);
        b[1] = (Button) findViewById(R.id.mButton02);
        b[2] = (Button) findViewById(R.id.mButton03);
        b[3] = (Button) findViewById(R.id.mButton04);
        b[4] = (Button) findViewById(R.id.mButton05);
        b[5] = (Button) findViewById(R.id.mButton06);
        b[6] = (Button) findViewById(R.id.mButton07);
        b[7] = (Button) findViewById(R.id.mButton08);
        b[8] = (Button) findViewById(R.id.mButton09);
        b[9] = (Button) findViewById(R.id.mButton10);
        b[10] = (Button) findViewById(R.id.mButton11);
        b[11] = (Button) findViewById(R.id.mButton12);
        b[12] = (Button) findViewById(R.id.mButton13);
        b[13] = (Button) findViewById(R.id.mButton14);
        b[14] = (Button) findViewById(R.id.mButton15);
        b[15] = (Button) findViewById(R.id.mButton16);

        for(int i=0, iLen = (gridHeight*gridWidth)-1; i<iLen; ++i){
            b[i].setBackground(imageParts[i]);
        }
        return b;
    }

    public void fill_grid(){
        for(int i=0;i<16;i++)
        {
            int text=cells.get(i);
            RelativeLayout.LayoutParams absParams = (RelativeLayout.LayoutParams)buttons[text].getLayoutParams();
            switch(i)
            {
                case(0):
                    absParams.leftMargin = 105;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(1):
                    absParams.leftMargin = 405;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(2):
                    absParams.leftMargin = 705;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(3):
                    absParams.leftMargin = 1005;
                    absParams.topMargin = 5;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(4):
                    absParams.leftMargin =105;
                    absParams.topMargin =305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(5):
                    absParams.leftMargin = 405;
                    absParams.topMargin =305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(6):
                    absParams.leftMargin = 705;
                    absParams.topMargin = 305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(7):
                    absParams.leftMargin = 1005;
                    absParams.topMargin = 305;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(8):
                    absParams.leftMargin = 105;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(9):
                    absParams.leftMargin = 405;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(10):
                    absParams.leftMargin = 705;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(11):
                    absParams.leftMargin = 1005;
                    absParams.topMargin = 605;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(12):
                    absParams.leftMargin = 105;
                    absParams.topMargin = 905;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(13):
                    absParams.leftMargin = 405;
                    absParams.topMargin = 905;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(14):
                    absParams.leftMargin = 705;
                    absParams.topMargin = 905;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case(15):
                    absParams.leftMargin = 1005;
                    absParams.topMargin = 905;
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
        zuk_pos=find_pos(15);
        switch(zuk_pos)
        {
            case(0):
                if(b_pos==1||b_pos==4)
                    bad_move=false;
                break;
            case(1):
                if(b_pos==0||b_pos==2||b_pos==5)
                    bad_move=false;
                break;
            case(2):
                if(b_pos==1||b_pos==6||b_pos==3)
                    bad_move=false;
                break;
            case(3):
                if(b_pos==2||b_pos==7)
                    bad_move=false;
                break;
            case(4):
                if(b_pos==0||b_pos==5||b_pos==8)
                    bad_move=false;
                break;
            case(5):
                if(b_pos==1||b_pos==4||b_pos==6||b_pos==9)
                    bad_move=false;
                break;
            case(6):
                if(b_pos==2||b_pos==5||b_pos==7||b_pos==10)
                    bad_move=false;
                break;
            case(7):
                if(b_pos==3||b_pos==6||b_pos==11)
                    bad_move=false;
                break;
            case(8):
                if(b_pos==4||b_pos==9||b_pos==12)
                    bad_move=false;
                break;
            case(9):
                if(b_pos==5||b_pos==8||b_pos==10||b_pos==13)
                    bad_move=false;
                break;
            case(10):
                if(b_pos==6||b_pos==9||b_pos==11||b_pos==14)
                    bad_move=false;
                break;
            case(11):
                if(b_pos==7||b_pos==10||b_pos==15)
                    bad_move=false;
                break;
            case(12):
                if(b_pos==8||b_pos==13)
                    bad_move=false;
                break;
            case(13):
                if(b_pos==14||b_pos==9||b_pos==12)
                    bad_move=false;
                break;
            case(14):
                if(b_pos==10||b_pos==13||b_pos==15)
                    bad_move=false;
                break;
            case(15):
                if(b_pos==11||b_pos==14)
                    bad_move=false;
                break;
        }

        if(bad_move)
        {
            return;
        }
        cells.remove(b_pos);
        cells.add(b_pos, 15);
        cells.remove(zuk_pos);
        cells.add(zuk_pos,b_text);

        fill_grid();
        moveCounter.setText(Integer.toString(Integer.parseInt(moveCounter.getText().toString())+1));

        for(int i=0;i<16;i++)
        {
            if(cells.get(i)!=goal[i])
            {
                return;
            }
        }
        Toast.makeText(Game_medium.this,"we have a winner",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this.getBaseContext(), SuccessActivity.class);
        intent.putExtra("movesMade",moveCounter.getText().toString());
        this.startActivity(intent);
    }

    public int find_pos(int element)
    {
        int i=0;
        for(i=0;i<16;i++)
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
