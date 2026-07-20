package io.github.seao3oiio.xmod.features.globalsearch;

final class GlobalSearchSubmitPolicy {
    private GlobalSearchSubmitPolicy() {
    }

    static boolean shouldOpen(String query, String source) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        return "search_keyboard".equals(source) || "search_btn".equals(source);
    }
}
