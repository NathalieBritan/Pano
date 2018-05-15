package com.facebook.rethinkvision.Bimostitch;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.content.FileProvider;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewCompat;
import android.widget.ImageView;

import org.cyanogenmod.focal.Bimostitch.Constants;
import org.cyanogenmod.focal.CameraActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BitmapHelper {

    public static class AsyncDrawable extends ColorDrawable {
        private final WeakReference<Object> tag;

        public AsyncDrawable(Object tag) {
            super(ViewCompat.MEASURED_STATE_MASK);
            this.tag = new WeakReference(tag);
        }

        public Object getTagFromDrawable() {
            return this.tag.get();
        }
    }

    public interface BitmapCacheInterface {
        void addBitmapToMemoryCache(String str, Bitmap bitmap);

        Bitmap getBitmapFromMemCache(String str);
    }

    public interface OnBitmapLoadListener {
        void onBitmapLoaded(Object obj, Bitmap bitmap);
    }

    public static class ThumbNailLoaderTask extends AsyncTask<Integer, Void, Bitmap> {
        private BitmapCacheInterface cacheInterface;
        public int desired_dimension;
        public int id = -1;
        public String key;
        private int kind;
        private OnBitmapLoadListener onBitmapLoadListener;
        public String path;
        private ContentResolver resolver;
        public WeakReference<?> weakReference;

        public ThumbNailLoaderTask(Context context, WeakReference<?> weakReference, int kind, String path, OnBitmapLoadListener onBitmapLoadListener, BitmapCacheInterface cacheInterface, int desired_dimension) {
            this.kind = kind;
            this.weakReference = weakReference;
            this.onBitmapLoadListener = onBitmapLoadListener;
            this.cacheInterface = cacheInterface;
            this.resolver = context.getContentResolver();
            this.path = path;
            this.desired_dimension = desired_dimension;
        }

        public ThumbNailLoaderTask(Context context, WeakReference<?> weakReference, int kind, OnBitmapLoadListener onBitmapLoadListener, BitmapCacheInterface cacheInterface) {
            this.kind = kind;
            this.weakReference = weakReference;
            this.onBitmapLoadListener = onBitmapLoadListener;
            this.cacheInterface = cacheInterface;
            this.resolver = context.getContentResolver();
            this.path = null;
            this.desired_dimension = -1;
        }

        protected Bitmap doInBackground(Integer... arg0) {
            if (isCancelled()) {
                return null;
            }
            Bitmap temp = Thumbnails.getThumbnail(this.resolver, (long) arg0[0].intValue(), this.kind, null);
            if (temp == null) {
                return null;
            }
            int orientation = BitmapHelper.getOrientation(this.path);
            Matrix matrix = new Matrix();
            if (this.desired_dimension > 0) {
                float scale;
                if (temp.getWidth() < temp.getHeight()) {
                    scale = ((float) this.desired_dimension) / ((float) temp.getWidth());
                } else {
                    scale = ((float) this.desired_dimension) / ((float) temp.getHeight());
                }
                if (((double) scale) < 1.0d) {
                    matrix.postScale(scale, scale);
                }
            }
            if (orientation != 0) {
                matrix.postRotate((float) orientation);
            }
            Bitmap bitmap = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
            if (bitmap != temp) {
                temp.recycle();
            }
            if (isCancelled()) {
                bitmap.recycle();
                return null;
            } else if (this.cacheInterface == null || bitmap == null) {
                return bitmap;
            } else {
                this.cacheInterface.addBitmapToMemoryCache(this.key, bitmap);
                return bitmap;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap.recycle();
            } else if (this.onBitmapLoadListener != null) {
                this.onBitmapLoadListener.onBitmapLoaded(this, bitmap);
            }
        }
    }

    static class C05851 implements OnBitmapLoadListener {
        C05851() {
        }

        public void onBitmapLoaded(Object sender, Bitmap bitmap) {
            if ((sender instanceof ThumbNailLoaderTask) && bitmap != null) {
                ThumbNailLoaderTask worker = (ThumbNailLoaderTask) sender;
                ImageView imageView = (ImageView) worker.weakReference.get();
                if (imageView != null && worker.id == imageView.getId()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class C05862 implements OnBitmapLoadListener {
        C05862() {
        }

        public void onBitmapLoaded(Object sender, Bitmap bitmap) {
            if ((sender instanceof ThumbNailLoaderTask) && bitmap != null) {
                ThumbNailLoaderTask worker = (ThumbNailLoaderTask) sender;
                ImageView imageView = (ImageView) worker.weakReference.get();
                if (imageView != null && worker.id == imageView.getId()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static class BitmapCache implements BitmapCacheInterface {
        LruCache<String, Bitmap> mMemoryCache;

        public BitmapCache(Context context) {
            RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(((Activity) context).getFragmentManager());
            if (mRetainFragment != null) {
                this.mMemoryCache = mRetainFragment.mRetainedCache;
            } else {
                this.mMemoryCache = null;
            }
            if (this.mMemoryCache == null && mRetainFragment != null) {
                LruCache c05871 = new LruCache<String, Bitmap>(((int) (Runtime.getRuntime().maxMemory() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) / 16) {
                    protected int sizeOf(String key, Bitmap bitmap) {
                        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
                    }
                };
                this.mMemoryCache = c05871;
                mRetainFragment.mRetainedCache = c05871;
            }
        }

        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            synchronized (this.mMemoryCache) {
                if (this.mMemoryCache.get(key) == null) {
                    this.mMemoryCache.put(key, bitmap);
                }
            }
        }

        public Bitmap getBitmapFromMemCache(String key) {
            Bitmap bitmap;
            synchronized (this.mMemoryCache) {
                bitmap = (Bitmap) this.mMemoryCache.get(key);
            }
            return bitmap;
        }

        public boolean removeItemFromCache(String key) {
            boolean z;
            synchronized (this.mMemoryCache) {
                z = this.mMemoryCache.remove(key) != null;
            }
            return z;
        }
    }

    public static String getOutputName(String albumName) {
        File mediaStorageDir = CameraActivity.getAlbumStorageDir(albumName);
        ArrayList<String> file_paths = getFilePathsFromSdcard(albumName);
        if (mediaStorageDir == null) {
            return null;
        }
        int count = file_paths.size() + 1;
        String file_name = mediaStorageDir.getPath() + File.separator + "bimostitch_pano_" + count + ".jpg";
        File file = new File(file_name);
        while (file.exists()) {
            count++;
            file_name = mediaStorageDir.getPath() + File.separator + "bimostitch_pano_" + count + ".jpg";
            file = new File(file_name);
        }
        return file_name;
    }

    public static String getOutputImageFile(Context context, String albumName) {
        File mediaStorageDir = CameraActivity.getPrivateStorageDir(context, albumName);
        ArrayList<String> file_paths = getFilePathsFromSdcard(albumName);
        if (mediaStorageDir == null) {
            return null;
        }
        int count = file_paths.size() + 1;
        String file_name = mediaStorageDir.getPath() + File.separator + "pic" + count + ".jpg";
        File file = new File(file_name);
        while (file.exists()) {
            count++;
            file_name = mediaStorageDir.getPath() + File.separator + "pic" + count + ".jpg";
            file = new File(file_name);
        }
        return file_name;
    }

    public static int get_bitmap_width(String key) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(key, options);
        int orientation = getOrientation(key);
        if (orientation == 90 || orientation == 270) {
            return options.outHeight;
        }
        return options.outWidth;
    }

    public static int get_bitmap_height(String key) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(key, options);
        int orientation = getOrientation(key);
        if (orientation == 90 || orientation == 270) {
            return options.outWidth;
        }
        return options.outHeight;
    }

    public static void saveBitmap(Bitmap bitmap, String path, int quality) {
        try {
            FileOutputStream out_stream = new FileOutputStream(new File(path));
            bitmap.compress(CompressFormat.JPEG, quality, out_stream);
            out_stream.close();
        } catch (Exception e) {
        }
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight, int orientation) {
        int height = options.outHeight;
        int width = options.outWidth;
        if (orientation == 90 || orientation == 270) {
            int t = height;
            height = width;
            width = t;
        }
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static int getOrientation(String filePath) {
        if (filePath == null) {
            return 0;
        }
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filePath);
        } catch (IOException e) {
        }
        String orientString = exif != null ? exif.getAttribute("Orientation") : null;
        int orientation = orientString != null ? Integer.parseInt(orientString) : 1;
        int rotationAngle = 0;
        if (orientation == 6) {
            rotationAngle = 90;
        }
        if (orientation == 3) {
            rotationAngle = 180;
        }
        if (orientation == 8) {
            return 270;
        }
        return rotationAngle;
    }

    public static Bitmap decodeSampledBitmap(String key, int reqWidth, int reqHeight, Config preferredConfig) {
        int orientation = getOrientation(key);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(key, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, orientation);
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        options.inPreferredConfig = preferredConfig;
        Bitmap bmp = BitmapFactory.decodeFile(key, options);
        if (orientation == 0) {
            return bmp;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate((float) orientation);
        Bitmap result = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp.recycle();
        return result;
    }

    public static Bitmap decodeSampledBitmap(String key, int reqWidth, int reqHeight) {
        int orientation = getOrientation(key);
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(key, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, orientation);
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        options.inPreferredConfig = Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(key, options);
        if (orientation == 0) {
            return bmp;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate((float) orientation);
        Bitmap result = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp.recycle();
        return result;
    }

    public static boolean cancelPotentialWork(String key, ImageView imageView) {
        ThumbNailLoaderTask tag = (ThumbNailLoaderTask) getTagFromDrawable(imageView);
        if (!(tag instanceof ThumbNailLoaderTask)) {
            return true;
        }
        ThumbNailLoaderTask bitmapWorkerTask = tag;
        if (bitmapWorkerTask == null) {
            return true;
        }
        if (key.equals(bitmapWorkerTask.key)) {
            return false;
        }
        bitmapWorkerTask.cancel(true);
        return true;
    }

    private static Object getTagFromDrawable(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).getTagFromDrawable();
            }
        }
        return null;
    }

    public static String getNameFromPath(String path) {
        if (path != null) {
            return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
        }
        return null;
    }

    public static String getLastDateModified(String path, Context context) {
        File file = new File(path);
        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        Date date = new Date(file.lastModified());
        return dateFormat.format(date) + " " + android.text.format.DateFormat.format("hh:mm:ss a", date);
    }

    public static void loadThumbNail(Context context, int id, ImageView imageView, int kind, String key) {
        if (cancelPotentialWork(key, imageView)) {
            BitmapCacheInterface cacheInterface = new BitmapCache(context);
            Bitmap bitmap = null;
            imageView.setId(id);
            if (cacheInterface != null) {
                bitmap = cacheInterface.getBitmapFromMemCache(key);
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
            ThumbNailLoaderTask task = new ThumbNailLoaderTask(context, new WeakReference(imageView), kind, new C05851(), cacheInterface);
            task.key = new String(key);
            task.id = id;
            imageView.setImageDrawable(new AsyncDrawable(task));
            task.execute(new Integer[]{Integer.valueOf(id)});
        }
    }

    public static void loadThumbNail(Context context, int id, ImageView imageView, int kind, String path, String key, int desired_dimension) {
        if (cancelPotentialWork(key, imageView)) {
            BitmapCache cacheInterface = new BitmapCache(context);
            Bitmap bitmap = null;
            imageView.setId(id);
            if (cacheInterface != null) {
                bitmap = cacheInterface.getBitmapFromMemCache(key);
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
            ThumbNailLoaderTask task = new ThumbNailLoaderTask(context, new WeakReference(imageView), kind, path, new C05862(), cacheInterface, desired_dimension);
            task.key = new String(key);
            task.id = id;
            imageView.setImageDrawable(new AsyncDrawable(task));
            task.execute(new Integer[]{Integer.valueOf(id)});
        }
    }

    private static ArrayList<String> getFilePathsFromSdcard(String albumName) {
        ArrayList<String> paths = new ArrayList();
        for (File f : CameraActivity.getAlbumStorageDir(albumName).listFiles()) {
            paths.add(f.getName());
        }
        return paths;
    }

    public static void copyExifData(String src_path, String dst_path) {
        ExifInterface oldexif = null;
        ExifInterface newexif = null;
        try {
            ExifInterface oldexif2 = new ExifInterface(src_path);
            try {
                newexif = new ExifInterface(dst_path);
                oldexif = oldexif2;
            } catch (IOException e) {
                oldexif = oldexif2;
            }
        } catch (IOException e2) {
        }
        int build = VERSION.SDK_INT;
        if (build >= 9) {
            if (oldexif.getAttribute("GPSAltitude") != null) {
                newexif.setAttribute("GPSAltitude", oldexif.getAttribute("GPSAltitude"));
            }
            if (oldexif.getAttribute("GPSAltitudeRef") != null) {
                newexif.setAttribute("GPSAltitudeRef", oldexif.getAttribute("GPSAltitudeRef"));
            }
        }
        if (build >= 8) {
            if (oldexif.getAttribute("GPSDateStamp") != null) {
                newexif.setAttribute("GPSDateStamp", oldexif.getAttribute("GPSDateStamp"));
            }
            if (oldexif.getAttribute("GPSProcessingMethod") != null) {
                newexif.setAttribute("GPSProcessingMethod", oldexif.getAttribute("GPSProcessingMethod"));
            }
            if (oldexif.getAttribute("GPSTimeStamp") != null) {
                newexif.setAttribute("GPSTimeStamp", "" + oldexif.getAttribute("GPSTimeStamp"));
            }
        }
        if (oldexif.getAttribute("DateTime") != null) {
            newexif.setAttribute("DateTime", oldexif.getAttribute("DateTime"));
        }
        if (oldexif.getAttribute("GPSLatitude") != null) {
            newexif.setAttribute("GPSLatitude", oldexif.getAttribute("GPSLatitude"));
        }
        if (oldexif.getAttribute("GPSLatitudeRef") != null) {
            newexif.setAttribute("GPSLatitudeRef", oldexif.getAttribute("GPSLatitudeRef"));
        }
        if (oldexif.getAttribute("GPSLongitude") != null) {
            newexif.setAttribute("GPSLongitude", oldexif.getAttribute("GPSLongitude"));
        }
        if (oldexif.getAttribute("GPSLatitudeRef") != null) {
            newexif.setAttribute("GPSLongitudeRef", oldexif.getAttribute("GPSLongitudeRef"));
        }
        if (oldexif.getAttribute("Make") != null) {
            newexif.setAttribute("Make", oldexif.getAttribute("Make"));
        }
        if (oldexif.getAttribute("Model") != null) {
            newexif.setAttribute("Model", oldexif.getAttribute("Model"));
        }
        try {
            newexif.saveAttributes();
        } catch (IOException e3) {
        }
    }

    public static Uri getURI(Context context, File image_file) {
        return FileProvider.getUriForFile(context, Constants.FILE_PROVIDER, image_file);
    }
}
