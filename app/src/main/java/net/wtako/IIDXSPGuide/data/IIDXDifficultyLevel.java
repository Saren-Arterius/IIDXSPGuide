package net.wtako.IIDXSPGuide.data;

import android.content.Context;
import android.support.annotation.ColorRes;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.interfaces.SelectionOption;

public enum IIDXDifficultyLevel implements SelectionOption {
    LEVEL_7(R.color.material_lime_500),
    LEVEL_8(R.color.material_yellow_500),
    LEVEL_9(R.color.material_amber_500),
    LEVEL_10(R.color.material_orange_500),
    LEVEL_11(R.color.material_deep_orange_500),
    LEVEL_12(R.color.material_red_500);

    private final int colorRes;

    IIDXDifficultyLevel(@ColorRes int colorRes) {
        this.colorRes = colorRes;
    }

    @Override
    public String getDisplayName() {
        return "★".concat(String.valueOf(getLevel()));
    }

    @Override
    public String getShortDisplayName() {
        return "★".concat(String.valueOf(getLevel()));
    }

    @Override
    public int getColor(Context ctx) {
        return ctx.getResources().getColor(colorRes);
    }

    public int getLevel() {
        return Integer.parseInt(name().split("_")[1]);
    }
}
