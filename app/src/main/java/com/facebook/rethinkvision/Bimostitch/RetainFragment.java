package com.facebook.rethinkvision.Bimostitch;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;


import android.support.v4.util.LruCache;
import java.util.LinkedList;
import java.util.List;

public class RetainFragment extends Fragment {
    public static final String TAG = RetainFragment.class.getName();
    public List<Runnable> mPendingPreviewCallbacks = new LinkedList();
    public LruCache<String, Bitmap> mRetainedCache;

    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
        if (fragment != null) {
            return fragment;
        }
        Fragment fragment2 = new RetainFragment();
        fm.beginTransaction().add(fragment2, TAG).commit();
        return (RetainFragment) fragment2;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
