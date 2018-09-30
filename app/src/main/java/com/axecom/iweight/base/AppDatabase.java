package com.axecom.iweight.base;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Administrator on 2018/7/22.
 */

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public final class AppDatabase {
    public static final String NAME = "AppDatabase";

    public static final int VERSION = 1;
}
