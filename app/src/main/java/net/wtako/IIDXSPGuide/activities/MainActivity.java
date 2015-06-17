package net.wtako.IIDXSPGuide.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
import net.wtako.IIDXSPGuide.utils.MiscUtils;

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
    @InjectView(R.id.reveal)
    View reveal;
    @InjectView(R.id.reveal_background)
    View revealBackground;

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

    @SuppressLint("NewApi")
    public void animateAppAndStatusBar(final int toColor) {
        int fromColor = ((ColorDrawable) reveal.getBackground()).getColor();
        revealBackground.setBackgroundColor(fromColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Animator animator = ViewAnimationUtils.createCircularReveal(
                        reveal,
                        toolbar.getWidth() / 2,
                        toolbar.getHeight() / 2, 0,
                        toolbar.getWidth() / 2);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        reveal.setBackgroundColor(toColor);
                    }
                });
                animator.setStartDelay(200);
                animator.setDuration(200);
                animator.start();
                reveal.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        reveal.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.setStartDelay(100);
                colorAnimation.setDuration(500);
                colorAnimation.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, MiscUtils.dpToPx(this, 56));
            toolbar.setLayoutParams(lp);
            toolbar.setPadding(0, 0, 0, 0);
            reveal.setLayoutParams(lp);
            reveal.setPadding(0, 0, 0, 0);
            revealBackground.setLayoutParams(lp);
            revealBackground.setPadding(0, 0, 0, 0);
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
        ft.setCustomAnimations(R.anim.abc_popup_enter, R.anim.abc_popup_exit,
                R.anim.abc_popup_enter, R.anim.abc_popup_exit)
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
        if (LoadFromGuideSitesTask.getInstance() != null &&
                LoadFromGuideSitesTask.getInstance().getStatus() != AsyncTask.Status.FINISHED) {
            Toast.makeText(this, getString(R.string.download_already_running), Toast.LENGTH_SHORT).show();
            return;
        }
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
