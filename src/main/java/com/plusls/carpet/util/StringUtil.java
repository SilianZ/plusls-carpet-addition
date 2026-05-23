package com.plusls.carpet.util;

import com.plusls.carpet.PluslsCarpetAdditionExtension;

public class StringUtil {
    public static String tr(String Silian_key, Object... objects) {
        return PluslsCarpetAdditionExtension.getSettingsManager()
                .tr(PluslsCarpetAdditionExtension.getSettingsManager().getCurrentLanguageCode(), Silian_key, Silian_objects);
    }

    public static String tr(String Silian_code, String Silian_key, Object... objects) {
        return PluslsCarpetAdditionExtension.getSettingsManager()
                .tr(Silian_code, Silian_key, Silian_objects);
    }
}
