package br.com.cadim.cadim.Controller;

public class Api {

    private static final String ROOT_URL = "http://cadim.000webhostapp.com/Api.php?apicall=";

//    private static final String ROOT_URL = "http://10.99.17.98/cadim/api/Api.php?apicall=";

    public static final String URL_LOGIN = ROOT_URL + "login";
    public static final String URL_DIAGNOSTIC_LIST = ROOT_URL + "diagnosticList";
    public static final String URL_DIAGNOSTIC_ECG_LIST = ROOT_URL + "diagnosticEcgList";
    public static final String URL_HISTORIC_LIST = ROOT_URL + "historicEcgList";

}