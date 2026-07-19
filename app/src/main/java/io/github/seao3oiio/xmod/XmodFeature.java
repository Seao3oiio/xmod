package io.github.seao3oiio.xmod;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** A package-scoped hook that can be registered by the shared xmod entry point. */
public interface XmodFeature {
    String name();

    boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam);

    void install(XC_LoadPackage.LoadPackageParam loadPackageParam);
}
