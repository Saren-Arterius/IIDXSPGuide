package net.wtako.IIDXSPGuide.utils;

import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;

public class LollipopUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static RippleDrawable getPressedColorRippleDrawable(int normalColor, int pressedColor) {
        return new RippleDrawable(MiscUtils.getPressedColorSelector(normalColor, pressedColor), new ColorDrawable(normalColor), null);
    }

}
