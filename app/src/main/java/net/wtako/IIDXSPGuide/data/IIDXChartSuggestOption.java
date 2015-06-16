package net.wtako.IIDXSPGuide.data;

public enum IIDXChartSuggestOption {
    NORMAL("正"),
    MIRROR("鏡"),
    RANDOM("乱"),
    SRANDOM("S乱"),
    RRANDOM("R乱"),
    HARD("難");

    private final String clickAgainName;

    IIDXChartSuggestOption(String clickAgainName) {
        this.clickAgainName = clickAgainName;
    }

    public String getClickAgainName() {
        return clickAgainName;
    }

    @Override
    public String toString() {
        return clickAgainName;
    }
}
