package net.wtako.IIDXSPGuide.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;

import java.text.Normalizer;
import java.util.Collection;
import java.util.regex.Pattern;

public class MiscUtils {

    public static String humanList(String separator, Collection<?> collection) {
        StringBuilder builder = new StringBuilder();
        boolean firstDiff = true;
        for (Object ch : collection) {
            if (ch.toString() == null || ch.toString().trim().isEmpty() || ch.toString().equals("-")) {
                continue;
            }
            if (!firstDiff) {
                builder.append(separator);
            }
            builder.append(ch);
            firstDiff = false;
        }
        return builder.toString();
    }

    public static int dpToPx(Context context, int dps) {
        if (context == null) {
            return 1;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static int parseInt(String string, int def) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return def;
        }
    }

    public static int getBrighterColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red += 30;
        green += 30;
        blue += 30;
        red = red > 255 ? 255 : red;
        green = green > 255 ? 255 : green;
        blue = blue > 255 ? 255 : blue;
        return Color.rgb(red, green, blue);
    }


    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static StateListDrawable getPressedColorDrawable(int normalColor, int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(pressedColor));
        states.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(pressedColor));
        states.addState(new int[]{}, new ColorDrawable(normalColor));
        return states;
    }

    public static ColorStateList getPressedColorSelector(int normalColor, int pressedColor) {
        return new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_focused},
                                new int[]{android.R.attr.state_activated},
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor,
                                pressedColor,
                                pressedColor,
                                normalColor
                        }
        );
    }
}
