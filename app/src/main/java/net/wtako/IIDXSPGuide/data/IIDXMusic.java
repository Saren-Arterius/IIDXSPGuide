package net.wtako.IIDXSPGuide.data;

import net.wtako.IIDXSPGuide.activities.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IIDXMusic {

    static List<String> sameSongs = Arrays.asList("ALL MY TURN", "Anisakis", "NEW GENERATION",
            "SPECIAL SUMMER", "ALFARSHEAR", "Apocalypse", "Beginning of life", "Bleeding Luv",
            "Blind Justice", "CHOO", "CRYMSON", "裁き", "DENJIN", "Final Count Down", "Indigo Vision",
            "JOURNEY TO", "Let the Snow", "Light and Cyber", "Look To The", "MARIA", "NEW SENSATION",
            "仮面", "Raspberry", "SPRING RAIN", "STARS", "SWITCH", "VOYAGER", "TYPE MARS", "ToyCube",
            "Vermil", "Winning Eleven", "carumba", "beatchic", "believe", "dissolve", "entrance",
            "quell", "smoooo", "u gotta", "with me", "エブリデイ", "オレはビートマニア", "BEMANI FOR YOU",
            "零", "青少年", "華爛漫", "花吹", "cannibal", "旋律", "恋愛レボリューション", "太陽", "合体せよ",
            "Chiharu", "ミラージュ", "フェティッシュペイパー", "蠍火", "Peptide", "キャッシュレス",
            "アタック", "もっと", "Session 9", "CN Ver", "YELLOW FROG", "Heavenly Sun", "HYPER EURO");
    String name;
    int minBPM;
    int maxBPM;
    IIDXVersion firstVersion;
    boolean isConsole;
    boolean isRemoved;
    transient String cmpName;
    transient String searchMatch;
    transient Integer sameSongIndex;
    Map<IIDXChartDifficulty, IIDXChart> charts = new HashMap<>();

    public IIDXMusic(String name, int minBPM, int maxBPM, IIDXVersion firstVersion, boolean isConsole) {
        this.name = name;
        this.minBPM = minBPM;
        this.maxBPM = maxBPM;
        this.firstVersion = firstVersion;
        this.isConsole = isConsole;
    }

    public String getSearchMatch() {
        if (searchMatch != null) {
            return searchMatch;
        }
        searchMatch = MainActivity.gson.toJson(this).replace("\"", "").replace('_', ' ').toLowerCase();

        return searchMatch;
    }

    public String getBPMDisplay() {
        StringBuilder builder = new StringBuilder();
        builder.append("BPM: ");
        builder.append(minBPM);
        if (maxBPM != minBPM) {
            builder.append(" - ");
            builder.append(maxBPM);
        }
        return builder.toString();
    }

    public List<IIDXChartDifficulty> sortedChartDifficulties() {
        List<IIDXChartDifficulty> sorted = new ArrayList<>();
        for (IIDXChartDifficulty chartDifficulty : IIDXChartDifficulty.values()) {
            if (charts.containsKey(chartDifficulty)) {
                sorted.add(chartDifficulty);
            }
        }
        return sorted;
    }

    public String getDifficultyDisplay() {
        StringBuilder builder = new StringBuilder();
        boolean firstDiff = true;
        for (IIDXChartDifficulty chartDifficulty : sortedChartDifficulties()) {
            if (!firstDiff) {
                builder.append(" / ");
            }
            builder.append(chartDifficulty.name().charAt(0));
            builder.append(": ");
            int lvl = charts.get(chartDifficulty).getLevel();
            builder.append(lvl == 0 ? "?" : lvl);
            firstDiff = false;
        }
        return builder.toString();
    }

    public String getCombosDisplay() {
        StringBuilder builder = new StringBuilder();
        boolean firstDiff = true;
        for (IIDXChartDifficulty chartDifficulty : sortedChartDifficulties()) {
            if (!firstDiff) {
                builder.append(" / ");
            }
            int combos = charts.get(chartDifficulty).getMaxCombos();
            builder.append(combos == 0 ? "?" : combos);
            firstDiff = false;
        }
        return builder.toString();
    }


    public void setIsConsole(boolean isConsole) {
        this.isConsole = isConsole;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public String getName() {
        return name;
    }

    public IIDXVersion getFirstVersion() {
        return firstVersion;
    }

    public Map<IIDXChartDifficulty, IIDXChart> getCharts() {
        return charts;
    }

    public int getSameSongIndex() {
        if (sameSongIndex != null) {
            return sameSongIndex;
        }
        for (int i = 0; i < sameSongs.size(); i++) {
            if (name.contains(sameSongs.get(i))) {
                sameSongIndex = i;
                break;
            }
        }
        if (sameSongIndex == null) {
            sameSongIndex = -1;
        }
        return sameSongIndex;
    }

    public String getCmpName() {
        if (cmpName != null) {
            return cmpName;
        }
        cmpName = name.replace(" ", "").toLowerCase();
        return cmpName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IIDXMusic)) {
            return false;
        }
        IIDXMusic other = (IIDXMusic) o;
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (other.name == null || other.name.isEmpty()) {
            return false;
        }
        /*
        if (!(firstVersion == IIDXVersion.CONSOLE_OR_UNKNOWN &&
                other.firstVersion != IIDXVersion.CONSOLE_OR_UNKNOWN)) {
            if (other.firstVersion != firstVersion) {
                return false;
            }
            if (other.isConsole != isConsole) {
                return false;
            }
        }
        */
        return getSameSongIndex() != -1 && other.name.contains(sameSongs.get(getSameSongIndex())) ||
                getCmpName().equals(other.getCmpName());
    }


}
