package com.axecom.iweight.bean;

import com.axecom.iweight.base.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/10/16.
 */

@Table(database = AppDatabase.class)
public class LocalOrder extends BaseModel implements Serializable {
    @PrimaryKey(autoincrement = true)//ID自增
    public long id;

    @Column
    public String token;
    @Column
    public String mac;
    @Column
    public String total_amount;
    @Column
    public String total_weight;
    @Column
    public String payment_id;
    @Column
    public String create_time;
    @Column
    public String total_number;
    @Column
    public String pricing_model;


    @Column
    @ForeignKey(saveForeignKeyModel = false)
    Commodity commodity;

    public List<Commodity> commodities;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "commodities")
    public List<Commodity> getMyAnts() {
        if (commodities == null || commodities.isEmpty()) {
            commodities = SQLite.select()
                    .from(Commodity.class)
                    .where(Commodity_Table.goods_id.eq(String.valueOf(id)))
                    .queryList();
        }
        return commodities;
    }

}
