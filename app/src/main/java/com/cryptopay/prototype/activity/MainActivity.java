package com.cryptopay.prototype.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cryptopay.prototype.domain.TypeItem;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.cryptopay.prototype.Constants;
import com.cryptopay.prototype.R;
import com.cryptopay.prototype.activity.general.LoadingDialog;
import com.cryptopay.prototype.activity.general.LoadingView;
import com.cryptopay.prototype.adapter.AdvertListAdapter;
import com.cryptopay.prototype.domain.Advert;
import com.cryptopay.prototype.domain.Wallet;
import com.cryptopay.prototype.domain.dto.AdvertDTO;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdvertListAdapter.OnItemClick, SwipeRefreshLayout.OnRefreshListener {

    public static final int LAYOUT = R.layout.activity_main;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private FloatingActionsMenu fab_menu;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_history;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_wallet;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_cart;
    private SharedPreferences sPref;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Advert> adverts;
    private AdvertListAdapter mAdapter;
    private LoadingView mLoadingView;
    private SearchView searchView;
    private List<Advert> searchList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(LAYOUT);
//        runSplash();

        initWallet();
        initToolbar();
        initNavigationView();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new AdvertListAdapter(new ArrayList<Advert>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mLoadingView = LoadingDialog.view(getSupportFragmentManager());
        mLoadingView.showLoadingIndicator();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        new AdvertTask().execute();
        fab_menu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        fab_history = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_history);
        fab_cart = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_cart);
        fab_wallet = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_wallet);
        fab_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                fab_history.setTitle("ok");
                fab_menu.collapse();
                switchPurse();
            }
        });
        fab_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.collapse();
                switchHistory();
            }
        });
        fab_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.collapse();
//                switchSend();
            }
        });

        searchView = (SearchView) findViewById(R.id.svSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                mLoadingView.showLoadingIndicator();
                searchList = new ArrayList<>();
                for (Advert advert : adverts) {
                    if (advert.getTitle().toLowerCase().contains(newText) || advert.getDescription().toLowerCase().contains(newText)) {
                        searchList.add(advert);
                    }
                }
                mAdapter.changeDataSet(searchList);
                mLoadingView.hideLoadingIndicator();
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mLoadingView.showLoadingIndicator();
                mAdapter.changeDataSet(adverts);
                mLoadingView.hideLoadingIndicator();
                return false;
            }
        });
        new Web3Utils.GetAllTypeItem().execute();

        sPref = getPreferences(MODE_PRIVATE);
        if (!sPref.getBoolean(Constants.save_recovery, false)) {
            if (Constants.wallet != null && Constants.wallet.getFile() != null && !Constants.wallet.getFile().equals("")) {
                showDialogRecovery();
            }
        }
    }

    public static final int SWITCH_FROM_MAIN_FOR_RECOVERY = 500;

    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private final static int REQUEST_READ_EXTERNAL_STORAGE = 4;

    public void showDialogRecovery() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save your key-file for next recovery ethereum wallet");

        builder.setNeutralButton("On device", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath()+File.separatorChar+"key"+Constants.wallet.getAddress()+".json";

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;

                try {
                    bis = new BufferedInputStream(new FileInputStream(Constants.wallet.getFile()));
                    bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                    byte[] buf = new byte[1024];
                    bis.read(buf);
                    do {
                        bos.write(buf);
                    } while(bis.read(buf) != -1);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bis != null) bis.close();
                        if (bos != null) bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "Key file saved in "+destinationFilename,Toast.LENGTH_LONG).show();
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor edit = sPref.edit();
                    edit.putBoolean(Constants.save_recovery, true);
                    edit.commit();
                }

            }
        });
//        builder.setPositiveButton("Send mail", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//                try {
////                    File Root= Environment.getExternalStorageDirectory();
//                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
//                    intent.setType("text/plain");
//                    String message="File to be shared is key.";
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile( new File(Constants.wallet.getFile())));
//                    intent.putExtra(Intent.EXTRA_TEXT, message);
////                    intent.setData(Uri.parse("mailto:your email"));
////                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    startActivity(intent);
//
//                    sPref = getPreferences(MODE_PRIVATE);
//                    SharedPreferences.Editor edit = sPref.edit();
//                    edit.putBoolean(Constants.save_recovery, true);
//                    edit.commit();
//
//                } catch(Exception e)  {
//                    System.out.println("is exception raises during sending mail"+e);
//                }
//            }
//        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SWITCH_FROM_MAIN_FOR_RECOVERY) {
            if (resultCode == RESULT_OK) {
                System.out.println("ok");
            }
        }
    }

    private void initWallet() {
//        sPref = getPreferences(MODE_PRIVATE);
//        Constants.wallet = new Wallet();
//        if (!sPref.getString(Constants.wallet_address, "").equals("")) {
//            Constants.wallet.setAddress(sPref.getString(Constants.wallet_address, ""));
//            Constants.wallet.setPassword(sPref.getString(Constants.wallet_password, ""));
//            Constants.wallet.setFile(sPref.getString(Constants.wallet_file, ""));
//            Constants.wallet.setPublicKey(new BigInteger(sPref.getString(Constants.wallet_publicKey, "0")));
//            Constants.wallet.setPrivateKey(new BigInteger(sPref.getString(Constants.wallet_privateKey, "0")));
//            new Web3Utils.GetBalanceWallet().execute();
//        } else {
//            finish();
////            new Web3Utils.GenerateNewWallet().execute(getApplicationContext().getFilesDir().getAbsolutePath(), "123123123", getPreferences(MODE_PRIVATE));
//        }


    }


    public static final int SWITCH_FROM_MAIN = 300;

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        toolbar.inflateMenu(R.menu.menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.location:

                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        Constants.advertDTO.setData(adverts);
                        intent.putExtra("from", SWITCH_FROM_MAIN);
                        startActivityForResult(intent, SWITCH_FROM_MAIN);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.view_nav_open, R.string.view_nav_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
//                    case R.id.main:
//                        break;
//                    case R.id.settings:
//                        switchSetting();
//                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void switchSend() {
        Intent intent = new Intent(MainActivity.this, SendActivity.class);
        startActivity(intent);
    }

    public void switchPurse() {
        Intent intent = new Intent(MainActivity.this, PurseActivity.class);
        startActivity(intent);
    }

    public void switchHistory() {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    public void switchContent(Advert advert) {
        ShopSectionActivity.advert = advert;
        Intent intent = new Intent(MainActivity.this, ShopSectionActivity.class);
        startActivity(intent);
    }

    public void switchSetting() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }


    @Override
    public void onRefresh() {
        new AdvertTask().execute();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onItemClick(@NonNull Advert advert) {
        switchContent(advert);
    }


    private void showError() {
        mLoadingView.hideLoadingIndicator();
        Snackbar snackbar = Snackbar.make(recyclerView, "Error loading adverts", Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> new AdvertTask().execute());
        snackbar.setDuration(3000);
        snackbar.show();
    }


    private class AdvertTask extends AsyncTask<Void, Void, AdvertDTO> {

        @Override
        protected AdvertDTO doInBackground(Void... params) {
            RestTemplate template = new RestTemplate();
            template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            try {
                return template.getForObject(Constants.URL.FIND_ALL_ADVERT, AdvertDTO.class);
            } catch (RuntimeException exception) {
                System.out.println(exception.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(AdvertDTO advertDTO) {
            if (advertDTO == null || advertDTO.getData() == null) {
                showError();
                return;
            }
            mLoadingView.hideLoadingIndicator();
            adverts = advertDTO.getData();
            mAdapter.changeDataSet(adverts);

        }
    }


}
