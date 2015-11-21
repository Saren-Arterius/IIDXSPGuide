package net.wtako.IIDXSPGuide.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
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
import net.wtako.IIDXSPGuide.tasks.LoadFromGuideSitesTask;
import net.wtako.IIDXSPGuide.utils.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MusicListFragment extends Fragment implements AdapterView.OnItemClickListener, ActionMode.Callback, TextWatcher {

    private static final String PREF_TYPE = "pref_type";
    private static final String PREF_OPTION = "pref_option";
    private final List<IIDXMusic> musicList = new ArrayList<>();
    @Bind(R.id.music_list)
    ListView listView;
    QuickAdapter<IIDXMusic> adapter;
    private ActionMode actionMode;
    private int level;
    private IIDXVersion version;
    private List<IIDXMusic> matches = new ArrayList<>();
    private EditText searchEditText;
    private CharSequence oldQuery;

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
        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);

        String type = getArguments().getString(PREF_TYPE);
        if (type != null) {
            CategorySelectFragment.CategorySelection categorySelection = CategorySelectFragment.
                    CategorySelection.valueOf(type);
            if (categorySelection == CategorySelectFragment.CategorySelection.DIFFICULTY_LEVEL) {
                IIDXDifficultyLevel diff = IIDXDifficultyLevel.valueOf(getArguments().getString(PREF_OPTION));
                level = diff.getLevel();
                if (getActivity() instanceof MainActivity) {
                    MainActivity main = ((MainActivity) getActivity());
                    main.getToolbar().setTitle(diff.getDisplayName());
                    main.animateAppAndStatusBar(diff.getColor(main));
                }
            } else if (categorySelection == CategorySelectFragment.CategorySelection.IIDX_VERSION) {
                version = IIDXVersion.valueOf(getArguments().getString(PREF_OPTION));
                if (getActivity() instanceof MainActivity) {
                    MainActivity main = ((MainActivity) getActivity());
                    main.getToolbar().setTitle(version.getDisplayName());
                    main.animateAppAndStatusBar(version.getColor(main));
                }
            }
        } else if (getActivity() instanceof MainActivity) {
            MainActivity main = ((MainActivity) getActivity());
            main.getToolbar().setTitle(R.string.text_all_music);
            main.animateAppAndStatusBar(main.getResources().getColor(R.color.material_amber_500));
        }
        if (adapter == null) {
            adapter = new QuickAdapter<IIDXMusic>(getActivity(), R.layout.list_music_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, IIDXMusic item) {
                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        if (!MainActivity.getSP(getActivity()).getBoolean(MainActivity.PREF_DATA_LOADED, false)) {
            final ProgressDialog pd = ProgressDialog.show(getActivity(), getString(R.string.download_title),
                    getString(R.string.download_indeterminate_message));
            pd.setCancelable(true);
            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getActivity().onBackPressed();
                }
            });
            if (LoadFromGuideSitesTask.getInstance() == null ||
                    LoadFromGuideSitesTask.getInstance().getStatus() == AsyncTask.Status.FINISHED) {
                new LoadFromGuideSitesTask(MainActivity.getInstance()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            LoadFromGuideSitesTask.getInstance().registerCallback(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    pd.dismiss();
                    addMusicToAdapter();
                }
            }).registerCallback(new Runnable() {
                @Override
                public void run() {
                    MainActivity.getInstance().preloadSearchMatches();
                }
            });
        } else if (oldQuery != null) {
            onFilter();
            searchEditText.setText(oldQuery);
            oldQuery = null;
        } else {
            if (musicList.isEmpty()) {
                addMusicToAdapter();
            }
        }
        return rootView;
    }

    private void addMusicToAdapter() {
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (getActivity() instanceof MainActivity) {
                MainActivity main = ((MainActivity) getActivity());
                IIDXMusic music;
                if (actionMode != null) {
                    music = matches.get(position);
                    InputMethodManager imm = (InputMethodManager) main.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    oldQuery = searchEditText.getText();
                    actionMode.finish();
                } else {
                    music = musicList.get(position);
                }
                main.onMusicDetails(music, level);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.music_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                onFilter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onFilter() {
        if (getActivity() instanceof MainActivity) {
            MainActivity main = ((MainActivity) getActivity());
            actionMode = main.getToolbar().startActionMode(this);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        searchEditText = new EditText(getActivity());
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setSingleLine(true);
        searchEditText.setHint(R.string.app_music_filter_hint);
        searchEditText.setHintTextColor(getResources().getColor(R.color.material_grey_500));
        mode.setCustomView(searchEditText);
        searchEditText.addTextChangedListener(this);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        searchEditText = null;

        if (oldQuery == null) {
            adapter.clear();
            adapter.addAll(musicList);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String query = s.toString();
        if (query.isEmpty()) {
            return;
        }
        adapter.clear();
        matches.clear();
        for (IIDXMusic music : musicList) {
            boolean matched = true;
            for (String subQuery : query.split("\\s+")) {
                if (!music.getSearchMatch().contains(subQuery.toLowerCase())) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                matches.add(music);
            }
        }
        adapter.addAll(matches);
    }
}
