package com.axecom.iweight.bean;

import com.axecom.iweight.base.BaseEntity;

import java.util.List;

/**
 * 日报表，月报表
 * @param <T>
 */
public class ReportResultBean<T> extends BaseEntity {
    public int total;//数据总条数
    public String total_amount;//总金额
    public String total_weight;//总重量
    public int all_number;
    public int total_num;//总笔数
    public List<list> list;

    public static class list {
        public String total_amount;//金额
        public String total_weight;//重量
        public int all_num;//笔数
        public int total_number;//件数
        public String times;//时间
    }
}
