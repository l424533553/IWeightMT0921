package com.axecom.iweight.bean;

import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.my.entity.scalescategory.AllGoods;
import com.axecom.iweight.my.entity.scalescategory.CategoryGoods;
import com.axecom.iweight.my.entity.scalescategory.Goods;

import java.io.Serializable;
import java.util.List;

public class ScalesCategoryGoods extends BaseEntity {
    public List<HotKeyBean> hotKeyGoods;
    public List<AllGoods> allGoods;
    public List<CategoryGoods> categoryGoods;
    public List<Goods> goodsList;


    public List<HotKeyBean> getHotKeyGoods() {
        return hotKeyGoods;
    }

    public void setHotKeyGoods(List<HotKeyBean> hotKeyGoods) {
        this.hotKeyGoods = hotKeyGoods;
    }

    public List<AllGoods> getAllGoods() {
        return allGoods;
    }

    public void setAllGoods(List<AllGoods> allGoods) {
        this.allGoods = allGoods;
    }

    public List<CategoryGoods> getCategoryGoods() {
        return categoryGoods;
    }

    public void setCategoryGoods(List<CategoryGoods> categoryGoods) {
        this.categoryGoods = categoryGoods;
    }

    public List<Goods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<Goods> goodsList) {
        this.goodsList = goodsList;
    }
}
