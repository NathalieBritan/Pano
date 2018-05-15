package org.cyanogenmod.focal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;

import fr.xplod.focal.R;

/**
 * Created by Administrator on 2017/4/16.
 */
public class ImageAct extends Activity {

    BigImageView mBigImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BigImageViewer.initialize(GlideImageLoader.with(this));
        setContentView(R.layout.activity_big_img);
        mBigImageView = (BigImageView) findViewById(R.id.mBigImage);
        String pic = getIntent().getStringExtra("pic");
        if(!pic.equals("")){
            mBigImageView.showImage(Uri.parse("file:///" + pic));
        }
        int id = getIntent().getIntExtra("id", 0);
        if (id == 0)
            mBigImageView.showImage(Uri.parse("file:///" + getRecentlyPhotoPath(this)));
        else
            mBigImageView.showImage(Uri.parse("file:///" + getRecentlyPhotoPath(this,id)));
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onDestroy();
        }
    };

    @Override
    public void finish() {
        super.finish();
        startActivity(new Intent(this, CameraActivity.class));
        mHandler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public static String getRecentlyPhotoPath(Context context) {

        String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/DCIM/" + "%' ";
        Uri uri = MediaStore.Files.getContentUri("external");

        Cursor cursor = context.getContentResolver().query(
                uri, new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
        String filePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return filePath;
    }

    public static String getRecentlyPhotoPath(Context context,int id) {

        String searchPath = MediaStore.Files.FileColumns._ID + " = "+id+" ";
        Uri uri = MediaStore.Files.getContentUri("external");

        Cursor cursor = context.getContentResolver().query(
                uri, new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
        String filePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return filePath;
    }
}
