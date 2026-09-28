package com.netk.mvolacash.admin.activation;

import java.util.Calendar;
import java.util.TimeZone;

/** API-23-compatible UTC calendar calculations for temporary licenses. */
public final class LicenseExpiration {
    private LicenseExpiration() {}

    public static long forLicense(LicenseType type, long issuedAt) {
        if (type == null || type == LicenseType.PERMANENT) {
            throw new IllegalArgumentException("Une licence permanente n’expire pas");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(Math.multiplyExact(issuedAt, 1000L));
        calendar.add(type == LicenseType.WEEK ? Calendar.DAY_OF_MONTH : Calendar.MONTH,
                type == LicenseType.WEEK ? 7 : 1);
        return calendar.getTimeInMillis() / 1000L;
    }
}
