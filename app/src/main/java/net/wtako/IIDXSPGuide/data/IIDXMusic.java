package net.wtako.IIDXSPGuide.data;

import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.utils.MiscUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IIDXMusic implements Serializable {

    static Map<String, String> cmpNames = new HashMap<>();
    String name;
    int minBPM;
    int maxBPM;
    IIDXVersion firstVersion;
    boolean isConsole;
    boolean isRemoved;
    transient String searchMatch;
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

    public String getCmpName() {
        String cmpName = cmpNames.get(name);
        if (cmpName == null) {
            cmpName = MiscUtils.deAccent(name.replace(" ", "").replace("\t", "").replace(".", "")
                    .replace('～', '~').replace('〜', '~').replace('！', '!')
                    .replace("'", "").split("-")[0]).toLowerCase(); // 12316
            cmpNames.put(name, cmpName);
        }
        return cmpName;
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

    public int getMinBPM() {
        return minBPM;
    }

    public int getMaxBPM() {
        return maxBPM;
    }

    public IIDXVersion getFirstVersion() {
        return firstVersion;
    }

    public void setFirstVersion(IIDXVersion firstVersion) {
        this.firstVersion = firstVersion;
    }

    public boolean isConsole() {
        return isConsole;
    }

    public Map<IIDXChartDifficulty, IIDXChart> getCharts() {
        return charts;
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
        if (!getCmpName().equals(other.getCmpName())) {
            return false;
        }
        return true;
    }


}
