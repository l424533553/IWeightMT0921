package com.axecom.iweight.bean;

import com.axecom.iweight.base.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.sql.Date;
import java.util.List;

//import java.util.Date;

@Table(database = AppDatabase.class)
public class OrderLocal extends BaseModel {
    @PrimaryKey(autoincrement = true)//ID自增
    public long id;
    @Column
    public String companyName;
    @Column
    public java.util.Date orderTime;
    @Column
    public String orderNumber;
    @Column
    public String stallNumber;
    public String qrCode;
    @Column
    public String seller;
    @Column
    public int sellerid;
    @Column
    public int tid;
    @Column
    public int marketId;
    @Column
    public String payId;
    @Column
    public String operator;
    @Column
    public double totalAmount;

    public List<OrderGoods> goods;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "goods")
    public List<OrderGoods> getGoods() {
        if (goods == null || goods.isEmpty()) {
            goods = SQLite.select()
                    .from(OrderGoods.class)
                    .where(OrderGoods_Table.orderNumber.eq(orderNumber))
                    .queryList();
        }
        return goods;
    }
}
