package com.axecom.iweight.base;

import com.axecom.iweight.bean.HotKeyBean;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

/**
 * Created by Administrator on 2018/7/22.
 */

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public final class AppDatabase {
    public static final String NAME = "AppDatabase";

    public static final int VERSION = 1;

}
