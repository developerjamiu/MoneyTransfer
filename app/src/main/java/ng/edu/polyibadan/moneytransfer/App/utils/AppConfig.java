package ng.edu.polyibadan.moneytransfer.App.utils;

public class AppConfig {

    //Public Key
    public static String publicKey = "FLWPUBK_TEST-37dce29d8e23f72b362903a9d035c052-X";

    //Encryption Key
    public static String encryptionKey = "FLWSECK_TESTb2bd909822df";

    // Server User Login Url
    public static String URL_LOGIN = "http://ancient-plains-77619.herokuapp.com/api/auth";
    //public static String URL_LOGIN = "http://10.0.2.2:5000/api/auth";

    // Server User Register Url
    public static String URL_REGISTER = "http://ancient-plains-77619.herokuapp.com/api/users";
    //public static String URL_REGISTER = "http://10.0.2.2:5000/api/users";

    // Server User Info Url
    public static String URL_ME = "http://ancient-plains-77619.herokuapp.com/api/users/me";
    //public static String URL_ME = "http://10.0.2.2:5000/api/users/me";

    // Server Fund Wallet Url
    public static String URL_FUND = "http://ancient-plains-77619.herokuapp.com/api/wallet/fund";
    //public static String URL_FUND = "http://10.0.2.2:5000/api/wallet/fund";

    // Server Transfer Money Url
    public static String URL_TRANSFER = "http://ancient-plains-77619.herokuapp.com/api/wallet/transfer";
    //public static String URL_TRANSFER = "http://10.0.2.2:5000/api/wallet/transfer";

    // Server Fetch Recipient Money Url
    public static String URL_FETCH_RECIPIENT = "http://ancient-plains-77619.herokuapp.com/api/wallet/recipient";
    //public static String URL_FETCH_RECIPIENT = "http://10.0.2.2:5000/api/wallet/recipient?emailAddress=";

    // Server TM Transfer Money Url
    public static String URL_TM_TRANSFER = "http://ancient-plains-77619.herokuapp.com/api/wallet/tmtransfer";
    //public static String URL_TM_TRANSFER = "http://10.0.2.2:5000/api/wallet/tmtransfer";
}
