package net.wtako.IIDXSPGuide.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.data.IIDXMusic;
import net.wtako.IIDXSPGuide.fragments.CategorySelectFragment;
import net.wtako.IIDXSPGuide.fragments.MainActivityFragment;
import net.wtako.IIDXSPGuide.fragments.MusicDetailsPagerFragment;
import net.wtako.IIDXSPGuide.fragments.MusicListFragment;
import net.wtako.IIDXSPGuide.interfaces.SelectionOption;
import net.wtako.IIDXSPGuide.tasks.LoadFromGuideSitesTask;
import net.wtako.IIDXSPGuide.utils.Database;

import java.text.MessageFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    public static final String PREF_DATA_LOADED = "data_loaded";
    public static final Gson gson = new Gson();
    private static SharedPreferences sharedPreferences;
    private static MainActivity instance;
    private static AsyncHttpClient asyncClient;
    private static SyncHttpClient syncClient;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    public static SharedPreferences getSP(Context ctx) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx != null ? ctx : MainActivity.getInstance());
        }
        return sharedPreferences;
    }

    public static AsyncHttpClient getAsyncHTTPClient() {
        if (asyncClient == null) {
            asyncClient = new AsyncHttpClient(true, 80, 443);
            asyncClient.setEnableRedirects(true);
        }
        return asyncClient;
    }

    public static SyncHttpClient getSyncHTTPClient() {
        if (syncClient == null) {
            syncClient = new SyncHttpClient(true, 80, 443);
            syncClient.setEnableRedirects(true);
        }
        return syncClient;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.fragment_main, new MainActivityFragment()).commit();

        if (!getSP(this).getBoolean(PREF_DATA_LOADED, false)) {
            new LoadFromGuideSitesTask(this).registerCallback(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, getString(R.string.download_finish), Toast.LENGTH_SHORT).show();
                }
            }).registerCallback(new Runnable() {
                @Override
                public void run() {
                    preloadSearchMatches();
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            preloadSearchMatches();
        }
    }

    public void preloadSearchMatches() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.w("PreloadStart", String.valueOf(System.currentTimeMillis()));
                for (IIDXMusic music : Database.getSavedIIDXMusicList(MainActivity.this).getSavedData()) {
                    music.getSearchMatch();
                }
                Log.w("PreloadEnd", String.valueOf(System.currentTimeMillis()));
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onMusicDetails(IIDXMusic music, int viewLevel) {
        MusicDetailsPagerFragment fragment = MusicDetailsPagerFragment
                .newInstance(Database.getSavedIIDXMusicList(this).getSavedData().indexOf(music), viewLevel);
        replaceToFragment(fragment);
    }

    public void onSelect(SelectionOption option) {
        MusicListFragment fragment = MusicListFragment.newInstance(option);
        replaceToFragment(fragment);
    }

    public void onButtonDifficulty(View view) {
        CategorySelectFragment fragment = CategorySelectFragment.newInstance(
                CategorySelectFragment.CategorySelection.DIFFICULTY_LEVEL);
        replaceToFragment(fragment);
    }

    public void onButtonIIDXVersion(View view) {
        CategorySelectFragment fragment = CategorySelectFragment.newInstance(
                CategorySelectFragment.CategorySelection.IIDX_VERSION);
        replaceToFragment(fragment);
    }

    public void replaceToFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.fragment_main, fragment).addToBackStack(null).commit();
    }

    public void onButtonMusicName(View view) {
        onSelect(null);
    }

    public void onButtonSettings(View view) {
        String[] options = new String[]{getString(R.string.text_re_download_music_info),
                getString(R.string.text_about)};
        new MaterialDialog.Builder(this).title(R.string.text_settings).items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                reloadMusicInfo();
                                break;
                            case 1:
                                showAboutMe();
                                break;
                        }
                    }
                }).show();
    }

    private void showAboutMe() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            String format = "{0} v{1}\nAuthor: Saren\n\n{2}\n\nThe music information is " +
                    "fetched from sites above WITHOUT any permissions. It`s to invoke clickagain`s author`s " +
                    "attention to update his website. Feel free to report to Google Play to take down to this app " +
                    "so that everybody can no longer use this app. I made this app for myself only, so I don't care at all.";
            String content = MessageFormat.format(format, getString(R.string.app_name),
                    version, getString(R.string.download_guide_source_text));
            SpannableStringBuilder ssb = new SpannableStringBuilder(content);
            Linkify.addLinks(ssb, Linkify.WEB_URLS);
            new MaterialDialog.Builder(MainActivity.this).title(R.string.text_about)
                    .content(ssb).positiveText(R.string.text_ok).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void reloadMusicInfo() {
        getSP(MainActivity.this).edit().putBoolean(MainActivity.PREF_DATA_LOADED, false).apply();
        Database.getSavedIIDXMusicList(MainActivity.this).getSavedData().clear();
        new LoadFromGuideSitesTask(MainActivity.this).registerCallback(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, getString(R.string.download_finish),
                        Toast.LENGTH_SHORT).show();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
