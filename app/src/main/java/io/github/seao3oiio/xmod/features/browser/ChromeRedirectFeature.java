package io.github.seao3oiio.xmod.features.browser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;

import io.github.seao3oiio.xmod.XmodFeature;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class ChromeRedirectFeature implements XmodFeature {
    private static final String TAG = "xmod/ChromeRedirect";
    private static final String CHROME_PACKAGE = "com.android.chrome";
    private static final long DUPLICATE_WINDOW_MILLIS = 2_000L;
    private static String lastRedirectUrl;
    private static long lastRedirectAt;

    @Override
    public String name() {
        return "OEM browser to Chrome";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return BrowserRedirectPolicy.supportsPackage(loadPackageParam.packageName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        redirectIfNeeded((Activity) param.thisObject,
                                ((Activity) param.thisObject).getIntent());
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onNewIntent",
                Intent.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        redirectIfNeeded((Activity) param.thisObject, (Intent) param.args[0]);
                    }
                }
        );
    }

    private static void redirectIfNeeded(Activity activity, Intent incoming) {
        if (incoming == null) {
            return;
        }

        Uri uri = incoming.getData();
        if (uri == null || !BrowserRedirectPolicy.isWebScheme(uri.getScheme())) {
            return;
        }

        String url = uri.toString();
        long redirectAt = SystemClock.elapsedRealtime();
        if (!claimRedirect(url, redirectAt)) {
            activity.finish();
            return;
        }

        Intent chromeIntent = new Intent(Intent.ACTION_VIEW, uri)
                .setPackage(CHROME_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivity(chromeIntent);
            activity.finish();
            XposedBridge.log(TAG + ": redirected web intent to Chrome");
        } catch (ActivityNotFoundException error) {
            releaseRedirect(url, redirectAt);
            XposedBridge.log(TAG + ": Chrome unavailable; kept OEM browser fallback");
        } catch (RuntimeException error) {
            releaseRedirect(url, redirectAt);
            XposedBridge.log(TAG + ": redirect failed; kept OEM browser fallback");
            XposedBridge.log(error);
        }
    }

    private static synchronized boolean claimRedirect(String url, long now) {
        if (url.equals(lastRedirectUrl)
                && now - lastRedirectAt < DUPLICATE_WINDOW_MILLIS) {
            return false;
        }
        lastRedirectUrl = url;
        lastRedirectAt = now;
        return true;
    }

    private static synchronized void releaseRedirect(String url, long redirectAt) {
        if (url.equals(lastRedirectUrl) && redirectAt == lastRedirectAt) {
            lastRedirectUrl = null;
            lastRedirectAt = 0L;
        }
    }
}
