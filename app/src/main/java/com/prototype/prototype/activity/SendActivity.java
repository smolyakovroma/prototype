package com.prototype.prototype.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.prototype.prototype.Constants;
import com.prototype.prototype.R;
import com.prototype.prototype.adapter.ShopListAdapter;

public class SendActivity extends AppCompatActivity {
    public static final int LAYOUT = R.layout.activity_send;
    private Toolbar toolbar;
    private EditText etAddressRecepient, etAmount, etTemplate;
    private CheckBox cbSaveTemplate;
    private TextView tvNameTemplate, tvBalance;
    public static final int CHOOSE_ADDRESS_FROM_QR = 100;
    private double balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        Intent intent = getIntent();
        initToolbar();
        etAddressRecepient = (EditText) findViewById(R.id.et_address_recepient);
        etAmount = (EditText) findViewById(R.id.et_amount);
        etTemplate = (EditText) findViewById(R.id.et_template);
        cbSaveTemplate = (CheckBox) findViewById(R.id.cb_save_template);
        tvNameTemplate = (TextView) findViewById(R.id.tv_name_template);
        balance = intent.getDoubleExtra("balance", 0d);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        tvBalance.setText(getResources().getString(R.string.text_your_balance)+" :"+balance+" ETH");

        cbSaveTemplate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    tvNameTemplate.setVisibility(View.VISIBLE);
                    etTemplate.setVisibility(View.VISIBLE);
                } else {
                    tvNameTemplate.setVisibility(View.INVISIBLE);
                    etTemplate.setVisibility(View.INVISIBLE);
                    etTemplate.setText("");
                }
            }
        });

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.send_toolbar);
        toolbar.setTitle(R.string.send_title);

//        toolbar.setBackgroundColor(getResources().getColor(R.color.whiteColor));
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.ic_arrow_left_thick));

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.qrcode:
                        Intent intent = new Intent(SendActivity.this, QRReadActivity.class);
                        startActivityForResult(intent, CHOOSE_ADDRESS_FROM_QR);
                        break;
                    case R.id.favfolder:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        toolbar.inflateMenu(R.menu.send_menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_ADDRESS_FROM_QR) {
            if (resultCode == RESULT_OK) {
                etAddressRecepient.setText(data.getStringExtra(QRReadActivity.ADDRESS));
            }else {

            }
        }
    }

}