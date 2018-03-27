package org.cyanogenmod.focal.common.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.util.HashMap;
import java.util.Map;

/**
 * This class renders a bitmap with a certain effect
 * and cache it for future use.
 */
public class BitmapFilter {
    private static BitmapFilter mSingleton;

    private Map<String, Bitmap> mGlowCache;
    private RenderScript mRS;
    private Allocation mBlurInputAllocation;
    private Allocation mBlurOutputAllocation;
    private ScriptIntrinsicBlur mBlurScript;

    public static BitmapFilter getSingleton() {
        if (mSingleton == null) {
            mSingleton = new BitmapFilter();
        }

        return mSingleton;
    }

    private BitmapFilter() {
        mGlowCache = new HashMap<String, Bitmap>();
    }

    /**
     * Blurs a bitmap. There's no caching on this one.
     *
     * @param src The bitmap to blur
     * @return A blurred bitmap
     */
    public Bitmap getBlur(Context context, Bitmap src, float radius) {
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            if (mRS == null) {
                mRS = RenderScript.create(context);
            }

            if (mBlurScript == null) {
                mBlurScript = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
            }

            if (mBlurInputAllocation == null) {
                mBlurInputAllocation = Allocation.createFromBitmap(mRS, src,
                        Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_GRAPHICS_TEXTURE);
                mBlurScript.setInput(mBlurInputAllocation);
            } else {
                mBlurInputAllocation.copyFrom(src);
            }

            if (mBlurOutputAllocation == null) {
                mBlurOutputAllocation = Allocation.createTyped(mRS, mBlurInputAllocation.getType());
            }

            mBlurScript.setRadius(radius);
            mBlurScript.forEach(mBlurOutputAllocation);
            mBlurOutputAllocation.copyTo(src);
        }

        return src;
    }

    /**
     * Returns a glowed image of the provided icon. If the
     * provided name is already in the cache, the cached image
     * will be returned. Otherwise, the bitmap will be glowed and
     * cached under the provided name
     *
     * @param name The name of the bitmap
     * @param src  The bitmap of the icon itself
     * @return Glowed bitmap
     */
    public Bitmap getGlow(String name, int glowColor, Bitmap src) {
        if (mGlowCache.containsKey(name)) {
            return mGlowCache.get(name);
        } else {
            // An added margin to the initial image
            int margin = 0;
            int halfMargin = margin / 2;

            // The glow radius
            int glowRadius = 4;

            // Extract the alpha from the source image
            Bitmap alpha = src.extractAlpha();

            // The output bitmap (with the icon + glow)
            Bitmap bmp = Bitmap.createBitmap(src.getWidth() + margin,
                    src.getHeight() + margin, Bitmap.Config.ARGB_8888);

            // The canvas to paint on the image
            Canvas canvas = new Canvas(bmp);

            Paint paint = new Paint();
            paint.setColor(glowColor);

            // Outer glow
            ColorFilter emphasize = new LightingColorFilter(glowColor, 1);
            paint.setColorFilter(emphasize);
            canvas.drawBitmap(src, halfMargin, halfMargin, paint);
            paint.setColorFilter(null);
            paint.setMaskFilter(new BlurMaskFilter(glowRadius, Blur.OUTER));
            canvas.drawBitmap(alpha, halfMargin, halfMargin, paint);

            // Cache icon
            mGlowCache.put(name, bmp);

            return bmp;
        }
    }
}
