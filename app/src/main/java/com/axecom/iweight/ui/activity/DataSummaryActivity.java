package com.axecom.iweight.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.bean.LocalSettingsBean;
import com.axecom.iweight.bean.Order;
import com.axecom.iweight.bean.OrderGoods;
import com.axecom.iweight.bean.OrderListResultBean;
import com.axecom.iweight.bean.OrderLocal;
import com.axecom.iweight.bean.Order_Table;
import com.axecom.iweight.bean.ReportResultBean;
import com.axecom.iweight.manager.PrinterManager;
import com.axecom.iweight.manager.ThreadPool;
import com.axecom.iweight.utils.LogUtils;
import com.axecom.iweight.utils.SPUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018-5-24.
 */

public class DataSummaryActivity extends BaseActivity {

    private View rootView;
    private ListView dataListView;
    private ListView salesDetailsListView;
    private DataAdapter dataAdapter;
    private SalesAdapter salesAdapter;
    private LinearLayout reportTitleLayout;
    private LinearLayout reportTotalLayout;
    private LinearLayout salesTitleLayout;
    private LinearLayout salesTotalLayout;
    private TextView dayReportTv;
    private TextView monthReportTv;
    private TextView salesDetailsReportTv;
    private TextView backTv;
    private TextView printTv;
    private TextView dateTv;
    private List<ReportResultBean.list> dataList = new ArrayList<>();
    private List<OrderListResultBean.list> orderList;

    private TextView countTotalTv, weightTotalTv, grandTotalTv, amountTotalTv;
    private TextView orderAmountTv;
    private Button prevPageBtn, nextPageBtn, prevMonthBtn, nextMonthBtn, prevDayBtn, nextDayBtn;
    private Button salesDetailsPrevPageBtn, salesDetailsNextPageBtn, salesDetailsPrevDayBtn, salesDetailsNextDayBtn;
    private int currentPage = 1;
    private String currentDay;
    private int typeVal = 1;
    private int pageNum = 9;
    private int previousPos = 10;
    private int orderType = 1;
    private ReportResultBean reportResultBean;
    private OrderListResultBean orderListResultBean;
    private PrinterManager printerManager;
    private String port = "/dev/ttyS4";

    @Override
    public View setInitView() {
        rootView = LayoutInflater.from(this).inflate(R.layout.data_summary_activity, null);
        dataListView = rootView.findViewById(R.id.data_summary_listview);
        salesDetailsListView = rootView.findViewById(R.id.sales_details_listview);
        reportTitleLayout = rootView.findViewById(R.id.data_summary_reports_title_layout);
        reportTotalLayout = rootView.findViewById(R.id.data_summary_reports_total_layout);
        salesTitleLayout = rootView.findViewById(R.id.data_summary_sales_title_layout);
        salesTotalLayout = rootView.findViewById(R.id.data_summary_sales_total_layout);
        dayReportTv = rootView.findViewById(R.id.data_summary_day_report_tv);
        monthReportTv = rootView.findViewById(R.id.data_summary_month_report_tv);
        salesDetailsReportTv = rootView.findViewById(R.id.data_summary_sales_details_report_tv);
        backTv = rootView.findViewById(R.id.data_summary_back_tv);
        dateTv = rootView.findViewById(R.id.data_summary_date_tv);
        printTv = rootView.findViewById(R.id.data_summary_print_tv);
        countTotalTv = rootView.findViewById(R.id.data_summary_reports_count_total_tv);
        orderAmountTv = rootView.findViewById(R.id.data_summary_order_amount_total_tv);
        weightTotalTv = rootView.findViewById(R.id.data_summary_reports_weight_total_tv);
        grandTotalTv = rootView.findViewById(R.id.data_summary_reports_grand_total_tv);
        amountTotalTv = rootView.findViewById(R.id.data_summary_reports_amount_total_tv);
        prevPageBtn = rootView.findViewById(R.id.data_summary_reports_prev_page_btn);
        nextPageBtn = rootView.findViewById(R.id.data_summary_reports_next_page_btn);
        prevMonthBtn = rootView.findViewById(R.id.data_summary_reports_prev_month_btn);
        nextMonthBtn = rootView.findViewById(R.id.data_summary_reports_next_month_btn);
        prevDayBtn = rootView.findViewById(R.id.data_summary_reports_prev_day_btn);
        nextDayBtn = rootView.findViewById(R.id.data_summary_reports_next_day_btn);
        salesDetailsPrevPageBtn = rootView.findViewById(R.id.data_summary_sales_details_prev_page_btn);
        salesDetailsNextPageBtn = rootView.findViewById(R.id.data_summary_sales_details_next_page_btn);
        salesDetailsPrevDayBtn = rootView.findViewById(R.id.data_summary_sales_details_prev_day_btn);
        salesDetailsNextDayBtn = rootView.findViewById(R.id.data_summary_sales_details_next_day_btn);


        dayReportTv.setOnClickListener(this);
        printTv.setOnClickListener(this);
        monthReportTv.setOnClickListener(this);
        salesDetailsReportTv.setOnClickListener(this);
        backTv.setOnClickListener(this);
        prevPageBtn.setOnClickListener(this);
        nextPageBtn.setOnClickListener(this);
        prevMonthBtn.setOnClickListener(this);
        nextMonthBtn.setOnClickListener(this);
        prevDayBtn.setOnClickListener(this);
        nextDayBtn.setOnClickListener(this);
        salesDetailsPrevPageBtn.setOnClickListener(this);
        salesDetailsNextPageBtn.setOnClickListener(this);
        salesDetailsPrevDayBtn.setOnClickListener(this);
        salesDetailsNextDayBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void initView() {
        printerManager = new PrinterManager(this);
        LocalSettingsBean.Value.PrinterPort printerPort = (LocalSettingsBean.Value.PrinterPort) SPUtils.readObject(this, LocalSettingsActivity.KEY_PRINTER_PORT);
        if (printerPort != null) {
            port = printerPort.val.split("：")[1];
        }
        if (TextUtils.equals(port, "/dev/ttyS4")) {
            printerManager.openGpinter();
        } else {
            printerManager.usbConn();
        }
        currentDay = getCurrentTime("yyyy-MM-dd");
        dataAdapter = new DataAdapter(this, dataList);
        dataListView.setAdapter(dataAdapter);
        getReportsList(currentDay, typeVal, currentPage + "", pageNum + "");

        orderList = new ArrayList<>();
        salesAdapter = new SalesAdapter(this, orderList);
        salesDetailsListView.setAdapter(salesAdapter);
        dateTv.setText(getCurrentTime("yyyy-MM-dd"));

    }

    public void getReportsList(final String dateVal, int typeVal, String page, final String pNum) {

        LogUtils.d(dateVal+"----"+typeVal+"------"+page+"-----"+pNum+"----");

        dataList.clear();
        //获取数据库所有订单资料
        List<Order> getListOrder = new ArrayList<>();

        if (typeVal == 1){//当前 日数据
            getListOrder = SQLite.select().from(Order.class).where(Order_Table.create_time_day.is(dateVal)).queryList();
        }else if (typeVal == 2){//当前 月数据
            getListOrder = SQLite.select().from(Order.class).where(Order_Table.create_time_month.is(dateVal)).queryList();
        }

        reportResultBean = new ReportResultBean();
        reportResultBean.list = new ArrayList<ReportResultBean.list>();
        ReportResultBean.list list = new ReportResultBean.list();
        float amount = 0f;
        float weight=0f;
        int num=0;
        int total_number = 0;
        //获取当前日，月选中的列表数据
        for (Order order : getListOrder){
            LogUtils.d(order.total_amount + "--"+order.goods_price+"--"+order.amount+"--"+order.goods_number+"--"+order.pricing_model);
            list = new ReportResultBean.list();
            list.total_amount = order.total_amount;
            list.total_weight = order.total_weight;
            list.all_num = Integer.parseInt(order.total_number);
            list.total_number = Integer.parseInt(order.goods_number);
            if (typeVal == 1){//日
                    list.times = order.create_time;
            }else if(typeVal == 2){//月
                    list.times = order.create_time_day;
            }
            reportResultBean.list.add(list);
            amount = amount + Float.parseFloat(order.total_amount);
            weight = weight + Float.parseFloat(order.total_weight);
            num = num + Integer.parseInt(order.goods_number);
            total_number = total_number + Integer.parseInt(order.total_number);

        }
        reportResultBean.total_amount = String.format("%.2f", amount);
        reportResultBean.total=total_number;
        reportResultBean.all_number=num;
        reportResultBean.total_num=num;
        reportResultBean.total_weight=String.format("%.2f",weight);

        countTotalTv.setText(reportResultBean.total_num + "");
        weightTotalTv.setText(reportResultBean.total_weight + "kg/" + reportResultBean.all_number + "件");
        grandTotalTv.setText(reportResultBean.total_amount);
        amountTotalTv.setText(reportResultBean.total_amount);
        dataList.addAll(reportResultBean.list);
        dataAdapter.notifyDataSetChanged();
    }

    public void getOrderList(String dateVal, String page, String pageNum) {

        List<Order> getlist = SQLite.select().from(Order.class).where(Order_Table.create_time_day.is(dateVal)).queryList();
        orderListResultBean = new OrderListResultBean();
        orderListResultBean.list = new ArrayList<OrderListResultBean.list>();
        OrderListResultBean.list list = new OrderListResultBean.list();
        float amount = 0f;
        for (Order order : getlist){
            list = new OrderListResultBean.list();
            list.order_no = order.order_no;
            list.goods_name  = order.goods_name;
            list.times = order.create_time;
            list.goods_weight = order.goods_weight;
            list.goods_price = order.goods_price;
            list.goods_number = order.goods_number;
            list.total_amount = order.total_amount;
//            list.payment_type = order.pricing_model;
            orderListResultBean.list.add(list);
            amount = amount + Float.parseFloat(order.total_amount);
        }
        orderListResultBean.total_amount = String.format("%.2f", amount);
        orderList.clear();
        orderList.addAll(orderListResultBean.list);
        salesAdapter.notifyDataSetChanged();
        scrollTo(salesDetailsListView, salesDetailsListView.getCount() - 1);
        orderAmountTv.setText(orderListResultBean.total_amount);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_summary_day_report_tv:
                orderType = 1;
                currentDay = getCurrentTime("yyyy-MM-dd");
                dateTv.setText(currentDay);
                dataList.clear();
                currentPage = 1;
                typeVal = 1;
                reportTitleLayout.setVisibility(View.VISIBLE);
                reportTotalLayout.setVisibility(View.VISIBLE);
                dataListView.setVisibility(View.VISIBLE);
                salesTitleLayout.setVisibility(View.GONE);
                salesTotalLayout.setVisibility(View.GONE);
                salesDetailsListView.setVisibility(View.GONE);
                prevMonthBtn.setVisibility(View.GONE);
                nextMonthBtn.setVisibility(View.GONE);
                prevDayBtn.setVisibility(View.VISIBLE);
                nextDayBtn.setVisibility(View.VISIBLE);
                prevPageBtn.setVisibility(View.GONE);
                nextPageBtn.setVisibility(View.GONE);

                salesDetailsPrevPageBtn.setVisibility(View.GONE);
                salesDetailsNextPageBtn.setVisibility(View.GONE);
                salesDetailsPrevDayBtn.setVisibility(View.GONE);
                salesDetailsNextDayBtn.setVisibility(View.GONE);
                dayReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_gray_btn_bg));
                monthReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                salesDetailsReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                LogUtils.d(currentDay+"-currentDay-"+typeVal+"--"+currentPage+"--"+pageNum);
                getReportsList(currentDay, typeVal, currentPage + "", pageNum + "");
                break;
            case R.id.data_summary_month_report_tv:
                orderType = 2;
                currentDay = getCurrentTime("yyyy-MM");
                dateTv.setText(currentDay);
                dataList.clear();
                currentPage = 1;
                typeVal = 2;
                reportTitleLayout.setVisibility(View.VISIBLE);
                reportTotalLayout.setVisibility(View.VISIBLE);
                dataListView.setVisibility(View.VISIBLE);
                salesTitleLayout.setVisibility(View.GONE);
                salesTotalLayout.setVisibility(View.GONE);
                salesDetailsListView.setVisibility(View.GONE);
                prevMonthBtn.setVisibility(View.VISIBLE);
                nextMonthBtn.setVisibility(View.VISIBLE);
                prevDayBtn.setVisibility(View.GONE);
                nextDayBtn.setVisibility(View.GONE);
                prevPageBtn.setVisibility(View.GONE);
                nextPageBtn.setVisibility(View.GONE);

                salesDetailsPrevPageBtn.setVisibility(View.GONE);
                salesDetailsNextPageBtn.setVisibility(View.GONE);
                salesDetailsPrevDayBtn.setVisibility(View.GONE);
                salesDetailsNextDayBtn.setVisibility(View.GONE);
                dayReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                monthReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_gray_btn_bg));
                salesDetailsReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                getReportsList(currentDay, typeVal , currentPage + "", pageNum + "");
                break;
            case R.id.data_summary_sales_details_report_tv:
                //销售明细
                orderType = 3;
                currentDay = getCurrentTime("yyyy-MM-dd");
                dateTv.setText(currentDay);
                orderList.clear();
                reportTitleLayout.setVisibility(View.GONE);
                reportTotalLayout.setVisibility(View.GONE);
                dataListView.setVisibility(View.GONE);
                salesTitleLayout.setVisibility(View.VISIBLE);
                salesTotalLayout.setVisibility(View.VISIBLE);
                salesDetailsListView.setVisibility(View.VISIBLE);
                prevPageBtn.setVisibility(View.GONE);
                nextPageBtn.setVisibility(View.GONE);
                prevMonthBtn.setVisibility(View.GONE);
                nextMonthBtn.setVisibility(View.GONE);
                prevDayBtn.setVisibility(View.GONE);
                nextDayBtn.setVisibility(View.GONE);

                salesDetailsPrevPageBtn.setVisibility(View.VISIBLE);
                salesDetailsNextPageBtn.setVisibility(View.VISIBLE);
                salesDetailsPrevDayBtn.setVisibility(View.VISIBLE);
                salesDetailsNextDayBtn.setVisibility(View.VISIBLE);

                dayReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                monthReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                salesDetailsReportTv.setBackground(DataSummaryActivity.this.getResources().getDrawable(R.drawable.shape_gray_btn_bg));
                getOrderList(getCurrentTime("yyyy-MM-dd"), "1", "10");
                break;
            case R.id.data_summary_back_tv:
                finish();
                break;
            case R.id.data_summary_reports_prev_page_btn:
                getReportsList(currentDay, typeVal, --currentPage <= 1 ? (currentPage = 1) + "" : --currentPage + "", pageNum + "");
                break;
            case R.id.data_summary_reports_next_page_btn:
                getReportsList(currentDay, typeVal, ++currentPage + "", pageNum + "");
                break;
            case R.id.data_summary_reports_prev_month_btn:
                currentPage = 1;
                currentDay = getMonthTime(currentDay, "yyyy-MM", 1);
                dateTv.setText(currentDay);
                //上一月
                getReportsList(currentDay, typeVal, (currentPage = 1) + "", pageNum + "");
                break;
            case R.id.data_summary_reports_next_month_btn:
                currentPage = 1;
                currentDay = getMonthTime(currentDay, "yyyy-MM", 2);
                dateTv.setText(currentDay);
                //下一月
                getReportsList(currentDay, typeVal, (currentPage = 1) + "", pageNum + "");
                break;
            case R.id.data_summary_reports_prev_day_btn:
                //前一天
                currentDay = getCurrentTime(currentDay, "yyyy-MM-dd", 3);
                dateTv.setText(currentDay);
                getReportsList(currentDay, typeVal, (currentPage = 1) + "", pageNum + "");
                break;
            case R.id.data_summary_reports_next_day_btn:
                //后一天
                currentDay = getCurrentTime(currentDay, "yyyy-MM-dd", 4);
                dateTv.setText(currentDay);
                getReportsList(currentDay, typeVal, (currentPage = 1) + "", pageNum + "");
                break;
            case R.id.data_summary_sales_details_prev_page_btn:
                //商品明细 上一页
                scrollTo(salesDetailsListView, salesDetailsListView.getFirstVisiblePosition() - previousPos <= 0 ? 0 : salesDetailsListView.getFirstVisiblePosition() - previousPos);
                getOrderList(currentDay,  --currentPage <= 1 ? (currentPage=1)+"" : --currentPage + "", "10");
                break;
            case R.id.data_summary_sales_details_next_page_btn:
                //商品明细 下一页
                getOrderList(currentDay, ++currentPage + "", previousPos + "");
                break;
            case R.id.data_summary_sales_details_prev_day_btn:
                //销售明细 前一天
                currentDay = getCurrentTime(currentDay, "yyyy-MM-dd", 3);
                dateTv.setText(currentDay);
                getOrderList(currentDay, "1", "10");
                break;
            case R.id.data_summary_sales_details_next_day_btn:
                //销售明细 后一天
                currentDay = getCurrentTime(currentDay, "yyyy-MM-dd", 4);
                dateTv.setText(currentDay);
                getOrderList(currentDay, "1", "10");
                break;
            case R.id.data_summary_print_tv:
                printerOrder(orderType, reportResultBean);
                break;
        }
    }

    private ThreadPool threadPool;

    public void printerOrder(final int orderType, final ReportResultBean reportResultBean) {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask(new Runnable() {
            @Override
            public void run() {
                switch (orderType) {
                    case 1:
                    case 2:
                        printerManager.printerOrderOfDayAndMonth(dataList, reportResultBean);
                        break;
                    case 3:
                        printerManager.printerOrderDetails(orderList, orderListResultBean);
                        break;
                }
            }
        });
    }

    public void insert(View view) {
        String uuid = UUID.randomUUID().toString();
        OrderLocal orderLocal = new OrderLocal();
                orderLocal.companyName = "teset";
//                orderLocal.orderTime = new Date(System.currentTimeMillis());
                orderLocal.orderNumber = uuid;
        orderLocal.save();

        for(int i=0;i<10;i++){
            OrderGoods goods =new OrderGoods();
            goods.name = "good"+i;
            goods.orderNumber = uuid;
            goods.save();
        }
    }



    public void read(View view) {
        List<OrderGoods> goods = SQLite.select().from(OrderGoods.class).queryList();
        for(OrderGoods goods1 :goods){
            LogUtils.d(goods1.toString());
        }
//        select DATE_FORMAT(start_time,'%Y%m') months,count(product_no) count from test group bymonths;

//        SQLite.select(count(Employee_Table.name), sum(Employee_Table.salary))
//                .from(Employee.class)
//        SELECT COUNT(`name`), SUM(`salary`) FROM `Employee`;



    }

    public void readGoods(View view) {
        List<OrderLocal> orderLocals = SQLite.select().from(OrderLocal.class).queryList();
        for(OrderLocal orderLocal :orderLocals){
            LogUtils.d(orderLocal.toString());
        }
//        SQLite.select().from(Goo)
    }

    class DataAdapter extends BaseAdapter {
        private Context context;
        private List<ReportResultBean.list> list;

        public DataAdapter(Context context, List<ReportResultBean.list> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.data_summary_item, null);
                holder = new ViewHolder();
                holder.timeTv = convertView.findViewById(R.id.data_item_time_tv);
                holder.countTv = convertView.findViewById(R.id.data_item_count_tv);
                holder.weightTv = convertView.findViewById(R.id.data_item_weight_tv);
                holder.grandTotalTv = convertView.findViewById(R.id.data_item_grand_total_tv);
                holder.incomeTv = convertView.findViewById(R.id.data_item_income_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ReportResultBean.list item = list.get(position);
            holder.timeTv.setText(item.times);
            holder.countTv.setText(item.all_num + "");
            holder.incomeTv.setText(item.total_amount);
            holder.grandTotalTv.setText(item.total_amount);
            holder.weightTv.setText(item.total_weight + "kg/" + item.total_number + "件");
            return convertView;
        }

        class ViewHolder {
            TextView timeTv;
            TextView countTv;
            TextView weightTv;
            TextView grandTotalTv;
            TextView incomeTv;
        }
    }

    class SalesAdapter extends BaseAdapter {

        private Context context;
        private List<OrderListResultBean.list> list;


        public SalesAdapter(Context context, List<OrderListResultBean.list> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.sales_data_item, null);
                holder = new ViewHolder();
                holder.goodsNameTv = convertView.findViewById(R.id.sales_data_item_goods_name_tv);
                holder.timeTv = convertView.findViewById(R.id.sales_data_item_time_tv);
                holder.weightTv = convertView.findViewById(R.id.sales_data_item_weight_tv);
                holder.pricePriceTv = convertView.findViewById(R.id.sales_data_item_number_tv);
                holder.priceNumberTv = convertView.findViewById(R.id.sales_data_item_price_number_tv);
                holder.totalAmountTv = convertView.findViewById(R.id.sales_data_item_total_amount_tv);
                holder.payTypeTv = convertView.findViewById(R.id.sales_data_item_pay_type_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            OrderListResultBean.list item = list.get(position);
            holder.goodsNameTv.setText(item.goods_name);
            holder.timeTv.setText(item.times);
            holder.weightTv.setText(item.goods_weight);
            holder.pricePriceTv.setText(item.goods_price);
            holder.priceNumberTv.setText(item.goods_number);
            holder.totalAmountTv.setText(item.total_amount);
            holder.payTypeTv.setText(item.payment_type);

            return convertView;
        }

        class ViewHolder {
            TextView goodsNameTv;
            TextView timeTv;
            TextView weightTv;
            TextView pricePriceTv;
            TextView priceNumberTv;
            TextView totalAmountTv;
            TextView payTypeTv;
        }
    }
}
