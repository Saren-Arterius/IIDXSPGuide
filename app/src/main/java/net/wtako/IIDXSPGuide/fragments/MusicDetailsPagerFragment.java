package net.wtako.IIDXSPGuide.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXChartDifficulty;
import net.wtako.IIDXSPGuide.data.IIDXMusic;
import net.wtako.IIDXSPGuide.utils.Database;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MusicDetailsPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private static final String PREF_MUSIC_ID = "pref_music_id";
    private static final String PREF_VIEW_LEVEL = "pref_view_level";

    @Bind(R.id.music_info_container)
    View container;
    @Bind(R.id.music_first_version)
    TextView firstVersion;
    @Bind(R.id.music_bpm)
    TextView musicBPM;
    @Bind(R.id.pager_titles)
    TitlePageIndicator titleIndicator;
    @Bind(R.id.pager)
    ViewPager mPager;
    IIDXMusic music;
    private ScreenSlidePagerAdapter mPagerAdapter;

    public MusicDetailsPagerFragment() {
    }

    public static MusicDetailsPagerFragment newInstance(int musicID, int viewLevel) {
        MusicDetailsPagerFragment frag = new MusicDetailsPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PREF_MUSIC_ID, musicID);
        bundle.putInt(PREF_VIEW_LEVEL, viewLevel);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_music_details_pager, container, false);
        ButterKnife.bind(this, rootView);

        music = Database.getSavedIIDXMusicList(getActivity()).getSavedData()
                .get(getArguments().getInt(PREF_MUSIC_ID));


        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        titleIndicator.setViewPager(mPager);
        titleIndicator.setFooterColor(getResources().getColor(R.color.material_deep_teal_200));
        titleIndicator.setLinePosition(TitlePageIndicator.LinePosition.Top);
        titleIndicator.setOnPageChangeListener(this);

        firstVersion.setText(music.getFirstVersion().getDisplayName());
        if (music.isRemoved()) {
            firstVersion.setPaintFlags(firstVersion.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        musicBPM.setText(music.getBPMDisplay());

        if (getActivity() instanceof MainActivity) {
            MainActivity main = ((MainActivity) getActivity());
            main.getToolbar().setTitle(music.getName());
            main.animateAppAndStatusBar(music.getFirstVersion().getColor(main));
            main.getToolbar().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isAdded()) {
                        return false;
                    }
                    ClipboardManager clipboard = (ClipboardManager) MainActivity.getInstance()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Music name", music.getName());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.getInstance(), MainActivity.getInstance()
                            .getString(R.string.text_copied_music_name_to_clipboard), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        switchToViewLevel();

        return rootView;
    }

    private void switchToViewLevel() {
        int viewLevel = getArguments().getInt(PREF_VIEW_LEVEL);
        if (viewLevel != 0) {
            List<IIDXChartDifficulty> diffs = music.sortedChartDifficulties();
            Collections.reverse(diffs);
            int index = diffs.size() - 1;
            for (IIDXChartDifficulty difficulty : diffs) {
                if (music.getCharts().get(difficulty).getLevel() == viewLevel) {
                    titleIndicator.setCurrentItem(index);
                    break;
                }
                index--;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            container.setVisibility(View.VISIBLE);
            return;
        }
        container.animate().translationY(0).start();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int page) {
            return MusicDetailsPageFragment.newInstance(getArguments().getInt(PREF_MUSIC_ID),
                    music.sortedChartDifficulties().get(page));
        }

        @Override
        public int getCount() {
            return music.getCharts().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return music.sortedChartDifficulties().get(position).name().replace('_', ' ');
        }

    }
}
