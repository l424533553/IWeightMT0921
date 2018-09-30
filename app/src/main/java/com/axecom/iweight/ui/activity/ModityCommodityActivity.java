package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.bean.CommodityBean;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.ui.view.SoftKey;
import java.util.Objects;

public class ModityCommodityActivity extends BaseActivity {

    private View rootView;

    private TextView idTv;
    private TextView nameTv;
    private EditText priceEt;
    private EditText traceableEt;
    private CheckedTextView isDefaultCtv;

    private SoftKey softKey;
    private CommodityBean bean;
    private int position;

    @SuppressLint("InflateParams")
    @Override
    public View setInitView() {
        rootView = LayoutInflater.from(this).inflate(R.layout.modity_commodity_price_layout, null);
        idTv = rootView.findViewById(R.id.modity_commodity_id_tv);
        nameTv = rootView.findViewById(R.id.modity_commodity_name_tv);
        priceEt = rootView.findViewById(R.id.modity_commodity_price_tv);
        traceableEt = rootView.findViewById(R.id.modity_commodity_traceable_code_tv);
        isDefaultCtv = rootView.findViewById(R.id.modity_commodity_is_default_tv);
        rootView.findViewById(R.id.modity_commodity_confirm_btn).setOnClickListener(this);
        rootView.findViewById(R.id.modity_commodity_cancel_btn).setOnClickListener(this);
        softKey = rootView.findViewById(R.id.modity_commodity_softkey);

        priceEt.requestFocus();
        disableShowInput(priceEt);

        return rootView;
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        bean = (CommodityBean) Objects.requireNonNull(intent.getExtras()).getSerializable("commodityBean");
        position = intent.getIntExtra("position", -1);
        nameTv.setText(bean.getHotKeyBean().getName());
        idTv.setText(bean.getHotKeyBean().getId());
        priceEt.setText( String.valueOf(bean.getHotKeyBean().price)  );
        traceableEt.setText(String.valueOf(bean.getHotKeyBean().traceable_code ));
        isDefaultCtv.setChecked(bean.getHotKeyBean().is_default != 0);
        softKey.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getAdapter().getItem(position).toString();
                switch (rootView.findFocus().getId()) {
                    case R.id.modity_commodity_price_tv:
                        setEditText(priceEt, position, text);
                        break;
                    case R.id.modity_commodity_traceable_code_tv:
                        setEditText(traceableEt, position, text);
                        break;
                }
            }
        });

        isDefaultCtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDefaultCtv.setChecked(!isDefaultCtv.isChecked());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modity_commodity_confirm_btn:
                HotKeyBean goods = new HotKeyBean();
                goods.id = bean.getHotKeyBean().id;
                goods.cid = bean.getHotKeyBean().cid;
                goods.name = bean.getHotKeyBean().name;
                goods.price = priceEt.getText().toString();
                goods.traceable_code = Integer.parseInt(traceableEt.getText().toString());
                goods.is_default = isDefaultCtv.isChecked() ? 1 : 0;
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("HotKeyBean", goods);
                intent.putExtra("position", position);
                intent.putExtras(bundle);
                setResult(1001, intent);
                finish();
                break;
            case R.id.modity_commodity_cancel_btn:
                finish();
                break;
        }
    }
}
