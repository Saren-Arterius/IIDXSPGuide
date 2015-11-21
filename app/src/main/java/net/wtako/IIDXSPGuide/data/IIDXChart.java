package net.wtako.IIDXSPGuide.data;

import net.wtako.IIDXSPGuide.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class IIDXChart {

    int level;
    int maxCombos;
    int clearRate;
    int normalClearDifficulty;
    int hardClearDifficulty;
    int atWikiID;
    List<IIDXChartSuggestOption> suggestions1P = new ArrayList<>();
    List<IIDXChartSuggestOption> suggestions2P = new ArrayList<>();
    List<String> clickAgainCharacteristics = new ArrayList<>();
    List<String> atWikiCharacteristics = new ArrayList<>();
    List<String> atWikiComments = new ArrayList<>();
    String clickAgainGuide;
    String alias;

    public IIDXChart(int level, int maxCombos, int atWikiID) {
        this.level = level;
        this.maxCombos = maxCombos;
        this.atWikiID = atWikiID;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getAtWikiID() {
        return atWikiID;
    }

    public List<IIDXChartSuggestOption> getSuggestions1P() {
        return suggestions1P;
    }

    public List<IIDXChartSuggestOption> getSuggestions2P() {
        return suggestions2P;
    }

    public List<String> getClickAgainCharacteristics() {
        return clickAgainCharacteristics;
    }

    public List<String> getAtWikiCharacteristics() {
        return atWikiCharacteristics;
    }

    public String getCharacteristicsDisplay() {
        List<String> chs = new ArrayList<>();
        for (String ch : clickAgainCharacteristics) {
            ch = ch.replace('2', '二').replace('２', '二');
            if (!chs.contains(ch)) {
                chs.add(ch);
            }
        }
        for (String ch : atWikiCharacteristics) {
            ch = ch.replace('2', '二').replace('２', '二');
            String shorterCh = null;
            boolean dontAdd = false;
            for (String ech : chs) {
                if (ch.contains(ech)) {
                    shorterCh = ech;
                    break;
                } else if (ech.contains(ch)) {
                    dontAdd = true;
                    break;
                }
            }
            if (shorterCh != null) {
                chs.remove(shorterCh);
            }
            if (!dontAdd) {
                chs.add(ch);
            }
        }
        return MiscUtils.humanList("、", chs);
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxCombos() {
        return maxCombos;
    }

    public void setMaxCombos(int maxCombos) {
        this.maxCombos = maxCombos;
    }

    public int getClearRate() {
        return clearRate;
    }

    public void setClearRate(int clearRate) {
        this.clearRate = clearRate;
    }

    public int getNormalClearDifficulty() {
        return normalClearDifficulty;
    }

    public void setNormalClearDifficulty(int normalClearDifficulty) {
        this.normalClearDifficulty = normalClearDifficulty;
    }

    public int getHardClearDifficulty() {
        return hardClearDifficulty;
    }

    public void setHardClearDifficulty(int hardClearDifficulty) {
        this.hardClearDifficulty = hardClearDifficulty;
    }

    public String getClickAgainGuide() {
        return clickAgainGuide;
    }

    public void setClickAgainGuide(String clickAgainGuide) {
        this.clickAgainGuide = clickAgainGuide;
    }

    public List<String> getAtWikiComments() {
        return atWikiComments;
    }

}
