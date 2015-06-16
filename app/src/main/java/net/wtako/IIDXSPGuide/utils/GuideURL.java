package net.wtako.IIDXSPGuide.utils;

public enum GuideURL {
    CLICKAGAIN_ALL_MUSIC("http://clickagain.sakura.ne.jp/cgi-bin/sort11/data.cgi?level8=1&level9=1&level10=1&level11=1&level12=1&ver0.5=1&ver0=1"),
    ATWIKI_BASE("http://www19.atwiki.jp/bemani2sp/pages/{0}.html"),
    ATWIKI_INIT_LEVEL_BLACK_ANOTHER("http://www19.atwiki.jp/bemani2sp/pages/1770.html"),
    ATWIKI_INIT_PENDUAL("http://www19.atwiki.jp/bemani2sp/pages/2321.html"),
    ATWIKI_INIT_LEVEL_7("http://www19.atwiki.jp/bemani2sp/pages/523.html"),
    ATWIKI_INIT_LEVEL_8("http://www19.atwiki.jp/bemani2sp/pages/512.html"),
    ATWIKI_INIT_LEVEL_9("http://www19.atwiki.jp/bemani2sp/pages/457.html"),
    ATWIKI_INIT_LEVEL_10("http://www19.atwiki.jp/bemani2sp/pages/270.html"),
    ATWIKI_INIT_LEVEL_11("http://www19.atwiki.jp/bemani2sp/pages/14.html"),
    ATWIKI_INIT_LEVEL_12("http://www19.atwiki.jp/bemani2sp/pages/17.html");

    private final String url;

    GuideURL(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
