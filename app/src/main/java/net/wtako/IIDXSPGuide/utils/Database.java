package net.wtako.IIDXSPGuide.utils;

import android.content.Context;

import com.google.gson.reflect.TypeToken;

import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXMusic;

import java.util.List;

/**
 * Created by saren on 18/2/15.
 */
public class Database {

    private static DatabaseData<IIDXMusic> savedIIDXMusicList;

    public static synchronized DatabaseData<IIDXMusic> getSavedIIDXMusicList(Context ctx) {
        if (savedIIDXMusicList != null) {
            return savedIIDXMusicList;
        }
        if (!MainActivity.getSP(ctx).getBoolean("reset_1", false)) {
            MainActivity.getSP(ctx).edit().remove("iidx_music_list").putBoolean("reset_1", true).apply();
        }
        savedIIDXMusicList = new DatabaseData<>(ctx, "iidx_music_list", new TypeToken<List<IIDXMusic>>() {
        }.getType());
        return savedIIDXMusicList;
    }

}
