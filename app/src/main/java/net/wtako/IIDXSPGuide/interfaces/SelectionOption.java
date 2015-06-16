package net.wtako.IIDXSPGuide.interfaces;

import android.content.Context;

public interface SelectionOption {
    int getColor(Context ctx);

    String getDisplayName();

    String getShortDisplayName();
}
