package io.github.seao3oiio.xmod.features.guitartuna;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.seao3oiio.xmod.XmodFeature;

public final class GuitarTunaRestartHostFeature implements XmodFeature {
    private static final String TAG = "xmod/GuitarTunaRestartHost";
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean RESTART_PENDING = new AtomicBoolean(false);

    @Override
    public String name() {
        return "GuitarTuna restart host";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return GuitarTunaRestartContract.HOST_PACKAGE.equals(loadPackageParam.packageName)
                && GuitarTunaRestartContract.HOST_PACKAGE.equals(loadPackageParam.processName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod(
                Application.class,
                "attach",
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!REGISTERED.compareAndSet(false, true)) {
                            return;
                        }
                        Application application = (Application) param.thisObject;
                        Context context = application.getApplicationContext();
                        if (context == null) {
                            context = (Context) param.args[0];
                        }
                        registerRestartReceiver(context);
                    }
                }
        );
    }

    private static void registerRestartReceiver(Context context) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context receiverContext, Intent intent) {
                String action = intent.getAction();
                if (GuitarTunaRestartContract.ACTION_INPUT_TAP.equals(action)) {
                    float xRatio = intent.getFloatExtra(
                            GuitarTunaRestartContract.EXTRA_X_RATIO,
                            -1.0f
                    );
                    float yRatio = intent.getFloatExtra(
                            GuitarTunaRestartContract.EXTRA_Y_RATIO,
                            -1.0f
                    );
                    if (isValidRatio(xRatio) && isValidRatio(yRatio)) {
                        MAIN_HANDLER.post(() -> injectTap(receiverContext, xRatio, yRatio));
                    }
                    return;
                }
                if (!GuitarTunaRestartContract.ACTION.equals(action)) {
                    setResultCode(Activity.RESULT_CANCELED);
                    return;
                }
                setResultCode(Activity.RESULT_OK);
                if (!RESTART_PENDING.compareAndSet(false, true)) {
                    return;
                }
                MAIN_HANDLER.postDelayed(
                        () -> startGuitarTuna(receiverContext),
                        GuitarTunaRestartContract.RESTART_DELAY_MILLIS
                );
            }
        };
        IntentFilter filter = new IntentFilter(GuitarTunaRestartContract.ACTION);
        filter.addAction(GuitarTunaRestartContract.ACTION_INPUT_TAP);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                        receiver,
                        filter,
                        GuitarTunaRestartContract.SENDER_PERMISSION,
                        MAIN_HANDLER,
                        Context.RECEIVER_EXPORTED
                );
            } else {
                context.registerReceiver(
                        receiver,
                        filter,
                        GuitarTunaRestartContract.SENDER_PERMISSION,
                        MAIN_HANDLER
                );
            }
            XposedBridge.log(TAG + ": ready");
        } catch (Throwable error) {
            REGISTERED.set(false);
            XposedBridge.log(TAG + ": could not register restart receiver");
            XposedBridge.log(error);
        }
    }

    private static boolean isValidRatio(float ratio) {
        return ratio >= 0.0f && ratio <= 1.0f && Float.isFinite(ratio);
    }

    private static void injectTap(Context context, float xRatio, float yRatio) {
        Object inputManager = context.getSystemService(Context.INPUT_SERVICE);
        WindowManager windowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        if (inputManager == null || windowManager == null) {
            XposedBridge.log(TAG + ": input service unavailable");
            return;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        float x = metrics.widthPixels * xRatio;
        float y = metrics.heightPixels * yRatio;
        long now = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(
                now,
                now,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                0
        );
        MotionEvent up = MotionEvent.obtain(
                now,
                now + 50L,
                MotionEvent.ACTION_UP,
                x,
                y,
                0
        );
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            Object downResult = XposedHelpers.callMethod(
                    inputManager,
                    "injectInputEvent",
                    down,
                    0
            );
            Object upResult = XposedHelpers.callMethod(
                    inputManager,
                    "injectInputEvent",
                    up,
                    0
            );
            if (!Boolean.TRUE.equals(downResult) || !Boolean.TRUE.equals(upResult)) {
                XposedBridge.log(TAG + ": system rejected input tap");
            }
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": could not inject input tap");
            XposedBridge.log(error);
        } finally {
            down.recycle();
            up.recycle();
        }
    }

    private static void startGuitarTuna(Context context) {
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .setClassName(
                            GuitarTunaRestartContract.TARGET_PACKAGE,
                            GuitarTunaRestartContract.TARGET_ACTIVITY
                    )
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(launch);
            XposedBridge.log(TAG + ": restarted GuitarTuna");
        } catch (Throwable error) {
            XposedBridge.log(TAG + ": could not restart GuitarTuna");
            XposedBridge.log(error);
        } finally {
            RESTART_PENDING.set(false);
        }
    }
}
