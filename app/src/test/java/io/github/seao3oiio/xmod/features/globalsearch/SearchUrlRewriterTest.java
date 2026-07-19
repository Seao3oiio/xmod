package io.github.seao3oiio.xmod.features.globalsearch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class SearchUrlRewriterTest {
    @Test
    public void rewritesBaiduWordQuery() {
        assertEquals(
                "https://www.google.com/search?q=hello%20world",
                SearchUrlRewriter.rewrite("https://m.baidu.com/s?word=hello+world&from=1001192y")
        );
    }

    @Test
    public void rewritesBaiduWdQueryAndKeepsUnicode() {
        assertEquals(
                "https://www.google.com/search?q=%E6%B5%8B%E8%AF%95",
                SearchUrlRewriter.rewrite("https://www.baidu.com/s?wd=%E6%B5%8B%E8%AF%95")
        );
    }

    @Test
    public void rewritesSogouKeywordQuery() {
        assertEquals(
                "https://www.google.com/search?q=xposed%20module",
                SearchUrlRewriter.rewrite(
                        "https://m.sogou.com/web/searchList.jsp?keyword=xposed%20module&pid=sogou-mobp"
                )
        );
    }

    @Test
    public void leavesNonSearchPagesAlone() {
        String original = "https://baike.baidu.com/item/Google";
        assertEquals(original, SearchUrlRewriter.rewrite(original));
    }

    @Test
    public void leavesOtherBaiduSubdomainsAlone() {
        String original = "https://pan.baidu.com/s?wd=private-share";
        assertEquals(original, SearchUrlRewriter.rewrite(original));
    }

    @Test
    public void leavesSearchHomepageWithoutQueryAlone() {
        String original = "https://www.baidu.com/";
        assertEquals(original, SearchUrlRewriter.rewrite(original));
    }

    @Test
    public void leavesAlreadyGoogleSearchAlone() {
        String original = "https://www.google.com/search?q=test";
        assertEquals(original, SearchUrlRewriter.rewrite(original));
    }
}
