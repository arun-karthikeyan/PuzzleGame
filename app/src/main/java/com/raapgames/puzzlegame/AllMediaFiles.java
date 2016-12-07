package com.raapgames.puzzlegame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.example.games.basegameutils.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.raapgames.puzzlegame.Constants.LOG_TAG;

public class AllMediaFiles{
    private InstagramSession mSession;
	private GridView gvAllImages;
	private HashMap<String, String> userInfo;
	private ArrayList<String> imageThumbList = new ArrayList<String>();
	private Context context;
    private ImageView imageView;
	private int WHAT_FINALIZE = 0;
	private static int WHAT_ERROR = 1;
	private ProgressDialog pd;
    private ImageLoader imageLoader;
	public static final String TAG_DATA = "data";
	public static final String TAG_IMAGES = "images";
	public static final String TAG_THUMBNAIL = "thumbnail";
    private static final String TAG = "AllMediaFiles";
	public static final String TAG_URL = "url";
	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (pd != null && pd.isShowing())
				pd.dismiss();
			if (msg.what == WHAT_FINALIZE) {
				setImageGridAdapter();
			} else {
				Toast.makeText(context, "Check your network.",
						Toast.LENGTH_SHORT).show();
			}
			return false;
		}
	});


	public AllMediaFiles(HashMap<String, String> userInfo, GridView gv) {
		this.gvAllImages = gv;
		this.userInfo = userInfo;
	}

	public ArrayList<String> setContext(Context context){
        this.context = context;
        imageView = (ImageView) ((Activity)context).findViewById(R.id.imageDisplay);
        imageLoader = new ImageLoader(context);
        mSession = new InstagramSession(context);
        Log.d(LOG_TAG,"Inside setContext");
        getAllMediaImages();
        return imageThumbList;
    }

	private void setImageGridAdapter() {
		gvAllImages.setAdapter(new MyGridListAdapter(context,imageThumbList));
	}

	private void getAllMediaImages() {
        Log.d(LOG_TAG,"Inside getAllMediaImages");
		pd = ProgressDialog.show(context, "", "Loading images...");
		new Thread(new Runnable() {

			@Override
			public void run() {
				int what = WHAT_FINALIZE;
				try {
					// URL url = new URL(mTokenUrl + "&code=" + code);
					JSONParser jsonParser = new JSONParser();

					JSONObject jsonObject = jsonParser
							.getJSONFromUrlByGet("https://api.instagram.com/v1/users/"
									+ userInfo.get(InstagramApp.TAG_ID)
                                        + "/media/recent/?access_token="
									+ mSession.getAccessToken());

					JSONArray data = jsonObject.getJSONArray(TAG_DATA);

					for (int data_i = 0; data_i < data.length(); data_i++) {
						JSONObject data_obj = data.getJSONObject(data_i);

						JSONObject images_obj = data_obj
								.getJSONObject(TAG_IMAGES);

						JSONObject standard_resolution = images_obj
								.getJSONObject(TAG_THUMBNAIL);

						// String str_height =
						// thumbnail_obj.getString(TAG_HEIGHT);
						//
						// String str_width =
						// thumbnail_obj.getString(TAG_WIDTH);

						String str_url = standard_resolution.getString(TAG_URL);
                        Log.d(TAG,str_url);
						imageThumbList.add(str_url);
					}

					System.out.println("jsonObject::" + jsonObject);

				} catch (Exception exception) {
					exception.printStackTrace();
					what = WHAT_ERROR;
				}
				// pd.dismiss();
				handler.sendEmptyMessage(what);
			}
		}).start();
	}
}
