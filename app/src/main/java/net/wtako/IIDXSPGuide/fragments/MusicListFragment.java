package net.wtako.IIDXSPGuide.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXChart;
import net.wtako.IIDXSPGuide.data.IIDXDifficultyLevel;
import net.wtako.IIDXSPGuide.data.IIDXMusic;
import net.wtako.IIDXSPGuide.data.IIDXVersion;
import net.wtako.IIDXSPGuide.interfaces.SelectionOption;
import net.wtako.IIDXSPGuide.utils.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MusicListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String PREF_TYPE = "pref_type";
    private static final String PREF_OPTION = "pref_option";
    private final List<IIDXMusic> musicList = new ArrayList<>();
    @InjectView(R.id.music_list)
    ListView listView;
    QuickAdapter<IIDXMusic> adapter;
    private int level;
    private IIDXVersion version;

    public MusicListFragment() {
    }

    public static MusicListFragment newInstance(SelectionOption selection) {
        MusicListFragment frag = new MusicListFragment();
        Bundle bundle = new Bundle();
        if (selection instanceof IIDXDifficultyLevel) {
            bundle.putString(PREF_TYPE, CategorySelectFragment.CategorySelection.DIFFICULTY_LEVEL.name());
            bundle.putString(PREF_OPTION, ((IIDXDifficultyLevel) selection).name());
        } else if (selection instanceof IIDXVersion) {
            bundle.putString(PREF_TYPE, CategorySelectFragment.CategorySelection.IIDX_VERSION.name());
            bundle.putString(PREF_OPTION, ((IIDXVersion) selection).name());
        }
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);
        ButterKnife.inject(this, rootView);

        String type = getArguments().getString(PREF_TYPE);
        if (type != null) {
            CategorySelectFragment.CategorySelection categorySelection = CategorySelectFragment.
                    CategorySelection.valueOf(type);
            if (categorySelection == CategorySelectFragment.CategorySelection.DIFFICULTY_LEVEL) {
                IIDXDifficultyLevel diff = IIDXDifficultyLevel.valueOf(getArguments().getString(PREF_OPTION));
                level = diff.getLevel();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getToolbar().setTitle(diff.getDisplayName());
                }
            } else if (categorySelection == CategorySelectFragment.CategorySelection.IIDX_VERSION) {
                version = IIDXVersion.valueOf(getArguments().getString(PREF_OPTION));
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).getToolbar().setTitle(version.getDisplayName());
                }
            }
        } else if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getToolbar().setTitle(R.string.text_all_music);
        }
        if (adapter == null) {
            adapter = new QuickAdapter<IIDXMusic>(getActivity(), R.layout.list_music_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, IIDXMusic item) {
                    ImageView iv = helper.getView(R.id.music_first_version_color);
                    iv.setColorFilter(item.getFirstVersion().getColor(getActivity()));
                    helper.setText(R.id.music_first_version_text, item.getFirstVersion().getShortDisplayName());
                    helper.setText(R.id.music_name, item.getName());
                    if (item.isRemoved()) {
                        helper.setTextColorRes(R.id.music_first_version_text, android.R.color.black);
                    } else {
                        helper.setTextColorRes(R.id.music_first_version_text, android.R.color.white);
                    }
                    helper.setText(R.id.music_bpm, item.getBPMDisplay());
                    helper.setText(R.id.music_difficulties, item.getDifficultyDisplay());
                    helper.setText(R.id.music_combos, item.getCombosDisplay());
                }
            };
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        if (musicList.isEmpty()) {
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... params) {
                    for (IIDXMusic music : Database.getSavedIIDXMusicList(getActivity()).getSavedData()) {
                        if (level != 0) {
                            for (IIDXChart chart : music.getCharts().values()) {
                                if (chart.getLevel() == level) {
                                    musicList.add(music);
                                    break;
                                }
                            }
                        } else if (version != null) {
                            if (music.getFirstVersion() == version) {
                                musicList.add(music);
                            }
                        } else {
                            musicList.add(music);
                        }
                    }
                    Collections.sort(musicList, new Comparator<IIDXMusic>() {
                        @Override
                        public int compare(IIDXMusic lhs, IIDXMusic rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    adapter.addAll(musicList);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onMusicDetails(musicList.get(position), level);
        }
    }

}
