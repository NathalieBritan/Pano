package org.cyanogenmod.focal;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.getkeepsafe.relinker.ReLinker;

import org.cyanogenmod.focal.common.helpers.Cockroach;
import org.cyanogenmod.focal.managers.CameraManager;

/**
 * Manages the application itself (on top of the activity), mainly to force Camera getting
 * closed in case of crash.
 */
public class CameraApplication extends Application {
    private final static String TAG = "FocalApp";
    private Thread.UncaughtExceptionHandler mDefaultExHandler;
    private CameraManager mCamManager;

    private Thread.UncaughtExceptionHandler mExHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            if (mCamManager != null) {
                Log.e(TAG, "Uncaught exception! Closing down camera safely firsthand");
                mCamManager.forceCloseCamera();
            }

            mDefaultExHandler.uncaughtException(thread, ex);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        Cockroach.install(new Cockroach.ExceptionHandler() {

            // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException

            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("AndroidRuntime","--->CockroachException:"+thread+"<---",throwable);
                            Toast.makeText(CameraApplication.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException("..."+(i++));
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });

        ReLinker.log(logcatLogger).force().recursively().loadLibrary(this, "jni_mosaic2", new ReLinker.LoadListener() {
            @Override
            public void success() {
                Log.e("ReLinker", "success");
            }

            @Override
            public void failure(Throwable t) {
                Log.e("ReLinker", "failed");
                t.printStackTrace();
            }
        });


        mDefaultExHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mExHandler);
    }

    private ReLinker.Logger logcatLogger = new ReLinker.Logger() {
        @Override
        public void log(String message) {
            Log.d("ReLinker", message);
        }
    };

    public void setCameraManager(CameraManager camMan) {
        mCamManager = camMan;
    }


}
