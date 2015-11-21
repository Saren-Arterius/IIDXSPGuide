package net.wtako.IIDXSPGuide.data;

public enum IIDXChartDifficulty {
    NORMAL("(N)", "NORMAL"),
    HYPER("(H)", "HYPER"),
    ANOTHER("(A)", "ANOTHER"),
    BLACK_ANOTHER("(黒)", "DARK"),
    LEGGENDARIA("†LEGGENDARIA", "LEGGENDARIA");

    private final String atWikiSuffix;
    private final String clickAgainName;

    IIDXChartDifficulty(String atWikiSuffix, String clickAgainName) {
        this.atWikiSuffix = atWikiSuffix;
        this.clickAgainName = clickAgainName;
    }

    public String getClickAgainName() {
        return clickAgainName;
    }

    public String getAtWikiSuffix() {
        return atWikiSuffix;
    }
}
