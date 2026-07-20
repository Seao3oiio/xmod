package io.github.seao3oiio.xmod.features.guitartuna;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.Base64;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.seao3oiio.xmod.XmodFeature;

public final class GuitarTunaRecoveryFeature implements XmodFeature {
    private static final String TAG = "xmod/GuitarTunaRecovery";
    private static final String TARGET_PACKAGE = "com.ovelin.guitartuna";
    private static final String TARGET_ACTIVITY =
            "com.yousician.yousiciannative.GuitarTunaActivity";
    private static final String RECOVERY_PREFS =
            "io.github.seao3oiio.xmod.guitartuna_recovery";
    private static final String LAST_RECOVERY_AT = "last_recovery_at";
    private static final String LAST_RECOVERY_VERSION = "last_recovery_version";
    private static final String ONBOARDING_PENDING = "onboarding_pending";

    private static final int SAMPLE_WIDTH = 64;
    private static final int SAMPLE_HEIGHT = 128;
    private static final int TEMPLATE_WIDTH = 8;
    private static final int TEMPLATE_HEIGHT = 16;
    private static final byte[] SPLASH_TEMPLATE = Base64.decode(
            "LS4zLS4zLS4zLS4zLS4zLS4zLS4zLS4zLC0yLC0yLC0yLC0yLC0yLC0yLC0yLC0yKywxKywx"
                    + "KywxKywxKywxKywxKywxKywxKisvKisvKisvKisvKisvKisvKisvKisvKSouKSouKSouKSou"
                    + "KSouKSouKSouKSouKCktKCktKCktKCktKCktKCktKCktKCktOz1AREVIPj9CLDM1IT05IzU0"
                    + "JicrJicrJSYqJSYqJSYqKSotJScrJScrJSYqJSYqJCUoJCUoJCUoJCUoJCUoJCUoJCUoJCUo"
                    + "IyQnIyQnIyQnIyQnIyQnIyQnIyQnIyQnIiMmIiMmIiMmIiMmIiMmIiMmIiMmIiMmICEkICEk"
                    + "ICEkICEkICEkICEkICEkICEkHyAjHyAjHyAjHyAjHyAjHyAjHyAjHyAjHh8hHh8hQkNFSUpM"
                    + "JlJHGTs0Hh8hHh8hHR4gHR4gJCUnHCQkHCQkHR4gHR4gHR4gHB0fHB0fHB0fHB0fHB0fHB0f"
                    + "HB0fHB0f",
            Base64.DEFAULT
    );
    private static final int SCREEN_WELCOME = 0;
    private static final int SCREEN_INSTRUMENT = 1;
    private static final int SCREEN_LEVEL = 2;
    private static final int SCREEN_MOTIVATION = 3;
    private static final int SCREEN_CUSTOMIZE = 4;
    private static final int SCREEN_MICROPHONE = 5;
    private static final int SCREEN_NOTIFICATIONS = 6;
    private static final int SCREEN_TUNER = 7;
    private static final byte[][] ONBOARDING_TEMPLATES = {
            decodeTemplate(
                    "oJF6ZFxKX0U3TzgxUjszbko4qodlz76kKC0uLC0tNy8sMCkoUjkxa0g4oH1bybSVERogHCIm"
                            + "IykrKi8xQT89YlVNb11RdGtcOzo1YFVIi31uopuRqKKbpaGako+KZmZjinNbjXRddmJShXFo"
                            + "p4d3rJOEfWxkYVhUV0lANzMwGSAkKCcpakg7h2NRiGxbSEZEExsgEBsfERsgGiAlVj02WT41"
                            + "hWJNPTg0EBofERsgFx8iSzo0Y0I4XUA1fV1ILy8tGRseFhodIiEiPTArMyglMSclOC8rHyAh"
                            + "IyQmNzY4MzM1MzQ2Nzk7Li8yGhweGxweISkpMktGMT89ODg6Nzc5LzAyHB0fGxweHSMkHzgz"
                            + "IjAuIyQmIyQmJCUnICEjGxweICEjLSwuLS0vLS4wLi8xLi8xJCUnGxweGxweGxweGxweGxwe"
                            + "GxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweICAiIiIkHBweGxweGxweGxwe"
                            + "GxweGxwe"
            ),
            decodeTemplate(
                    "GRkbGRkbGRkbGRkbGRkbGRkbGhocGRkbHBsdIiEjHx8hHh4gHR0fHBweGRkbGhocIyYnSFBP"
                            + "RE1MR0pKTExOPz9BGxsdGRkbGSEgFjoxFkA1NENATUxOOzs9GxsdGRkbHR4gJSYpJygqHh8i"
                            + "Hx8iJiYoIiEkHR4gLSsuaF1Zdm9qLy4vPTxAjoSDW09OMC0wListSDkxQDcuLiwsSUdLq5SO"
                            + "dWRhLywvKCktMjI1LzI0KisvLS4yWFNTTEtOJygsJSUpLC8zLTI1JycrJicrKS4xKCwvJSUp"
                            + "KCktTU5OPTY3KSktMC4wZ1RLaWFfLCwwLSsuTDw0Z1FGOzc4S0dItZmMnId/NjM0KSktNjAw"
                            + "SkA8MTAzNDU6cWppS0JBKSksJicqLTE1LTQ3KCgtKCgtLjA1LzE2JicrJygrUE1QUk9RKSkt"
                            + "KiotUkZFQjk6JSUpNzU4lIOAlYSBOzg8PjY2c1BEW0hBMC8xLCsvbl9dhHd3NzY6NDEyTzkz"
                            + "TENBLy4x"
            ),
            decodeTemplate(
                    "FRUWFRUWFRUWFRUWFRUWFRUWFhYXFRUWGRkaHRwdGxocGhobGhobFhYXFRUWFhYXJCcnPkhG"
                            + "P0pHOkVDOkRCGx0eFRUWFhYXFiIfGDgwGToxGTkxGTgwGh8fGBgZFRUWJCQmLi4wKystKCgq"
                            + "KSkrKSkrJiYoHh4gNzg7SktPREVJOjs/MjM3MjM3MjM3LC0wNjc5RUZKOjs+Njc7MjM2MTI1"
                            + "MTI2KysuMzM2QkNGPT5BOjs+MTI2LzA0MDA0KSotODg7TE1RTU1RRUZKODk9MjM3MzQ3LC0w"
                            + "MjM2Q0NHQkNHQkNGNzg8MDE0MDE1KistNDQ3RkZKRkZKQkNGMzQ4MDE0MDE1KisuOTk8TU1R"
                            + "TU5RTE1QPD1BNDU4MzQ4LC0wISEjJygqJycqJygqJSUoICEjICAjHR4gFRUWFRUWFRUWFRUW"
                            + "FRUWFRUWFRUWFRUWFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYXFhYX"
                            + "FhYXFhYX"
            ),
            decodeTemplate(
                    "FhYYFhYYFhYYFhYYFhYYFhYYFhYYFhYYIB8hIB4hHRseGxsdFRoaFRUXFRUXFhYYKjQzN0pG"
                            + "MUVBN0VDKEE8Ky8wKSkqGRkbFiIhFy0pFy0pISwrMDAyNDQ2MDAyGhocLCsvQ0FHQ0FHREJH"
                            + "Q0JHQkJHOzo/KSgsKysvQkJHQkFGQkJHQ0JHQUBFPDtBKSktLSwwRUVKRURKRkVLR0ZLQkFG"
                            + "PDtBKiotLS0xRkZLRkVLR0ZMR0ZLR0ZMPz5EKiouKysuQkFHQkFGQkJHQkJHQkFGPDtBKSks"
                            + "LCwwRURKRURKRkVLR0ZLRURJPTxCKiktLi0xSEdMSUhOSklOQkFHPj1CPj1DKysuHx4hKCgr"
                            + "KSgsKSksJyYqJSUoJSUoHh4gFhYYFRUXFRUXFRUXFRUXFRUXFRUXFhYYFhUXFhQWFhQWFhQWFhQW"
                            + "FhQWFhQWFhUXFCcjEjwyEj0zEjwyEjwyEj0zEjwyFCcjDV9JBLGBBLKBBal7Bal7BLKBBLGB"
                            + "DV9J"
            ),
            decodeTemplate(
                    "FRUWFRUXFRUXFBUWFRUWFRUWFRUWFRUWDQwREA0wHxodJR4bExMVGRgZFhYXFRUWMTI6JyRB"
                            + "Y0UwZUUtHh8pMi4wFxcYFRUWLyYfMiggXUAqVjkhMC1CLDBWFhYaFRQUYEcqX00yISEpLS00"
                            + "Mi1LJypVGSU1FyY5n1YgfUcgLTVOKDJDJiQxR0FMQE9dHkVzbzkVWC0THx0jGhsgMC80OTc8"
                            + "JyIeFRISGB0aFCYgEiQfEiUhGyIhGhocFBQWFRUWHSonKUlBJkY9KkpBLjs4Ly4vHx8gFRUW"
                            + "LCstTEpMRkRFUlFSVFRVU1NUMjIzFRUWMTEyTU1OT09QU1NTMTEyKyssIiIjFRUWHBwdJSUm"
                            + "KiorJSUmFxcYFBQVFRUWFhYXFRUWFRQVFBQVFBUWFRYXFhUWFhUWFRYXFhQWFCYiCYJgCI1o"
                            + "CI1oCYJgFCYiFhQWFhQWFCQgC3ZYCYJgCYJgC3ZYFCQgFhQWFhYXFhUWFRYXFCAeFCAeFRYX"
                            + "FhUWFhYX"
            ),
            decodeTemplate(
                    "GxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxwe"
                            + "GxweGxweGxsdGxsdGhseGxweGxweGxweGxweICIlMTlGLzU9JicnHyAhGxweGxweGxweNTU8"
                            + "Z4e2WFttQEJOJSkwGxwdGxweGxweODQ5c32ldldeLy0zHB0gGxweGxweGxweICIlTjo7d0M8"
                            + "JyMlGhweGxweGxweHB0fGxweGxweIB4fHBweGxweGxweJScoREhJNDg5Gh8gGh4fGxweGxwe"
                            + "GxweHicnJklBJEpBGEA2FzItGxweGxweGxweJSYoP0JDQERFJSkqHR8hHh8hHR4gGxweIiMl"
                            + "MjE0MjEzMzM1Li8xJygqJSYnHB0fGyIiGzgyGjs0HS0qHR4gGxweGxweGxweFzUvCoxoCZZu"
                            + "EVtHGxodGxweGxweGxweGx4fGSclGSknGiIiGxweGxweGxweGxweGxweGxseGxsdGxweGxwe"
                            + "GxweGxweGxwe"
            ),
            decodeTemplate(
                    "GxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxweGxwe"
                            + "GxweGxweGxsdGxseGxweGxweGxweGxweGxweICEkK0I+LzQzIB8iGxweGxweGxweHB0fKy0w"
                            + "S5V8YGtbLCgsGx0fGxweGxweHB0fLSsvTHlmhHdgTTQzGhwfGxweGxweGxweICEkMTAyZUA5"
                            + "QiwpGhseGxweGxweGhsdGhsdGhseGhweGhweGxweGxweJygqQUFDOzs9QUFDMjM1GxweGxwe"
                            + "GxweHicnJkdAIkc+KjExJSYoGhsdGxweGxweHiIkKDQzKjg2LTAxLCwuKywuHyAiGxweIyQm"
                            + "ODY5Ozk7Ojk7NjU3LS4wIyQmGxweGyMjGT41GUE4GUE3GD41GSMjGxsdGxweGDEsDX1eC4Rj"
                            + "DIFhDIFgFzYvGxodGxweGxweHyQlHyQlGiAgGx8gGx0eGxweGxweGxweHB0fHBwfGxweGxwe"
                            + "GxweGxweGxwe"
            ),
            decodeTemplate(
                    "GxseGhsdGhsdGhsdGxweGxweGxweGxweHC8sLTAxLS4wJykrGx0eGx4fGSgmGiYkHicnKy0u"
                            + "KywuJSorGyAgGx0eGiQkGyMjKCksKCktIiMmGiAgGiAhHR0gJk9FP1dSICImHR4iGx0gGycm"
                            + "HCgnHB0gHikpKC8xHB4jHB4iHB4iHCgoHCopHB4iHB4iHB4iGx4iHB4iHB4iHCQlHCQlHB4i"
                            + "HB4iGx4iGx0gGx0gGxwgJCgoJCgoGxwgGx0gGx0gHR4hHR8hPDEtZz4qaD8rPTIuHR8iHR4h"
                            + "KSouNDY5alRKeT8ke0AlbllPNjg7KCktJygrLS8yYU9HdD0kd0AmZFJKLjAzJygsJicqLTAz"
                            + "XktEazUgbjchX01GLTI0JSwuJygsMDI1X0tEWC4hWS0fXkpELzY3JDIyGxweGhseNyknQicg"
                            + "QSUdNSglGhweGx0fGxweGhweJCMkT0A6Tj03IyIjGhweGxweGxweGxweISEiTz43TDszISAh"
                            + "GxweGxwe"
            )
    };

    private static final long FIRST_CHECK_DELAY_MILLIS = 5_000L;
    private static final long SECOND_CHECK_DELAY_MILLIS = 10_000L;
    private static final long PROCESS_EXIT_DELAY_MILLIS = 250L;
    private static final long ONBOARDING_CHECK_INTERVAL_MILLIS = 1_000L;
    private static final long ONBOARDING_RETRY_MILLIS = 4_000L;
    private static final long ONBOARDING_TIMEOUT_MILLIS = 90_000L;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean RECOVERY_REQUESTED = new AtomicBoolean(false);

    private static WeakReference<Activity> resumedActivity = new WeakReference<>(null);
    private static boolean compatibleVersion;
    private static long installedVersionCode = -1L;
    private static boolean firstSplashMatched;
    private static int watchdogGeneration;
    private static Runnable firstCheckTask;
    private static Runnable secondCheckTask;
    private static Runnable onboardingTask;
    private static long onboardingDeadline;
    private static long lastOnboardingActionAt;
    private static long lastOnboardingMismatchLogAt;
    private static int lastOnboardingScreen = -1;
    private static int candidateOnboardingScreen = -1;
    private static int candidateOnboardingCount;
    private static boolean motivationChoiceSelected;

    @Override
    public String name() {
        return "GuitarTuna stuck-splash recovery";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return TARGET_PACKAGE.equals(loadPackageParam.packageName)
                && TARGET_PACKAGE.equals(loadPackageParam.processName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!templatesAreValid()) {
            XposedBridge.log(TAG + ": invalid visual template; recovery disabled");
            return;
        }

        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onCreate",
                Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;
                        if (!isTargetActivity(activity)) {
                            return;
                        }
                        installedVersionCode = getInstalledVersionCode(activity);
                        compatibleVersion = GuitarTunaRecoveryPolicy.supports(
                                installedVersionCode
                        );
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;
                        if (isTargetActivity(activity) && compatibleVersion) {
                            if (isOnboardingPending(activity)) {
                                armOnboarding(activity);
                            } else {
                                armWatchdog(activity);
                            }
                        }
                    }
                }
        );
        XposedHelpers.findAndHookMethod(
                Activity.class,
                "onPause",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;
                        if (isTargetActivity(activity)) {
                            disarmWatchdog(activity);
                        }
                    }
                }
        );
    }

    private static boolean isTargetActivity(Activity activity) {
        return TARGET_ACTIVITY.equals(activity.getClass().getName());
    }

    private static long getInstalledVersionCode(Activity activity) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
                    TARGET_PACKAGE,
                    0
            );
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? packageInfo.getLongVersionCode()
                    : packageInfo.versionCode;
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": could not verify GuitarTuna version");
            return -1L;
        }
    }

    private static byte[] decodeTemplate(String encoded) {
        return Base64.decode(encoded, Base64.DEFAULT);
    }

    private static boolean templatesAreValid() {
        int expectedLength = TEMPLATE_WIDTH * TEMPLATE_HEIGHT * 3;
        if (SPLASH_TEMPLATE.length != expectedLength) {
            return false;
        }
        for (byte[] template : ONBOARDING_TEMPLATES) {
            if (template.length != expectedLength) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOnboardingPending(Activity activity) {
        return activity.getSharedPreferences(RECOVERY_PREFS, Context.MODE_PRIVATE)
                .getBoolean(ONBOARDING_PENDING, false);
    }

    private static void armWatchdog(Activity activity) {
        cancelWatchdogTasks();
        cancelOnboardingTask();
        resumedActivity = new WeakReference<>(activity);
        firstSplashMatched = false;
        int generation = ++watchdogGeneration;
        firstCheckTask = () -> checkSplash(generation, true);
        MAIN_HANDLER.postDelayed(firstCheckTask, FIRST_CHECK_DELAY_MILLIS);
    }

    private static void disarmWatchdog(Activity activity) {
        if (resumedActivity.get() != activity) {
            return;
        }
        resumedActivity.clear();
        firstSplashMatched = false;
        watchdogGeneration++;
        cancelWatchdogTasks();
        cancelOnboardingTask();
    }

    private static void cancelWatchdogTasks() {
        if (firstCheckTask != null) {
            MAIN_HANDLER.removeCallbacks(firstCheckTask);
            firstCheckTask = null;
        }
        if (secondCheckTask != null) {
            MAIN_HANDLER.removeCallbacks(secondCheckTask);
            secondCheckTask = null;
        }
    }

    private static void armOnboarding(Activity activity) {
        cancelWatchdogTasks();
        cancelOnboardingTask();
        resumedActivity = new WeakReference<>(activity);
        firstSplashMatched = false;
        int generation = ++watchdogGeneration;
        onboardingDeadline = SystemClock.elapsedRealtime() + ONBOARDING_TIMEOUT_MILLIS;
        lastOnboardingActionAt = 0L;
        lastOnboardingMismatchLogAt = 0L;
        lastOnboardingScreen = -1;
        candidateOnboardingScreen = -1;
        candidateOnboardingCount = 0;
        motivationChoiceSelected = false;
        scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
        XposedBridge.log(TAG + ": automating clean Guitar onboarding");
    }

    private static void cancelOnboardingTask() {
        if (onboardingTask != null) {
            MAIN_HANDLER.removeCallbacks(onboardingTask);
            onboardingTask = null;
        }
    }

    private static void scheduleOnboardingCheck(int generation, long delayMillis) {
        cancelOnboardingTask();
        onboardingTask = () -> checkOnboarding(generation);
        MAIN_HANDLER.postDelayed(onboardingTask, delayMillis);
    }

    private static void checkOnboarding(int generation) {
        onboardingTask = null;
        Activity activity = resumedActivity.get();
        if (!isCurrentOnboarding(activity, generation)) {
            return;
        }
        if (SystemClock.elapsedRealtime() >= onboardingDeadline) {
            XposedBridge.log(TAG + ": onboarding automation timed out");
            return;
        }

        SurfaceView surfaceView = findUnitySurface(activity);
        if (surfaceView == null) {
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }
        final Bitmap sample;
        try {
            sample = Bitmap.createBitmap(
                    SAMPLE_WIDTH,
                    SAMPLE_HEIGHT,
                    Bitmap.Config.ARGB_8888
            );
        } catch (RuntimeException | OutOfMemoryError error) {
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }
        try {
            PixelCopy.request(
                    surfaceView,
                    sample,
                    result -> handleOnboardingSample(
                            activity,
                            surfaceView,
                            generation,
                            sample,
                            result
                    ),
                    MAIN_HANDLER
            );
        } catch (RuntimeException error) {
            sample.recycle();
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
        }
    }

    private static boolean isCurrentOnboarding(Activity activity, int generation) {
        return activity != null
                && generation == watchdogGeneration
                && resumedActivity.get() == activity
                && !activity.isFinishing()
                && !activity.isDestroyed()
                && activity.hasWindowFocus()
                && isOnboardingPending(activity);
    }

    private static void handleOnboardingSample(
            Activity activity,
            SurfaceView surfaceView,
            int generation,
            Bitmap sample,
            int result
    ) {
        int screen = -1;
        int bestScreen = -1;
        double bestRmse = Double.POSITIVE_INFINITY;
        try {
            if (result == PixelCopy.SUCCESS && isCurrentOnboarding(activity, generation)) {
                byte[] observed = blockAverageRgb(sample);
                for (int index = 0; index < ONBOARDING_TEMPLATES.length; index++) {
                    double rmse = GuitarTunaRecoveryPolicy.normalizedRmse(
                            observed,
                            ONBOARDING_TEMPLATES[index]
                    );
                    if (rmse < bestRmse) {
                        bestRmse = rmse;
                        bestScreen = index;
                    }
                }
                if (bestRmse <= 0.09) {
                    screen = bestScreen;
                }
            }
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": failed to evaluate onboarding screen");
            XposedBridge.log(error);
        } finally {
            sample.recycle();
        }

        if (!isCurrentOnboarding(activity, generation)) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (screen < 0 && now - lastOnboardingMismatchLogAt >= 5_000L) {
            lastOnboardingMismatchLogAt = now;
            XposedBridge.log(
                    TAG + ": onboarding visual mismatch; best rmse="
                            + Math.round(bestRmse * 1_000.0) + "/1000"
            );
        }
        if (screen < 0) {
            candidateOnboardingScreen = -1;
            candidateOnboardingCount = 0;
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }
        if (screen != candidateOnboardingScreen) {
            candidateOnboardingScreen = screen;
            candidateOnboardingCount = 1;
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }
        candidateOnboardingCount++;
        if (candidateOnboardingCount < 2) {
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }
        candidateOnboardingScreen = -1;
        candidateOnboardingCount = 0;
        if (screen == SCREEN_TUNER) {
            finishOnboarding(activity);
            return;
        }
        if (screen == lastOnboardingScreen
                && now - lastOnboardingActionAt < ONBOARDING_RETRY_MILLIS) {
            scheduleOnboardingCheck(generation, ONBOARDING_CHECK_INTERVAL_MILLIS);
            return;
        }

        lastOnboardingScreen = screen;
        lastOnboardingActionAt = now;
        performOnboardingAction(activity, generation, screen);
        scheduleOnboardingCheck(generation, 1_500L);
    }

    private static void performOnboardingAction(
            Activity activity,
            int generation,
            int screen
    ) {
        switch (screen) {
            case SCREEN_WELCOME:
                requestSystemTap(activity, 0.88f, 0.083f);
                break;
            case SCREEN_INSTRUMENT:
                requestSystemTap(activity, 0.26f, 0.398f);
                break;
            case SCREEN_LEVEL:
                requestSystemTap(activity, 0.50f, 0.413f);
                break;
            case SCREEN_MOTIVATION:
                if (!motivationChoiceSelected) {
                    requestSystemTap(activity, 0.165f, 0.635f);
                    motivationChoiceSelected = true;
                    MAIN_HANDLER.postDelayed(() -> {
                        if (isCurrentOnboarding(activity, generation)) {
                            requestSystemTap(activity, 0.50f, 0.911f);
                        }
                    }, 500L);
                } else {
                    requestSystemTap(activity, 0.50f, 0.911f);
                }
                break;
            case SCREEN_CUSTOMIZE:
                requestSystemTap(activity, 0.50f, 0.913f);
                break;
            case SCREEN_MICROPHONE:
                requestSystemTap(activity, 0.275f, 0.795f);
                break;
            case SCREEN_NOTIFICATIONS:
                requestSystemTap(activity, 0.25f, 0.864f);
                break;
            default:
                return;
        }
        XposedBridge.log(TAG + ": completed onboarding step " + screen);
    }

    private static void requestSystemTap(
            Activity activity,
            float xRatio,
            float yRatio
    ) {
        Intent request = new Intent(GuitarTunaRestartContract.ACTION_INPUT_TAP)
                .setPackage(GuitarTunaRestartContract.HOST_PACKAGE)
                .putExtra(GuitarTunaRestartContract.EXTRA_X_RATIO, xRatio)
                .putExtra(GuitarTunaRestartContract.EXTRA_Y_RATIO, yRatio)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        try {
            activity.sendBroadcast(request);
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": could not request system input tap");
            XposedBridge.log(error);
        }
    }

    private static void finishOnboarding(Activity activity) {
        boolean committed = activity.getSharedPreferences(
                RECOVERY_PREFS,
                Context.MODE_PRIVATE
        ).edit().putBoolean(ONBOARDING_PENDING, false).commit();
        if (!committed) {
            XposedBridge.log(TAG + ": could not clear onboarding marker");
            return;
        }
        cancelOnboardingTask();
        XposedBridge.log(TAG + ": entered Guitar 6-string tuner");
    }

    private static void checkSplash(int generation, boolean firstCheck) {
        Activity activity = resumedActivity.get();
        if (!isCurrentWatchdog(activity, generation)) {
            return;
        }

        SurfaceView surfaceView = findUnitySurface(activity);
        if (surfaceView == null) {
            XposedBridge.log(TAG + ": Unity surface unavailable; skipped splash check");
            return;
        }

        final Bitmap sample;
        try {
            sample = Bitmap.createBitmap(
                    SAMPLE_WIDTH,
                    SAMPLE_HEIGHT,
                    Bitmap.Config.ARGB_8888
            );
        } catch (RuntimeException | OutOfMemoryError error) {
            XposedBridge.log(TAG + ": could not allocate splash sample");
            return;
        }

        try {
            PixelCopy.request(
                    surfaceView,
                    sample,
                    result -> handlePixelCopy(
                            activity,
                            generation,
                            sample,
                            result,
                            firstCheck
                    ),
                    MAIN_HANDLER
            );
        } catch (RuntimeException error) {
            sample.recycle();
            XposedBridge.log(TAG + ": splash sampling unavailable for this launch");
        }
    }

    private static boolean isCurrentWatchdog(Activity activity, int generation) {
        return activity != null
                && generation == watchdogGeneration
                && resumedActivity.get() == activity
                && !activity.isFinishing()
                && !activity.isDestroyed()
                && activity.hasWindowFocus()
                && !RECOVERY_REQUESTED.get();
    }

    private static SurfaceView findUnitySurface(Activity activity) {
        try {
            int unitySurfaceId = activity.getResources().getIdentifier(
                    "unitySurfaceView",
                    "id",
                    TARGET_PACKAGE
            );
            if (unitySurfaceId != 0) {
                View knownView = activity.findViewById(unitySurfaceId);
                if (knownView instanceof SurfaceView
                        && isUsableSurface((SurfaceView) knownView)) {
                    return (SurfaceView) knownView;
                }
            }
        } catch (RuntimeException ignored) {
            // Fall back to the largest visible valid SurfaceView.
        }
        return findLargestSurface(activity.getWindow().getDecorView(), null);
    }

    private static SurfaceView findLargestSurface(View view, SurfaceView best) {
        if (view instanceof SurfaceView) {
            SurfaceView candidate = (SurfaceView) view;
            if (isUsableSurface(candidate)
                    && (best == null || surfaceArea(candidate) > surfaceArea(best))) {
                best = candidate;
            }
        }
        if (!(view instanceof ViewGroup)) {
            return best;
        }
        ViewGroup group = (ViewGroup) view;
        for (int index = 0; index < group.getChildCount(); index++) {
            best = findLargestSurface(group.getChildAt(index), best);
        }
        return best;
    }

    private static boolean isUsableSurface(SurfaceView surfaceView) {
        try {
            return surfaceView.isShown()
                    && surfaceView.getWidth() > 0
                    && surfaceView.getHeight() > 0
                    && surfaceView.getHolder().getSurface().isValid();
        } catch (RuntimeException error) {
            return false;
        }
    }

    private static long surfaceArea(SurfaceView surfaceView) {
        return (long) surfaceView.getWidth() * surfaceView.getHeight();
    }

    private static void handlePixelCopy(
            Activity activity,
            int generation,
            Bitmap sample,
            int result,
            boolean firstCheck
    ) {
        boolean matched = false;
        try {
            if (result == PixelCopy.SUCCESS && isCurrentWatchdog(activity, generation)) {
                matched = GuitarTunaRecoveryPolicy.matchesSplash(
                        blockAverageRgb(sample),
                        SPLASH_TEMPLATE
                );
            }
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": failed to evaluate splash sample");
            XposedBridge.log(error);
        } finally {
            sample.recycle();
        }

        if (!isCurrentWatchdog(activity, generation)) {
            return;
        }
        if (!matched) {
            firstSplashMatched = false;
            return;
        }
        if (firstCheck) {
            firstSplashMatched = true;
            secondCheckTask = () -> checkSplash(generation, false);
            MAIN_HANDLER.postDelayed(secondCheckTask, SECOND_CHECK_DELAY_MILLIS);
            return;
        }
        if (firstSplashMatched) {
            requestRecovery(activity);
        }
    }

    private static byte[] blockAverageRgb(Bitmap source) {
        int[] pixels = new int[SAMPLE_WIDTH * SAMPLE_HEIGHT];
        source.getPixels(
                pixels,
                0,
                SAMPLE_WIDTH,
                0,
                0,
                SAMPLE_WIDTH,
                SAMPLE_HEIGHT
        );

        int blockWidth = SAMPLE_WIDTH / TEMPLATE_WIDTH;
        int blockHeight = SAMPLE_HEIGHT / TEMPLATE_HEIGHT;
        int pixelsPerBlock = blockWidth * blockHeight;
        byte[] rgb = new byte[TEMPLATE_WIDTH * TEMPLATE_HEIGHT * 3];
        for (int templateY = 0; templateY < TEMPLATE_HEIGHT; templateY++) {
            for (int templateX = 0; templateX < TEMPLATE_WIDTH; templateX++) {
                int red = 0;
                int green = 0;
                int blue = 0;
                for (int y = 0; y < blockHeight; y++) {
                    int sourceY = templateY * blockHeight + y;
                    for (int x = 0; x < blockWidth; x++) {
                        int sourceX = templateX * blockWidth + x;
                        int color = pixels[sourceY * SAMPLE_WIDTH + sourceX];
                        red += Color.red(color);
                        green += Color.green(color);
                        blue += Color.blue(color);
                    }
                }
                int output = (templateY * TEMPLATE_WIDTH + templateX) * 3;
                rgb[output] = (byte) ((red + pixelsPerBlock / 2) / pixelsPerBlock);
                rgb[output + 1] = (byte) ((green + pixelsPerBlock / 2) / pixelsPerBlock);
                rgb[output + 2] = (byte) ((blue + pixelsPerBlock / 2) / pixelsPerBlock);
            }
        }
        return rgb;
    }

    private static void requestRecovery(Activity activity) {
        long requestedVersionCode = installedVersionCode;
        if (!GuitarTunaRecoveryPolicy.supports(requestedVersionCode)
                || !RECOVERY_REQUESTED.compareAndSet(false, true)) {
            return;
        }
        cancelWatchdogTasks();

        Context applicationContext = activity.getApplicationContext();
        if (applicationContext == null) {
            applicationContext = activity;
        }
        SharedPreferences recoveryPreferences = applicationContext.getSharedPreferences(
                RECOVERY_PREFS,
                Context.MODE_PRIVATE
        );
        long now = System.currentTimeMillis();
        if (!GuitarTunaRecoveryPolicy.canRecover(
                recoveryPreferences.getLong(LAST_RECOVERY_AT, 0L),
                now
        )) {
            RECOVERY_REQUESTED.set(false);
            XposedBridge.log(TAG + ": recovery cooldown is active");
            return;
        }

        boolean localStateReset;
        try {
            localStateReset = resetLocalState(activity);
        } catch (Throwable error) {
            RECOVERY_REQUESTED.set(false);
            XposedBridge.log(TAG + ": failed to reset GuitarTuna local state");
            XposedBridge.log(error);
            return;
        }
        if (!localStateReset) {
            RECOVERY_REQUESTED.set(false);
            return;
        }

        BroadcastReceiver completion = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() != Activity.RESULT_OK) {
                    RECOVERY_REQUESTED.set(false);
                    XposedBridge.log(TAG + ": launcher restart host unavailable");
                    return;
                }
                if (!recoveryPreferences.edit()
                        .putLong(LAST_RECOVERY_AT, now)
                        .putLong(LAST_RECOVERY_VERSION, requestedVersionCode)
                        .putBoolean(ONBOARDING_PENDING, true)
                        .commit()) {
                    XposedBridge.log(TAG + ": could not persist recovery cooldown");
                }
                XposedBridge.log(TAG + ": cleared local state; restarting GuitarTuna");
                MAIN_HANDLER.postDelayed(
                        () -> Process.killProcess(Process.myPid()),
                        PROCESS_EXIT_DELAY_MILLIS
                );
            }
        };
        Intent restartRequest = new Intent(GuitarTunaRestartContract.ACTION)
                .setPackage(GuitarTunaRestartContract.HOST_PACKAGE)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        try {
            applicationContext.sendOrderedBroadcast(
                    restartRequest,
                    null,
                    completion,
                    MAIN_HANDLER,
                    Activity.RESULT_CANCELED,
                    null,
                    null
            );
        } catch (Throwable error) {
            RECOVERY_REQUESTED.set(false);
            XposedBridge.log(TAG + ": could not request launcher restart");
            XposedBridge.log(error);
        }
    }

    private static boolean resetLocalState(Activity activity) {
        Context context = activity.getApplicationContext();
        if (context == null) {
            context = activity;
        }
        boolean fullyDeleted = true;
        File dataDirectory = context.getDataDir();
        File[] children = dataDirectory.listFiles();
        if (children == null) {
            return false;
        } else {
            for (File child : children) {
                if ("lib".equals(child.getName())) {
                    continue;
                }
                if ("shared_prefs".equals(child.getName())) {
                    fullyDeleted &= deleteSharedPreferencesExceptRecovery(child);
                } else {
                    fullyDeleted &= deleteRecursively(child);
                }
            }
        }

        if (!fullyDeleted) {
            XposedBridge.log(TAG + ": some local files survived reset");
        }
        return true;
    }

    private static boolean deleteSharedPreferencesExceptRecovery(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }
        String preferencesFile = RECOVERY_PREFS + ".xml";
        String backupFile = preferencesFile + ".bak";
        boolean deleted = true;
        for (File file : files) {
            String name = file.getName();
            if (preferencesFile.equals(name) || backupFile.equals(name)) {
                continue;
            }
            deleted &= deleteRecursively(file);
        }
        return deleted;
    }

    private static boolean deleteRecursively(File file) {
        boolean deleted = true;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                deleted = false;
            } else {
                for (File child : children) {
                    deleted &= deleteRecursively(child);
                }
            }
        }
        return file.delete() && deleted;
    }

}
