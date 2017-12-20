package com.prototype.prototype;

import com.prototype.prototype.domain.Transaction;
import com.prototype.prototype.domain.Wallet;
import com.prototype.prototype.domain.dto.AdvertDTO;
import com.prototype.prototype.domain.dto.ItemDTO;
import com.prototype.prototype.domain.dto.TransactionDTO;

import java.math.BigInteger;
import java.util.ArrayList;

public class Constants {
    public static final int TAB_ONE = 0;
    public static final int TAB_TWO = 1;
    public static final int TAB_THREE = 2;
    public static AdvertDTO advertDTO;
    public static ItemDTO itemDTO;
    public static TransactionDTO transactionDTO = new TransactionDTO();
    public static int CART_COUNT = 0;
    public static Wallet wallet;
    public static BigInteger balance = new BigInteger("0");

    //saving preferences
    public static final String wallet_address = "wallet_address";
    public static final String wallet_password = "wallet_password";
    public static final String wallet_file = "wallet_file";
    public static final String wallet_publicKey = "wallet_publicKey";
    public static final String wallet_privateKey = "wallet_privateKey";

    //saving settings preferences
    public static final String settings_show_splash = "settings_show_splash";


    public static class URL {
        public static String PORT = "";
        public static String HOST = "http://192.168.0.101:8080"+PORT+"/";
        public static String FIND_ALL_ADVERT = HOST+"findalladvert";
        public static String FIND_ALL_ITEM = HOST+"findallitembyid";
        public static String GET_TEST_ETH = HOST+"gettesteth/"+wallet.getAddress();
        public static String FIND_ALL_TRANSACTION = HOST+"findalltransactionbyaddress/"+wallet.getAddress();
        public static String GET_STATUS = HOST+"getstatus/"+wallet.getAddress();
        public static String SAVE_TRANSACTION = HOST+"savetransaction/";
        public static String TRANSACTION_VIEWER = "https://rinkeby.etherscan.io/tx/";

    }

}
