package io.github.seao3oiio.xmod.features.globalsearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GlobalSearchSubmitPolicyTest {
    @Test
    public void acceptsKeyboardSubmission() {
        assertTrue(GlobalSearchSubmitPolicy.shouldOpen("xmod", "search_keyboard"));
    }

    @Test
    public void acceptsSearchButtonSubmission() {
        assertTrue(GlobalSearchSubmitPolicy.shouldOpen("xmod", "search_btn"));
    }

    @Test
    public void rejectsLiveInput() {
        assertFalse(GlobalSearchSubmitPolicy.shouldOpen("xmod", "input"));
    }

    @Test
    public void rejectsInputFocus() {
        assertFalse(GlobalSearchSubmitPolicy.shouldOpen("xmod", "input_focus"));
    }

    @Test
    public void rejectsUnknownSource() {
        assertFalse(GlobalSearchSubmitPolicy.shouldOpen("xmod", "suggestion"));
    }

    @Test
    public void rejectsMissingQuery() {
        assertFalse(GlobalSearchSubmitPolicy.shouldOpen(null, "search_keyboard"));
        assertFalse(GlobalSearchSubmitPolicy.shouldOpen("   ", "search_keyboard"));
    }
}
