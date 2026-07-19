package io.github.seao3oiio.xmod.features.globalsearch;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

import io.github.seao3oiio.xmod.XmodFeature;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class GlobalSearchGoogleFeature implements XmodFeature {
    private static final String TAG = "xmod/GlobalSearch";
    private static final String GOOGLE_NAME = "Google";
    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search?q=";
    private static final String SEARCH_ENGINE_MANAGER =
            "com.heytap.quicksearchbox.common.manager.SearchEngineManager";
    private static final String WEB_ENGINE_INFO = "com.heytap.common.bean.WebEngineInfo";
    private static final String VENDOR_WEB_VIEW =
            "com.heytap.browser.export.webview.WebView";
    private static final Set<String> TARGET_PACKAGES = new HashSet<>(Arrays.asList(
            "com.heytap.quicksearchbox",
            "com.oplus.quicksearchbox",
            "com.oppo.quicksearchbox"
    ));
    private static volatile Object googleEngineInfo;

    private static final XC_MethodHook STRING_URL_HOOK = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            if (!(param.args[0] instanceof String)) {
                return;
            }
            String original = (String) param.args[0];
            String rewritten = SearchUrlRewriter.rewrite(original);
            if (!original.equals(rewritten)) {
                param.args[0] = rewritten;
                logRewrite();
            }
        }
    };

    private static final XC_MethodHook URI_ARGUMENT_HOOK = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            if (!(param.args[0] instanceof Uri)) {
                return;
            }
            Uri originalUri = (Uri) param.args[0];
            String original = originalUri.toString();
            String rewritten = SearchUrlRewriter.rewrite(original);
            if (!original.equals(rewritten)) {
                param.args[0] = Uri.parse(rewritten);
                logRewrite();
            }
        }
    };

    @Override
    public String name() {
        return "Global Search to Google";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return TARGET_PACKAGES.contains(loadPackageParam.packageName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookStringUrlEntryPoints();
        hookUriIntentEntryPoints();
        hookVendorWebView(loadPackageParam.classLoader);
        hookSearchEngineManager(loadPackageParam.classLoader);
        hookOptionalCustomTabs(loadPackageParam.classLoader);
    }

    private static void hookVendorWebView(ClassLoader classLoader) {
        Class<?> vendorWebView = XposedHelpers.findClassIfExists(VENDOR_WEB_VIEW, classLoader);
        if (vendorWebView == null) {
            XposedBridge.log(TAG + ": vendor WebView class not found");
            return;
        }

        XposedHelpers.findAndHookMethod(
                vendorWebView,
                "loadUrl",
                String.class,
                STRING_URL_HOOK
        );
        XposedHelpers.findAndHookMethod(
                vendorWebView,
                "loadUrl",
                String.class,
                Map.class,
                STRING_URL_HOOK
        );
        XposedHelpers.findAndHookMethod(
                vendorWebView,
                "loadUrl",
                String.class,
                Map.class,
                boolean.class,
                STRING_URL_HOOK
        );
        XposedHelpers.findAndHookMethod(
                vendorWebView,
                "loadUrlEx",
                String.class,
                Map.class,
                boolean.class,
                boolean.class,
                boolean.class,
                STRING_URL_HOOK
        );
    }

    private static void hookSearchEngineManager(ClassLoader classLoader) {
        Class<?> manager = XposedHelpers.findClassIfExists(SEARCH_ENGINE_MANAGER, classLoader);
        Class<?> engineInfo = XposedHelpers.findClassIfExists(WEB_ENGINE_INFO, classLoader);
        if (manager == null || engineInfo == null) {
            XposedBridge.log(TAG + ": search engine classes not found; URL fallback remains active");
            return;
        }

        XposedHelpers.findAndHookMethod(
                manager,
                "loadSearchEngineList",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Object google = getGoogleEngineInfo(engineInfo);
                        Object result = param.getResult();
                        if (result instanceof List) {
                            try {
                                @SuppressWarnings("unchecked")
                                List<Object> engines = (List<Object>) result;
                                engines.clear();
                                engines.add(google);
                                XposedBridge.log(TAG + ": replaced engine list with Google");
                                return;
                            } catch (RuntimeException error) {
                                XposedBridge.log(TAG + ": engine list was immutable: " + error);
                            }
                        }
                        param.setResult(Collections.singletonList(google));
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                manager,
                "getCurrentSearchEngineInfo",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(getGoogleEngineInfo(engineInfo));
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                manager,
                "getCurrentEngineName",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(GOOGLE_NAME + "搜索");
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                manager,
                "getCurrentEngineUrl",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(GOOGLE_SEARCH_URL);
                    }
                }
        );
    }

    private static Object getGoogleEngineInfo(Class<?> engineInfoClass) {
        Object existing = googleEngineInfo;
        if (existing != null) {
            return existing;
        }

        synchronized (GlobalSearchGoogleFeature.class) {
            if (googleEngineInfo == null) {
                Object created = XposedHelpers.newInstance(engineInfoClass);
                XposedHelpers.callMethod(created, "setName", GOOGLE_NAME);
                XposedHelpers.callMethod(created, "setUrl", GOOGLE_SEARCH_URL);
                XposedHelpers.callMethod(created, "setCardId", "google");
                XposedHelpers.callMethod(created, "setEngineChannel", "google");
                googleEngineInfo = created;
            }
            return googleEngineInfo;
        }
    }

    private static void hookStringUrlEntryPoints() {
        XposedHelpers.findAndHookMethod(Uri.class, "parse", String.class, STRING_URL_HOOK);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, STRING_URL_HOOK);
        XposedHelpers.findAndHookMethod(WebView.class, "loadUrl", String.class, STRING_URL_HOOK);
        XposedHelpers.findAndHookMethod(
                WebView.class,
                "loadUrl",
                String.class,
                Map.class,
                STRING_URL_HOOK
        );
    }

    private static void hookUriIntentEntryPoints() {
        XposedHelpers.findAndHookMethod(Intent.class, "setData", Uri.class, URI_ARGUMENT_HOOK);
        XposedHelpers.findAndHookMethod(
                Intent.class,
                "setDataAndType",
                Uri.class,
                String.class,
                URI_ARGUMENT_HOOK
        );
        XposedHelpers.findAndHookConstructor(
                Intent.class,
                String.class,
                Uri.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        rewriteUriArgument(param, 1);
                    }
                }
        );
    }

    private static void hookOptionalCustomTabs(ClassLoader classLoader) {
        Class<?> customTabsIntent = XposedHelpers.findClassIfExists(
                "androidx.browser.customtabs.CustomTabsIntent",
                classLoader
        );
        if (customTabsIntent == null) {
            return;
        }

        XposedHelpers.findAndHookMethod(
                customTabsIntent,
                "launchUrl",
                android.content.Context.class,
                Uri.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        rewriteUriArgument(param, 1);
                    }
                }
        );
    }

    private static void rewriteUriArgument(XC_MethodHook.MethodHookParam param, int index) {
        if (!(param.args[index] instanceof Uri)) {
            return;
        }
        Uri originalUri = (Uri) param.args[index];
        String original = originalUri.toString();
        String rewritten = SearchUrlRewriter.rewrite(original);
        if (!original.equals(rewritten)) {
            param.args[index] = Uri.parse(rewritten);
            logRewrite();
        }
    }

    private static void logRewrite() {
        // Search terms are deliberately excluded from persistent Xposed logs.
        XposedBridge.log(TAG + ": redirected vendor search URL to Google");
    }
}
