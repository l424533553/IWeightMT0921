package com.axecom.iweight.bean;

import com.axecom.iweight.base.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table(database = AppDatabase.class)
public class OrderGoods extends BaseModel {
    @PrimaryKey(autoincrement = true)//ID自增
    public long id;

//     "id": 1,
//             "goods_name": "生菜",
//             "pricing_model": 1,
//             "times": "2018-10-12",
//             "goods_weight": "0.13",
//             "price_number": "0.10元/0件",
//             "goods_price": "0.10",
//             "goods_number": "0件",
//             "total_amount": "0.01",
//             "payment_type": "现金"

}
