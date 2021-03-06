package com.cryptopay.prototype.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cryptopay.prototype.Constants;
import com.cryptopay.prototype.domain.dto.TransactionDTO;
import com.cryptopay.prototype.fragment.HistoryFragment;
import com.cryptopay.prototype.domain.dto.AdvertDTO;
import com.cryptopay.prototype.fragment.AbstractTabFragment;
import com.cryptopay.prototype.fragment.AdvertFragment;
import com.cryptopay.prototype.fragment.SecondFragment;

import java.util.HashMap;
import java.util.Map;

public class TabsFragmentAdapter extends FragmentPagerAdapter {

    private Map<Integer, AbstractTabFragment> tabs;
    private Context context;
//    private AdvertDTO data;
    private AdvertFragment advertFragment;
    private HistoryFragment historyFragment;

    public  TabsFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        initTabs(context);

    }
    private void initTabs(Context context) {
        //добавляем закладки
        advertFragment = AdvertFragment.getInstance(context);
        historyFragment = HistoryFragment.getInstance(context);
        tabs = new HashMap<>();
        tabs.put(0, advertFragment);
        tabs.put(1, SecondFragment.getInstance(context));
        tabs.put(2, historyFragment);
//        tabs.put(3, FourthFragment.getInstance(context));
    }

    public void setAdvertData(AdvertDTO data) {
        Constants.advertDTO = data;
        advertFragment.refreshList(data);
    }

    public void setTransactionData(TransactionDTO data){
        Constants.transactionDTO = data;
        historyFragment.refreshList(data);
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    public AdvertFragment getAdvertFragment() {
        return advertFragment;
    }

    public HistoryFragment getHistoryFragment() {
        return historyFragment;
    }
}
