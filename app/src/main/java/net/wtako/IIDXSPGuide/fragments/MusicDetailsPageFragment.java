package net.wtako.IIDXSPGuide.fragments;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;
import com.linearlistview.LinearListView;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXChart;
import net.wtako.IIDXSPGuide.data.IIDXChartDifficulty;
import net.wtako.IIDXSPGuide.data.IIDXDifficultyLevel;
import net.wtako.IIDXSPGuide.data.IIDXMusic;
import net.wtako.IIDXSPGuide.utils.Database;
import net.wtako.IIDXSPGuide.utils.GuideURL;
import net.wtako.IIDXSPGuide.utils.MiscUtils;
import net.wtako.IIDXSPGuide.widgets.ObservableScrollView;
import net.wtako.IIDXSPGuide.widgets.ScrollViewScrollDetector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.text.MessageFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MusicDetailsPageFragment extends Fragment {

    private static final long ATWIKI_PAGE_SIZE = 55000;
    private static final String PREF_MUSIC_ID = "pref_music_id";
    private static final String PREF_MUSIC_CHART = "pref_music_chart";

    @Bind(R.id.music_details_scroll_view)
    ObservableScrollView scrollView;

    @Bind(R.id.chart_diff_bg)
    ImageView diffBG;
    @Bind(R.id.chart_diff_text)
    TextView diffText;
    @Bind(R.id.chart_combos_bg)
    ImageView comboBG;
    @Bind(R.id.chart_combos_text)
    TextView comboText;
    @Bind(R.id.chart_ncdiff_bg)
    ImageView ncdiffBG;
    @Bind(R.id.chart_ncdiff_text)
    TextView ncdiffText;
    @Bind(R.id.chart_hcdiff_bg)
    ImageView hcdiffBG;
    @Bind(R.id.chart_hcdiff_text)
    TextView hcdiffText;
    @Bind(R.id.chart_characteristics)
    TextView chartChars;
    @Bind(R.id.chart_suggestions_1p)
    TextView suggestions1p;
    @Bind(R.id.chart_suggestions_2p)
    TextView suggestions2p;
    @Bind(R.id.chart_clear_rate)
    TextView clearRate;
    @Bind(R.id.chart_guide)
    TextView chartGuide;
    @Bind(R.id.atwiki_comments)
    LinearListView commentList;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    QuickAdapter<String> adapter;
    private IIDXChart chart;
    private boolean loadingComments = false;

    public static MusicDetailsPageFragment newInstance(int musicID, IIDXChartDifficulty chartDifficulty) {
        MusicDetailsPageFragment frag = new MusicDetailsPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PREF_MUSIC_ID, musicID);
        bundle.putString(PREF_MUSIC_CHART, chartDifficulty.name());
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_details_page, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        IIDXMusic music = Database.getSavedIIDXMusicList(getActivity()).getSavedData()
                .get(getArguments().getInt(PREF_MUSIC_ID));
        IIDXChartDifficulty difficulty = IIDXChartDifficulty.valueOf(getArguments().getString(PREF_MUSIC_CHART));
        chart = music.getCharts().get(difficulty);

        setupViewsDisplay();

        if (adapter == null) {
            adapter = new QuickAdapter<String>(getActivity(), R.layout.list_comment_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, String item) {
                    helper.setText(R.id.comment_text, item);
                }
            };
        }

        commentList.setAdapter(adapter);

        if (chart.getAtWikiComments().isEmpty()) {
            loadComments();
        } else {
            adapter.clear();
            adapter.addAll(chart.getAtWikiComments());
            progressBar.setVisibility(View.GONE);
        }

        ScrollViewScrollDetector svsd = new ScrollViewScrollDetector() {

            View container = ButterKnife.findById(getActivity(), R.id.music_info_container);
            boolean triggered = false;

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            protected void onScrollUp() {
                if (triggered) {
                    return;
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    container.setVisibility(View.GONE);
                    return;
                }
                triggered = true;
                container.animate().translationY(MiscUtils.dpToPx(getActivity(), -80)).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        triggered = false;
                    }
                }).start();
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            protected void onScrollDown() {
                if (triggered) {
                    return;
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    container.setVisibility(View.VISIBLE);
                    return;
                }
                triggered = true;
                container.animate().translationY(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        triggered = false;
                    }
                }).start();
            }
        };
        svsd.setScrollThreshold(8);
        scrollView.setOnScrollChangedListener(svsd);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.music_details_page, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onCommentsRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCommentsRefresh() {
        if (loadingComments) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        adapter.clear();
        chart.getAtWikiComments().clear();
        loadComments();
    }

    private void loadComments() {
        if (chart.getAtWikiID() != 0) {
            loadingComments = true;
            String url = MessageFormat.format(GuideURL.ATWIKI_BASE.getUrl(), String.valueOf(chart.getAtWikiID()));
            MainActivity.getAsyncHTTPClient().get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                    progressBar.setProgress(100);
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            Document doc = Jsoup.parse(new String(responseBody));
                            try {
                                for (Element row : doc.select("#wikibody > ul").first().children()) {
                                    StringBuilder builder = new StringBuilder();
                                    for (Node node : row.childNodes()) {
                                        if (node instanceof TextNode) {
                                            builder.append(((TextNode) node).text());
                                        } else {
                                            builder.append("\n");
                                        }
                                    }
                                    if (!chart.getAtWikiComments().contains(builder.toString())) {
                                        chart.getAtWikiComments().add(builder.toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                            if (!isAdded()) {
                                return false;
                            }
                            Database.getSavedIIDXMusicList(getActivity()).save();
                            return true;
                        }

                        @Override
                        protected void onPostExecute(Boolean success) {
                            loadingComments = false;
                            if (!isAdded()) {
                                return;
                            }
                            progressBar.setVisibility(View.GONE);
                            if (!success) {
                                return;
                            }
                            adapter.clear();
                            adapter.addAll(chart.getAtWikiComments());
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    loadingComments = false;
                    Toast.makeText(getActivity(), R.string.error_network_problem, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    if (!isAdded()) {
                        return;
                    }
                    progressBar.setProgress((int) ((bytesWritten * 100) / ATWIKI_PAGE_SIZE));
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupViewsDisplay() {
        diffText.setText("★".concat(String.valueOf(chart.getLevel() == 0 ? "?" : chart.getLevel())));
        for (IIDXDifficultyLevel level : IIDXDifficultyLevel.values()) {
            if (level.getLevel() == chart.getLevel()) {
                diffBG.setColorFilter(level.getColor(getActivity()));
                break;
            }
        }
        comboBG.setColorFilter(getResources().getColor(R.color.material_deep_teal_500));
        ncdiffBG.setColorFilter(getResources().getColor(R.color.material_amber_500));
        hcdiffBG.setColorFilter(getResources().getColor(R.color.material_red_500));
        comboText.setText(chart.getMaxCombos() == 0 ? "?" : MessageFormat.format("{0}", chart.getMaxCombos()));
        ncdiffText.setText(chart.getNormalClearDifficulty() == 0 ? "-" :
                chart.getNormalClearDifficulty() == 20 ? "BAD" :
                        chart.getNormalClearDifficulty() == 16 ? (chart.getLevel() == 12 ? "15+" : "10+") :
                                String.valueOf(chart.getNormalClearDifficulty()));
        hcdiffText.setText(chart.getHardClearDifficulty() == 0 ? "-" :
                chart.getHardClearDifficulty() == 20 ? "乙" :
                        chart.getHardClearDifficulty() == 16 ? (chart.getLevel() == 12 ? "15+" : "10+") :
                                String.valueOf(chart.getHardClearDifficulty()));
        String display = chart.getCharacteristicsDisplay();
        chartChars.setText(display.isEmpty() ? "-" : display);
        suggestions1p.setText(MiscUtils.humanList("、", chart.getSuggestions1P()));
        suggestions2p.setText(MiscUtils.humanList("、", chart.getSuggestions2P()));
        clearRate.setText(chart.getClearRate() == 0 ? "-%" : String.valueOf(chart.getClearRate()).concat("%"));
        if (chart.getClickAgainGuide() != null && chart.getClickAgainGuide().length() > 2) {
            chartGuide.setText(chart.getClickAgainGuide());
        } else {
            chartGuide.setVisibility(View.GONE);
        }
    }
}
