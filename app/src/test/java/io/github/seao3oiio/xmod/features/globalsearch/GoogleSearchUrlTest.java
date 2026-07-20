package io.github.seao3oiio.xmod.features.globalsearch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GoogleSearchUrlTest {
    @Test
    public void encodesSpaces() {
        assertEquals(
                "https://www.google.com/search?q=hello%20world",
                GoogleSearchUrl.build("hello world")
        );
    }

    @Test
    public void encodesUnicode() {
        assertEquals(
                "https://www.google.com/search?q=%E6%B5%8B%E8%AF%95",
                GoogleSearchUrl.build("测试")
        );
    }

    @Test
    public void encodesReservedCharacters() {
        assertEquals(
                "https://www.google.com/search?q=xmod%26chrome%3Dyes",
                GoogleSearchUrl.build("xmod&chrome=yes")
        );
    }
}
