package net.wtako.IIDXSPGuide.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.text.MessageFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MusicDetailsPageFragment extends Fragment {

    private static final long ATWIKI_PAGE_SIZE = 55000;
    private static final String PREF_MUSIC_ID = "pref_music_id";
    private static final String PREF_MUSIC_CHART = "pref_music_chart";
    @InjectView(R.id.chart_diff_bg)
    ImageView diffBG;
    @InjectView(R.id.chart_diff_text)
    TextView diffText;
    @InjectView(R.id.chart_combos_bg)
    ImageView comboBG;
    @InjectView(R.id.chart_combos_text)
    TextView comboText;
    @InjectView(R.id.chart_ncdiff_bg)
    ImageView ncdiffBG;
    @InjectView(R.id.chart_ncdiff_text)
    TextView ncdiffText;
    @InjectView(R.id.chart_hcdiff_bg)
    ImageView hcdiffBG;
    @InjectView(R.id.chart_hcdiff_text)
    TextView hcdiffText;
    @InjectView(R.id.chart_characteristics)
    TextView chartChars;
    @InjectView(R.id.chart_suggestions_1p)
    TextView suggestions1p;
    @InjectView(R.id.chart_suggestions_2p)
    TextView suggestions2p;
    @InjectView(R.id.chart_clear_rate)
    TextView clearRate;
    @InjectView(R.id.chart_guide)
    TextView chartGuide;
    @InjectView(R.id.atwiki_comments)
    LinearListView commentLayout;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    QuickAdapter<String> adapter;
    private IIDXMusic music;
    private IIDXChartDifficulty difficulty;


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
        ButterKnife.inject(this, rootView);
        music = Database.getSavedIIDXMusicList(getActivity()).getSavedData()
                .get(getArguments().getInt(PREF_MUSIC_ID));
        difficulty = IIDXChartDifficulty.valueOf(getArguments().getString(PREF_MUSIC_CHART));

        final IIDXChart chart = setupViewsDisplay();

        if (adapter == null) {
            adapter = new QuickAdapter<String>(getActivity(), R.layout.list_comment_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, String item) {
                    helper.setText(R.id.comment_text, item);
                }
            };
        }
        commentLayout.setAdapter(adapter);

        if (chart.getAtWikiComments().isEmpty()) {
            loadComments(chart);
        } else {
            adapter.clear();
            adapter.addAll(chart.getAtWikiComments());
            progressBar.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void loadComments(final IIDXChart chart) {
        if (chart.getAtWikiID() != 0) {
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
                            if (!success) {
                                return;
                            }
                            adapter.clear();
                            adapter.addAll(chart.getAtWikiComments());
                            progressBar.setVisibility(View.GONE);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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

    private IIDXChart setupViewsDisplay() {
        final IIDXChart chart = music.getCharts().get(difficulty);
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
                chart.getHardClearDifficulty() == 20 ? "BAD" :
                        chart.getHardClearDifficulty() == 16 ? (chart.getLevel() == 12 ? "15+" : "10+") :
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
        return chart;
    }


}
