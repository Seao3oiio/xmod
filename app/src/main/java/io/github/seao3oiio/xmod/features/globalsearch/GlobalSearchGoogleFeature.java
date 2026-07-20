package io.github.seao3oiio.xmod.features.globalsearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import io.github.seao3oiio.xmod.XmodFeature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class GlobalSearchGoogleFeature implements XmodFeature {
    private static final String TAG = "xmod/GlobalSearch";
    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final String MAIN_SEARCH_BAR =
            "com.heytap.quicksearchbox.ui.widget.searchbar.MainSearchBar";
    private static final Set<String> TARGET_PACKAGES = new HashSet<>(Arrays.asList(
            "com.heytap.quicksearchbox",
            "com.oplus.quicksearchbox",
            "com.oppo.quicksearchbox"
    ));

    @Override
    public String name() {
        return "Global Search to Google in Chrome";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return TARGET_PACKAGES.contains(loadPackageParam.packageName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> mainSearchBar = XposedHelpers.findClassIfExists(
                MAIN_SEARCH_BAR,
                loadPackageParam.classLoader
        );
        if (mainSearchBar == null) {
            XposedBridge.log(TAG + ": compatible MainSearchBar class not found");
            return;
        }

        XposedHelpers.findAndHookMethod(
                mainSearchBar,
                "A1",
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        interceptSubmittedSearch(param);
                    }
                }
        );
    }

    private static void interceptSubmittedSearch(XC_MethodHook.MethodHookParam param) {
        String source = param.args[1] instanceof String
                ? (String) param.args[1]
                : "";
        String rawQuery = param.args[0] instanceof String
                ? (String) param.args[0]
                : null;
        if (!GlobalSearchSubmitPolicy.shouldOpen(rawQuery, source)) {
            return;
        }

        String query = rawQuery.trim();
        if (openInChrome((View) param.thisObject, query)) {
            param.setResult(null);
        }
    }

    private static boolean openInChrome(View searchBar, String query) {
        Uri googleSearch = Uri.parse(GoogleSearchUrl.build(query));
        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, googleSearch)
                .setPackage(CHROME_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Context context = searchBar.getContext();
        if (!(context instanceof Activity)) {
            chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(chromeIntent);
            XposedBridge.log(TAG + ": submitted search opened in Chrome");
            return true;
        } catch (RuntimeException error) {
            XposedBridge.log(TAG + ": Chrome launch failed; kept Global Search fallback");
            XposedBridge.log(error);
            return false;
        }
    }
}
