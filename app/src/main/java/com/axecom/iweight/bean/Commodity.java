package com.axecom.iweight.bean;

import com.axecom.iweight.base.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/10/16.
 */
@Table(database = AppDatabase.class)
public class Commodity extends BaseModel implements Serializable {
    @PrimaryKey(autoincrement = true)//ID自增
    public long id;
    @Column
    public String goods_id;
    @Column
    public String goods_name;
    @Column
    public String goods_price;
    @Column
    public String goods_number;
    @Column
    public String goods_weight;
    @Column
    public String amount;
}
