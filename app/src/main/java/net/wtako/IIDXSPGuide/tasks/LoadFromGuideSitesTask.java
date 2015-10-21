package net.wtako.IIDXSPGuide.tasks;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXChart;
import net.wtako.IIDXSPGuide.data.IIDXChartDifficulty;
import net.wtako.IIDXSPGuide.data.IIDXChartSuggestOption;
import net.wtako.IIDXSPGuide.data.IIDXMusic;
import net.wtako.IIDXSPGuide.data.IIDXVersion;
import net.wtako.IIDXSPGuide.utils.Database;
import net.wtako.IIDXSPGuide.utils.GuideURL;
import net.wtako.IIDXSPGuide.utils.MiscUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class LoadFromGuideSitesTask extends AsyncTask<Void, Void, Boolean> {

    private final static int ITEMS_COUNT = 4140;
    private static LoadFromGuideSitesTask instance;
    private final NotificationManager mNotifyManager;
    private final NotificationCompat.Builder onGoingBuilder;
    private final Context ctx;
    private List<GuideURL> guideURLs = new ArrayList<>();
    private Set<Runnable> callbacks = new HashSet<>();
    private int sitesProcessed = 0;
    private int itemsProcessed = 0;

    public LoadFromGuideSitesTask(Context ctx) {
        instance = this;
        this.ctx = ctx;
        mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        onGoingBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_file_download_white_24dp).setContentTitle(ctx.getString(R.string.download_title));
        onGoingBuilder.setOngoing(true);
    }

    public static LoadFromGuideSitesTask getInstance() {
        return instance;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (GuideURL guideURL : GuideURL.values()) {
            if (guideURL.name().startsWith("ATWIKI_INIT_")) {
                guideURLs.add(guideURL);
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        publishProgress();
        for (GuideURL guideURL : guideURLs) {
            loadAtWikiPage(guideURL);
            sitesProcessed++;
            publishProgress();
        }
        loadClickAgain();
        sitesProcessed++;
        publishProgress();
        Database.getSavedIIDXMusicList(ctx).save();
        MainActivity.getSP(ctx).edit().putBoolean(MainActivity.PREF_DATA_LOADED, true).apply();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        mNotifyManager.cancel(ITEMS_COUNT);
        for (Runnable callback : callbacks) {
            callback.run();
        }
        instance = this;
    }

    public LoadFromGuideSitesTask registerCallback(Runnable runnable) {
        callbacks.add(runnable);
        return this;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        if (itemsProcessed % 46 != 0) { // LOL
            return;
        }
        if (itemsProcessed >= ITEMS_COUNT) {
            onGoingBuilder.setContentText(ctx.getString(R.string.download_almost_finish));
            onGoingBuilder.setProgress(0, 0, true);
        } else {
            onGoingBuilder.setContentText(MessageFormat.format(ctx.getString(R.string.download_message),
                    sitesProcessed, guideURLs.size()));
            onGoingBuilder.setProgress(ITEMS_COUNT, itemsProcessed, false);
        }
        mNotifyManager.notify(ITEMS_COUNT, onGoingBuilder.build());
    }

    private void loadClickAgain() {
        MainActivity.getSyncHTTPClient().get(GuideURL.CLICKAGAIN_ALL_MUSIC.getUrl(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc;
                try {
                    doc = Jsoup.parse(new String(responseBody, "SHIFT_JIS"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                for (Element table : doc.select("table[id][oncontextmenu]")) {
                    IIDXChartDifficulty difficulty = null;
                    int level = 0;
                    boolean isConsole = false;
                    boolean isRemoved = false;
                    for (IIDXChartDifficulty iterateDifficulty : IIDXChartDifficulty.values()) {
                        String[] segments = table.attr("id").split(iterateDifficulty.getClickAgainName());
                        if (segments.length == 1) {
                            level = MiscUtils.parseInt(segments[0], 0);
                            if (level > 0) {
                                difficulty = iterateDifficulty;
                                break;
                            } else {
                                continue;
                            }
                        }
                        level = Integer.parseInt(segments[0]);
                        difficulty = iterateDifficulty;
                        isConsole = segments[1].equals("CS");
                        isRemoved = segments[1].equals("Removed");
                        break;
                    }
                    if (difficulty == null) {
                        Log.w("WTF", "diff == null " + table.attr("id"));
                        continue;
                    }
                    for (Element row : table.child(0).children()) {
                        IIDXVersion firstVersion = null;
                        boolean musicFromConsole = isConsole;
                        if (row.hasClass("top")) {
                            continue;
                        }
                        for (Node child : row.child(0).childNodes()) {
                            if (!(child instanceof TextNode)) {
                                continue;
                            }
                            String nodeText = ((TextNode) child).text().trim();
                            if (nodeText.equals("CS")) {
                                musicFromConsole = true;
                            } else {
                                for (IIDXVersion version : IIDXVersion.values()) {
                                    if (nodeText.equals(version.getShortDisplayName())) {
                                        firstVersion = version;
                                        break;
                                    }
                                }
                            }
                        }
                        if (firstVersion == null) {
                            continue;
                        }

                        IIDXChartDifficulty musicDifficulty = difficulty;

                        String name = ((TextNode) row.child(1).childNodes().get(0)).text().trim();
                        String alias = null;
                        try {
                            alias = ((TextNode) row.child(1).childNodes().get(2)).text().trim();
                        } catch (Exception ignored) {
                        }
                        if (row.child(1).childNodes().size() >= 2) {
                            Node node = row.child(1).childNodes().get(1);
                            if (node instanceof Element && ((Element) node).text().equals(
                                    IIDXChartDifficulty.LEGGENDARIA.getAtWikiSuffix())) {
                                musicDifficulty = IIDXChartDifficulty.LEGGENDARIA;
                            }
                        }

                        int minBPM;
                        int maxBPM;
                        String[] bpms = row.child(2).text().split("-");
                        if (bpms.length == 1) {
                            minBPM = Integer.parseInt(bpms[0]);
                            maxBPM = minBPM;
                        } else {
                            minBPM = Integer.parseInt(bpms[0]);
                            maxBPM = Integer.parseInt(bpms[bpms.length - 1]);
                        }

                        int maxCombos = MiscUtils.parseInt(row.child(3).text(), 0);
                        int clearRate = MiscUtils.parseInt(row.child(4).text(), 0);

                        String ncdText = row.child(5).text().trim();
                        int normalClearDifficulty;
                        if (ncdText.equalsIgnoreCase("BAD")) {
                            normalClearDifficulty = 20;
                        } else if (ncdText.endsWith("+")) {
                            normalClearDifficulty = 16;
                        } else {
                            normalClearDifficulty = MiscUtils.parseInt(ncdText, 0);
                        }

                        String hcdText = row.child(6).text().trim();
                        int hardClearDifficulty;
                        if (hcdText.equalsIgnoreCase("乙")) {
                            hardClearDifficulty = 20;
                        } else if (hcdText.endsWith("+")) {
                            hardClearDifficulty = 16;
                        } else {
                            hardClearDifficulty = MiscUtils.parseInt(hcdText, 0);
                        }

                        List<IIDXChartSuggestOption> suggestions1p = new ArrayList<>();
                        for (Node child : row.child(7).childNodes()) {
                            if (!(child instanceof TextNode)) {
                                continue;
                            }
                            String nodeText = ((TextNode) child).text().trim();
                            for (IIDXChartSuggestOption option : IIDXChartSuggestOption.values()) {
                                if (nodeText.equals(option.getClickAgainName())) {
                                    if (!suggestions1p.contains(option)) {
                                        suggestions1p.add(option);
                                    }
                                    break;
                                }
                            }
                        }

                        List<IIDXChartSuggestOption> suggestions2p = new ArrayList<>();
                        for (Node child : row.child(8).childNodes()) {
                            if (!(child instanceof TextNode)) {
                                continue;
                            }
                            String nodeText = ((TextNode) child).text().trim();
                            for (IIDXChartSuggestOption option : IIDXChartSuggestOption.values()) {
                                if (nodeText.equals(option.getClickAgainName())) {
                                    if (!suggestions2p.contains(option)) {
                                        suggestions2p.add(option);
                                    }
                                    break;
                                }
                            }
                        }

                        String[] clickAgainCharacteristics = ((Element) row.child(9).childNodes()
                                .get(0)).text().split("、");

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 2; i < row.child(9).childNodes().size(); i++) {
                            Node child = row.child(9).childNodes().get(i);
                            if (!(child instanceof TextNode)) {
                                stringBuilder.append("\n");
                                continue;
                            }
                            String nodeText = ((TextNode) child).text().trim();
                            stringBuilder.append(nodeText);
                        }
                        String guide = stringBuilder.toString();

                        IIDXMusic music = new IIDXMusic(name, minBPM, maxBPM, firstVersion, musicFromConsole);
                        music = Database.getSavedIIDXMusicList(ctx).getSavedItem(music);
                        music.setIsRemoved(isRemoved);
                        music.setIsConsole(musicFromConsole);
                        IIDXChart existingChart = music.getCharts().get(musicDifficulty);
                        if (existingChart == null) {
                            existingChart = new IIDXChart(level, maxCombos, 0);
                        } else {
                            existingChart.setLevel(level);
                            existingChart.setMaxCombos(maxCombos);
                        }

                        if (existingChart.getClearRate() == 0) {
                            existingChart.setClearRate(clearRate);
                        }
                        Collections.addAll(existingChart.getClickAgainCharacteristics(), clickAgainCharacteristics);
                        existingChart.setClickAgainGuide(guide);
                        existingChart.setHardClearDifficulty(hardClearDifficulty);
                        existingChart.setNormalClearDifficulty(normalClearDifficulty);
                        existingChart.getSuggestions1P().addAll(suggestions1p);
                        existingChart.getSuggestions2P().addAll(suggestions2p);
                        existingChart.setAlias(alias);
                        music.getCharts().put(musicDifficulty, existingChart);
                        // Log.w("WTF-CA", gson.toJson(music));
                        Database.getSavedIIDXMusicList(ctx).addUnique(music);
                        itemsProcessed++;
                        publishProgress();
                    }

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (ctx instanceof Activity) {
                    ((Activity) ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, R.string.error_network_problem, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void loadAtWikiPage(final GuideURL guideURL) {
        String[] segments = guideURL.name().split("_");
        String digit = segments[segments.length - 1];
        final int level = MiscUtils.parseInt(digit, 0);
        MainActivity.getSyncHTTPClient().get(guideURL.getUrl(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                for (Element row : doc.select(".atwiki_tr_odd, .atwiki_tr_even")) {
                    int chartLevel = level;
                    if (row.child(3).text().trim().equals("notes")) {
                        continue;
                    }
                    IIDXVersion version = IIDXVersion.fromVersionDigit(row.child(0).text());
                    IIDXChartDifficulty difficulty = null;
                    String name = null;
                    String cellText = row.child(1).text();
                    for (IIDXChartDifficulty iterateDifficulty : IIDXChartDifficulty.values()) {
                        if (cellText.endsWith(iterateDifficulty.getAtWikiSuffix())) {
                            difficulty = iterateDifficulty;
                            name = cellText.replace(iterateDifficulty.getAtWikiSuffix(), "").trim();
                            break;
                        }
                    }
                    if (difficulty == null) {
                        continue;
                    }
                    int atWikiID;
                    try {
                        atWikiID = MiscUtils.parseInt(row.child(1).child(0).attr("href").split("pages/")[1].split(".html")[0], 0);
                    } catch (Exception e) {
                        atWikiID = 0;
                    }
                    int minBPM;
                    int maxBPM;
                    String[] bpms = row.child(2).text().split("-|－");
                    if (bpms.length == 1) {
                        minBPM = Integer.parseInt(bpms[0]);
                        maxBPM = minBPM;
                    } else {
                        minBPM = Integer.parseInt(bpms[0]);
                        maxBPM = Integer.parseInt(bpms[1]);
                    }
                    int maxCombo = MiscUtils.parseInt(row.child(3).text(), 0);
                    String[] characteristics = row.child(4).text().split("、|\\s|・");


                    int clearRate = 0;
                    if (guideURL == GuideURL.ATWIKI_INIT_PENDUAL) {
                        clearRate = MiscUtils.parseInt(row.child(6).text().split("%")[0], 0);
                        if (chartLevel == 0) {
                            try {
                                chartLevel = MiscUtils.parseInt(row.parent().parent()
                                        .previousElementSibling().text().replace("☆", ""), 0);
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    IIDXChart chart = new IIDXChart(chartLevel, maxCombo, atWikiID);
                    chart.setClearRate(clearRate);
                    Collections.addAll(chart.getAtWikiCharacteristics(), characteristics);
                    IIDXMusic music = new IIDXMusic(name, minBPM, maxBPM, version, IIDXVersion.isConsole(row.child(0).text()));
                    music = Database.getSavedIIDXMusicList(ctx).getSavedItem(music);
                    music.getCharts().put(difficulty, chart);
                    // Log.w("WTF-AT", gson.toJson(music));
                    Database.getSavedIIDXMusicList(ctx).addUnique(music);
                    itemsProcessed++;
                    publishProgress();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (ctx instanceof Activity) {
                    ((Activity) ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, R.string.error_network_problem, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


}
