package com.axecom.iweight.my.entity.scalescategory;

import java.util.List;

/**
 * author: luofaxin
 * date： 2018/9/26 0026.
 * email:424533553@qq.com
 * describe:
 */
public class CategoryGoods {

    public int id;
    public String name;
    public List<child> child;

    public class child {
        public int id;
        public String name;
        public int cid;
        public int traceable_code;
        public String price;
        public int is_default;
    }
}
