package net.wtako.IIDXSPGuide.data;

import android.content.Context;
import android.support.annotation.ColorRes;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.interfaces.SelectionOption;

public enum IIDXVersion implements SelectionOption {
    CONSOLE_OR_UNKNOWN("CS/Unknown", "CS", R.color.material_grey_500),
    IIDX_1ST("1st", null, R.color.material_grey_800),
    IIDX_SUBSTREAM("substream", "SUB", R.color.material_brown_500),
    IIDX_2ND("2nd", null, R.color.material_yellow_500),
    IIDX_3RD("3rd", null, R.color.material_green_500),
    IIDX_4TH("4th", null, R.color.material_purple_300),
    IIDX_5TH("5th", null, R.color.material_deep_orange_500),
    IIDX_6TH("6th", null, R.color.material_light_green_500),
    IIDX_7TH("7th", null, R.color.material_light_green_300),
    IIDX_8TH("8th", null, R.color.material_indigo_500),
    IIDX_9TH("9th", null, R.color.material_amber_500),
    IIDX_10TH("10th", null, R.color.material_grey_500),
    IIDX_RED("IIDX RED", "RED", R.color.material_red_400),
    HAPPY_SKY("HAPPY SKY", "SKY", R.color.material_teal_200),
    DISTORTED("DistorteD", "DD", R.color.material_grey_600),
    GOLD("GOLD", "GO", R.color.material_amber_700),
    DJ_TROOPERS("DJ TROOPERS", "DJT", R.color.material_green_700),
    EMPRESS("EMPRESS", "EMP", R.color.material_pink_200),
    PREMIUM_BEST("PREMIUM BEST", "PB", R.color.material_pink_300),
    SIRIUS("SIRIUS", "SIR", R.color.material_blue_200),
    RESORT_ANTHEM("Resort Anthem", "RA", R.color.material_deep_orange_400),
    LINCLE("Lincle", "Lin", R.color.material_teal_400),
    TRICORO("tricoro", "tri", R.color.material_light_green_600),
    SPADA("SPADA", "SPD", R.color.material_red_500),
    PENDUAL("PENDUAL", "PEN", R.color.material_grey_500),
    COPULA("copula", "CPL", R.color.material_amber_400);

    private final String versionName;
    private final String clickAgainName;
    private final int colorRes;

    IIDXVersion(String versionName, String clickAgainName, @ColorRes int colorRes) {
        this.versionName = versionName;
        if (clickAgainName == null) {
            this.clickAgainName = versionName;
        } else {
            this.clickAgainName = clickAgainName;
        }
        this.colorRes = colorRes;
    }

    public static IIDXVersion fromVersionDigit(String digit) {
        if (digit.toLowerCase().startsWith("s")) {
            return IIDX_SUBSTREAM;
        }
        if (digit.toLowerCase().startsWith("pb")) {
            return PREMIUM_BEST;
        }
        try {
            int intDigit = Integer.parseInt(digit);
            if (intDigit == 1) {
                return IIDX_1ST;
            }
            if (intDigit >= 17) {
                return IIDXVersion.values()[intDigit + 2];
            }
            return IIDXVersion.values()[intDigit + 1];
        } catch (Exception e) {
            return CONSOLE_OR_UNKNOWN;
        }
    }

    public static boolean isConsole(String digit) {
        if (digit.toLowerCase().startsWith("pb")) {
            return true;
        }
        if (digit.toLowerCase().startsWith("cs")) {
            return true;
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return versionName;
    }

    @Override
    public String getShortDisplayName() {
        return clickAgainName;
    }

    public int getColor(Context ctx) {
        return ctx.getResources().getColor(colorRes);
    }
}
