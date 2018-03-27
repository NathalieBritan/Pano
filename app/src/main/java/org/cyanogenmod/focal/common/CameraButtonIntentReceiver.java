package org.cyanogenmod.focal.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.cyanogenmod.focal.CameraActivity;

/**
 * {@code CameraButtonIntentReceiver} is invoked when the camera button is
 * long-pressed.
 *
 * It is declared in {@code AndroidManifest.xml} to receive the
 * {@code android.intent.action.CAMERA_BUTTON} intent.
 *
 */
public class CameraButtonIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(context, CameraActivity.class);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}
