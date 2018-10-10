package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.CommodityBean;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.SaveGoodsReqBean;
import com.axecom.iweight.bean.ScalesCategoryGoods;
import com.axecom.iweight.impl.ItemDragHelperCallback;
import com.axecom.iweight.impl.OnDragVHListener;
import com.axecom.iweight.impl.OnItemMoveListener;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.ActivityController;
import com.axecom.iweight.manager.MacManager;
import com.axecom.iweight.my.entity.scalescategory.AllGoods;
import com.axecom.iweight.my.entity.scalescategory.CategoryGoods;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.axecom.iweight.utils.FileUtils;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.axecom.iweight.base.BaseActivity.FLAG_HOMEKEY_DISPATCHED;

public class CommodityManagementActivity extends Activity implements View.OnClickListener {

    interface SelectedGoodsState {
        String selectedGoods = "select_goods";
        String NOT_PUSH_REMOTE = "NOT_PUSH_REMOTE";
    }

    private RecyclerView commodityRV;
    private GridView classGv;
    private ClassAdapter classAdapter;
    private DragAdapter adapter;
    private List<CommodityBean> hotKeyList;
    private Map<String, CommodityBean> hotKeyMap;
    private List<CommodityBean> allGoodsList;
    private List<CommodityBean> categoryList;
    private List<CommodityBean> categoryChildList;
    private LinearLayout classTitleLayout;
    private TextView allTitleTv;

    private boolean isShowDelTv = false;

    protected SysApplication sysApplication;
    private String GOODSDATA = "goodsdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
        setContentView(R.layout.commodity_management_activity);
        sysApplication = (SysApplication) getApplication();
        setInitView();
        ActivityController.addActivity(this);
        initView();
    }


    public void setInitView() {
        hotKeyList = new ArrayList<>();
        hotKeyMap = new ArrayMap<>();
        allGoodsList = new ArrayList<>();
        categoryList = new ArrayList<>();
        categoryChildList = new ArrayList<>();
        adapter = new DragAdapter(this, hotKeyList);
        classAdapter = new ClassAdapter(this, allGoodsList);


        commodityRV = findViewById(R.id.commodity_management_rv);
        classGv = findViewById(R.id.commodity_management_class_gv);
        classTitleLayout = findViewById(R.id.commodity_management_class_title_layout);
        allTitleTv = findViewById(R.id.commodity_management_class_titlte_all_tv);
        findViewById(R.id.commodity_management_save_btn).setOnClickListener(this);
        findViewById(R.id.commodity_management_back_btn).setOnClickListener(this);
        EditText searchEt = findViewById(R.id.commodity_management_search_et);
        getGoodsData();
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    return;
                }
                Pattern pattern = Pattern.compile(s.toString());
                List<CommodityBean> result = new ArrayList<>();

                for (int i = 0; i < allGoodsList.size(); i++) {
                    Matcher matcher = pattern.matcher(allGoodsList.get(i).getAllGoods().name);
                    if (matcher.find()) {
                        result.add(allGoodsList.get(i));
                    }
                }
                classAdapter = new ClassAdapter(CommodityManagementActivity.this, result);
                classGv.setAdapter(classAdapter);
                setClassTitleTxtColor();
            }
        });
        allTitleTv.setOnClickListener(this);
    }


    public void initView() {

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        commodityRV.setLayoutManager(manager);
        ItemDragHelperCallback callback = new ItemDragHelperCallback() {
            @Override
            public boolean isLongPressDragEnabled() {
                // 长按拖拽打开
                return true;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(commodityRV);

        commodityRV.setAdapter(adapter);

        //TODO 待恢复
//        adapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClickListener(View v) {
//                int position = (int) v.getTag();
////                hotKeyList.get(position);
//                Intent intent = new Intent(CommodityManagementActivity.this, ModityCommodityActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("commodityBean", hotKeyList.get(position));
//                intent.putExtra("position", position);
//                intent.putExtras(bundle);
//                startActivityForResult(intent, 1001);
//            }
//        });

        classGv.setAdapter(classAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commodity_management_class_titlte_all_tv:
                classAdapter = new ClassAdapter(this, allGoodsList);
                classGv.setAdapter(classAdapter);
                setClassTitleTxtColor();
                break;
            case R.id.commodity_management_save_btn:
                List<HotKeyBean> list = new ArrayList<>();
                for (CommodityBean bean : hotKeyList) {
                    list.add(bean.getHotKeyBean());
                }
                requestSaveSelectedGoods(list);
                break;
            case R.id.commodity_management_back_btn:
                finish();
                break;
        }
    }

    public void requestSaveSelectedGoods(List<HotKeyBean> dataList) {
        SaveGoodsReqBean goodsReqBean = new SaveGoodsReqBean();
        List<SaveGoodsReqBean.Goods> goodsList = new ArrayList<>();
        SaveGoodsReqBean.Goods good;
        for (int i = 0; i < dataList.size(); i++) {
            good = new SaveGoodsReqBean.Goods();
            HotKeyBean bean = dataList.get(i);
            good.id = bean.id;
            good.cid = bean.cid;
            good.is_default = bean.is_default;
            good.name = bean.name;
            good.price = bean.price;
            good.traceable_code = bean.traceable_code;
            goodsList.add(good);
        }
        goodsReqBean.setToken(AccountManager.getInstance().getAdminToken());
        goodsReqBean.setMac(MacManager.getInstace(SysApplication.getContext()).getMac());
        goodsReqBean.setGoods(goodsList);
        storeGoodsData(goodsReqBean);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (resultCode == 1001) {
            if (data != null) {
                int position = data.getIntExtra("position", -1);
                HotKeyBean goods = (HotKeyBean) Objects.requireNonNull(data.getExtras()).getSerializable("HotKeyBean");
                CommodityBean bean = new CommodityBean();
                bean.setHotKeyBean(goods);
                hotKeyList.set(position, bean);
            }

        }*/
    }

    public void setClassTitleTxtColor() {
        for (int i = 0; i < classTitleLayout.getChildCount(); i++) {
            ((TextView) classTitleLayout.getChildAt(i)).setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.black));
        }
        allTitleTv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.green_3CB371));
    }

    public void storeGoodsData(SaveGoodsReqBean goodsReqBean) {
        List<HotKeyBean> list = new ArrayList<>();
        for (CommodityBean bean : hotKeyList) {
            list.add(bean.getHotKeyBean());
        }
        FileUtils.saveObject(CommodityManagementActivity.this, (Serializable) list, SelectedGoodsState.selectedGoods);
        if (!NetworkUtil.isConnected(CommodityManagementActivity.this)) {
            SPUtils.put(CommodityManagementActivity.this, SelectedGoodsState.NOT_PUSH_REMOTE, true);
            saveSelectedGoods("本地保存成功");
            return;
        }
        RetrofitFactory.getInstance().API()
                .storeGoodsData(goodsReqBean)
                .compose(this.<BaseEntity>setThread())
                .subscribe(new Observer<BaseEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoading();
                    }

                    @Override
                    public void onNext(BaseEntity baseEntity) {
                        if (baseEntity.isSuccess()) {
                            saveSelectedGoods(baseEntity.getMsg());
                        } else {
                            showLoading(baseEntity.getMsg());
                            SPUtils.put(CommodityManagementActivity.this, SelectedGoodsState.NOT_PUSH_REMOTE, true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        SPUtils.put(CommodityManagementActivity.this, SelectedGoodsState.NOT_PUSH_REMOTE, true);
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    private void saveSelectedGoods(String message) {
        EventBus.getDefault().post(new BusEvent(BusEvent.SAVE_COMMODITY_SUCCESS, true));
        Toast.makeText(CommodityManagementActivity.this, message, Toast.LENGTH_SHORT).show();
        adapter = new DragAdapter(CommodityManagementActivity.this, hotKeyList);
        adapter.showDeleteTv(false);
        commodityRV.setAdapter(adapter);
    }

    public void getGoodsData() {

        boolean b = !NetworkUtil.isConnected(CommodityManagementActivity.this);
        if (b) {
            ScalesCategoryGoods goods = (ScalesCategoryGoods) FileUtils.readObject(CommodityManagementActivity.this, GOODSDATA);
            if (goods == null) return;
            processCategoryGoods(goods);
            return;
        }


        RetrofitFactory.getInstance().API()
                .getGoodsData(AccountManager.getInstance().getAdminToken(), MacManager.getInstace(this).getMac())
                .compose(this.<BaseEntity<ScalesCategoryGoods>>setThread())
                .subscribe(new Observer<BaseEntity<ScalesCategoryGoods>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoading();
                    }

                    @Override
                    public void onNext(final BaseEntity<ScalesCategoryGoods> scalesCategoryGoodsBaseEntity) {
                        if (scalesCategoryGoodsBaseEntity.isSuccess()) {
                            final ScalesCategoryGoods data = scalesCategoryGoodsBaseEntity.getData();
                            FileUtils.saveObject(CommodityManagementActivity.this, data, GOODSDATA);
                            processCategoryGoods(data);
                        } else {
                            showLoading(scalesCategoryGoodsBaseEntity.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        closeLoading();
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    private void processCategoryGoods(final ScalesCategoryGoods data) {
        List<HotKeyBean> hotKeyGoods = (List<HotKeyBean>) FileUtils.readObject(CommodityManagementActivity.this, CommodityManagementActivity.SelectedGoodsState.selectedGoods);
        CommodityBean commodityBean;
        List<HotKeyBean> hotKeyBeanList = data.hotKeyGoods;
        if (hotKeyGoods != null && hotKeyGoods.size() > 0) {
            hotKeyBeanList = hotKeyGoods;
        }
        for (int i = 0; i < hotKeyBeanList.size(); i++) {
            commodityBean = new CommodityBean();
            HotKeyBean hotKeyBean = hotKeyBeanList.get(i);
            commodityBean.setHotKeyBean(hotKeyBean);
            hotKeyList.add(commodityBean);
            String uid = hotKeyBean.getName() + hotKeyBean.getCid();
            hotKeyMap.put(uid, commodityBean);
        }
        CommodityBean allGoodsBean;
        for (int i = 0; i < data.allGoods.size(); i++) {
            allGoodsBean = new CommodityBean();
            allGoodsBean.setAllGoods(data.allGoods.get(i));
            allGoodsList.add(allGoodsBean);
        }
        CommodityBean categoryBean;
        for (int i = 0; i < data.categoryGoods.size(); i++) {
            categoryBean = new CommodityBean();
            categoryBean.setCategoryGoods(data.categoryGoods.get(i));
            categoryList.add(categoryBean);
            final TextView titleTv = new TextView(CommodityManagementActivity.this);
            titleTv.setText(categoryBean.getCategoryGoods().name);
            titleTv.setTextSize(25);
            titleTv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.black));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(UIUtils.dip2px(10f), 0, 0, 0);
            titleTv.setLayoutParams(params);
            titleTv.setGravity(Gravity.CENTER);
            titleTv.setTag((data.categoryGoods.get(i)).id);
            classTitleLayout.addView(titleTv);
            final int finalI = i;
            final int finalI1 = i;
            titleTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryChildList.clear();
                    CommodityBean clildBean;
                    for (int j = 0; j < (data.categoryGoods.get(finalI)).child.size(); j++) {
                        if (((data.categoryGoods.get(finalI)).child.get(j)).cid ==
                                (data.categoryGoods.get(finalI1)).id) {
                            clildBean = new CommodityBean();
                            clildBean.setCategoryChilds((data.categoryGoods.get(finalI)).child.get(j));
                            categoryChildList.add(clildBean);
                            ClassAdapter adapter = new ClassAdapter(CommodityManagementActivity.this, categoryChildList);
                            classGv.setAdapter(adapter);
                            titleTv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.green_3CB371));
                            for (int i = 0; i < classTitleLayout.getChildCount(); i++) {
                                TextView tv = (TextView) classTitleLayout.getChildAt(i);
                                if (tv.getTag() == titleTv.getTag()) {
                                    tv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.green_3CB371));
                                } else {
                                    tv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.black));
                                }
                            }
                            allTitleTv.setTextColor(CommodityManagementActivity.this.getResources().getColor(R.color.black));
                        }
                    }
                }
            });
        }
//                            CommodityBean clildBean;
//                            for (int i = 0; i < scalesCategoryGoodsBaseEntity.getData().categoryGoods.size(); i++) {
//                                for (int j = 0; j < ((ScalesCategoryGoods.categoryGoods)scalesCategoryGoodsBaseEntity.getData().categoryGoods.get(i)).child.size(); j++) {
//                                    clildBean = new CommodityBean();
//                                    clildBean.setCategoryChilds((ScalesCategoryGoods.categoryGoods.child) ((ScalesCategoryGoods.categoryGoods)scalesCategoryGoodsBaseEntity.getData().categoryGoods.get(i)).child.get(j));
//                                    categoryChildList.add(clildBean);
//                                }
//                            }
        adapter.notifyDataSetChanged();
        classAdapter.notifyDataSetChanged();
    }

    private SweetAlertDialog mSweetAlertDialog;

    public void closeLoading() {
        if (mSweetAlertDialog != null && mSweetAlertDialog.isShowing()) {
            mSweetAlertDialog.dismissWithAnimation();
        }
    }

    public void showLoading(String titleText) {
        SweetAlertDialog mSweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        mSweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mSweetAlertDialog.setTitleText(titleText);


        mSweetAlertDialog.setCancelable(true);
        mSweetAlertDialog.show();
    }

    public void showLoading() {
        mSweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mSweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mSweetAlertDialog.setCancelable(true);
        mSweetAlertDialog.show();
    }

    private class ClassAdapter extends BaseAdapter {
        List<CommodityBean> list;
        private Context context;

        ClassAdapter(Context context, List<CommodityBean> list) {
            this.list = list;
            this.context = context;
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

        /**
         * @param position    位置索引
         * @param convertView view
         * @param parent      group 控件
         * @return 返回 控件View
         */
        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.commodity_class_item, null);
                holder = new ViewHolder();
                holder.nameBtn = convertView.findViewById(R.id.commodity_class_name_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final CommodityBean item = list.get(position);
            holder.nameBtn.setText(item.getAllGoods() != null ? item.getAllGoods().name : item.getCategoryChilds().name);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    CommodityBean bean = (CommodityBean) classAdapter.getItem(position);
                    AllGoods goods = item.getAllGoods();
                    if (goods!=null&&hotKeyMap.containsKey(goods.getName() + goods.cid)) return;
                    CategoryGoods.child categoryChilds = item.getCategoryChilds();
                    if (categoryChilds != null&&hotKeyMap.containsKey(categoryChilds.name + categoryChilds.cid))return;
                    item.setShow(isShowDelTv);
                    HotKeyBean HotKeyBean = new HotKeyBean();
                    if (goods != null) {
                        HotKeyBean.id = goods.id;
                        HotKeyBean.cid = goods.cid;
                        HotKeyBean.name = goods.name;
                        HotKeyBean.price = goods.price;
                        HotKeyBean.traceable_code = goods.traceable_code;
                        HotKeyBean.is_default = goods.is_default;
                    }
                    if (categoryChilds != null) {
                        HotKeyBean.id = categoryChilds.id;
                        HotKeyBean.cid = categoryChilds.cid;
                        HotKeyBean.name = categoryChilds.name;
                        HotKeyBean.price = categoryChilds.price;
                        HotKeyBean.traceable_code = categoryChilds.traceable_code;
                        HotKeyBean.is_default = categoryChilds.is_default;
                    }
                    CommodityBean hotKeyBean = new CommodityBean();
                    hotKeyBean.setHotKeyBean(HotKeyBean);
                    hotKeyMap.put(hotKeyBean.getHotKeyBean().getName() + hotKeyBean.getHotKeyBean().cid, hotKeyBean);
                    hotKeyList.add(hotKeyBean);

                    for (int i = 0; i < hotKeyList.size() - 1; i++) {
                        for (int j = hotKeyList.size() - 1; j > i; j--) {
                            if (hotKeyList.get(j).getHotKeyBean().id == hotKeyList.get(i).getHotKeyBean().id) {
                                hotKeyList.remove(j);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        class ViewHolder {
            Button nameBtn;
        }
    }


    public interface OnItemClickListener {
        void onItemClickListener(View v);
    }


    private class DragAdapter extends RecyclerView.Adapter<DragAdapter.DragViewHolder> implements OnItemMoveListener {
        private List<CommodityBean> mItems;
        private LayoutInflater mInflater;
        private OnItemClickListener onItemClickListener;

        DragAdapter(Context context, List<CommodityBean> items) {
            mInflater = LayoutInflater.from(context);
            this.mItems = items;
        }

        @NonNull
        @Override
        public DragViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.commodity_item_2, parent, false);
            DragViewHolder holder = new DragViewHolder(view);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteTv(true);
                    return true;
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClickListener(v);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull DragViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            if (mItems.get(position).isShow()) {
                holder.deleteTv.setVisibility(View.VISIBLE);
            }
            if (mItems.get(position).getHotKeyBean().traceable_code >= 0) {
                holder.selectedTv.setVisibility(View.VISIBLE);
            }
            holder.nameTv.setText(mItems.get(position).getHotKeyBean().name);
            holder.deleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItemByPosition(position);
                }
            });
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            CommodityBean item = mItems.get(fromPosition);
            mItems.remove(fromPosition);
            mItems.add(toPosition, item);
            notifyItemMoved(fromPosition, toPosition);
        }

        public void showDeleteTv(boolean show) {
            for (int i = 0; i < mItems.size(); i++) {
                mItems.get(i).setShow(show);
            }
            isShowDelTv = show;
            notifyDataSetChanged();
        }

        private void removeItemByPosition(int position) {
            mItems.remove(position);
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

//        private void startEditMode(RecyclerView parent) {
//            int visibleChildCount = parent.getChildCount();
//            for (int i = 0; i < visibleChildCount; i++) {
//                View view = parent.getChildAt(i);
//                ImageView deleteTv = view.findViewById(R.id.commodity_item_2_delete_tv);
//                if (deleteTv != null) {
//                    deleteTv.setVisibility(View.VISIBLE);
//                }
//            }
//        }

        class DragViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
            private TextView nameTv;
            private ImageView deleteTv;
            private ImageView selectedTv;

            private DragViewHolder(View itemView) {
                super(itemView);
                nameTv = itemView.findViewById(R.id.commodity_item_2_name_tv);
                deleteTv = itemView.findViewById(R.id.commodity_item_2_delete_tv);
                selectedTv = itemView.findViewById(R.id.commodity_item_2_selected_iv);
            }

            @Override
            public void onItemSelected() {
//                deleteTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemFinish() {
//                itemView.setBackgroundColor(0);
            }
        }
    }

    public <T> ObservableTransformer<T, T> setThread() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


}
