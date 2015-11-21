package net.wtako.IIDXSPGuide.utils;

import android.content.Context;

import net.wtako.IIDXSPGuide.activities.MainActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by saren on 18/2/15.
 */
public class DatabaseData<T> {
    private final String key;
    private final Context ctx;
    private final Type type;
    public Integer dataSizeLimit;
    private List<T> savedData;

    public DatabaseData(Context ctx, String key, Type type) {
        this.key = key;
        if (ctx == null) {
            this.ctx = MainActivity.getInstance();
        } else {
            this.ctx = ctx;
        }
        this.type = type;
    }

    public synchronized T getSavedItem(T item) {
        if (item == null) {
            return null;
        }
        if (savedData == null) {
            savedData = getSavedData();
        }
        for (int i = 0; i < savedData.size(); i++) {
            if (savedData.get(i) == null) {
                savedData.remove(i--);
                continue;
            }
            if (savedData.get(i).equals(item)) {
                item = savedData.get(i);
                break;
            }
        }
        return item;
    }

    public synchronized List<T> getSavedData() {
        if (savedData != null) {
            return savedData;
        }
        try {
            if (MainActivity.getSP(ctx).contains(key)) {
                savedData = MainActivity.gson.fromJson(MainActivity.getSP(ctx).getString(key, null), type);
            } else {
                savedData = new ArrayList<>();
            }
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
            savedData = new ArrayList<>();
        }
        savedData = Collections.synchronizedList(savedData);
        return savedData;
    }

    public synchronized void save() {
        if (savedData == null) {
            savedData = getSavedData();
        }
        try {
            if (dataSizeLimit != null) {
                savedData = savedData.subList(0, savedData.size() < dataSizeLimit ? savedData.size() : dataSizeLimit);
            }
            MainActivity.getSP(ctx).edit().putString(key, MainActivity.gson.toJson(new ArrayList<>(savedData))).apply();
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean contains(T item) {
        return item != null && (!(item instanceof Integer) || (Integer) item >= 0) && getSavedData().contains(item);
    }

    public synchronized DatabaseData<T> addUnique(T item) {
        try {
            if (savedData == null) {
                savedData = getSavedData();
            }
            if (!contains(item)) {
                savedData.add(item);
            }
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> update(T item) {
        try {
            if (savedData == null) {
                savedData = getSavedData();
            }
            for (int i = 0; i < savedData.size(); i++) {
                if (savedData.get(i) == null) {
                    savedData.remove(i--);
                    continue;
                }
                if (savedData.get(i).equals(item)) {
                    savedData.set(i, item);
                    break;
                }
            }
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> equalObjectPrependOrBringToFront(T item) {
        try {
            if (savedData == null) {
                savedData = getSavedData();
            }
            T saved = getSavedItem(item);
            savedData.remove(item);
            savedData.add(0, saved);
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
        }
        return this;
    }

    public synchronized DatabaseData<T> prependOrBringToFront(T item) {
        try {
            if (savedData == null) {
                savedData = getSavedData();
            }
            savedData.remove(item);
            savedData.add(0, item);
        } catch (Exception | StackOverflowError e) {
            e.printStackTrace();
        }
        return this;
    }

}
