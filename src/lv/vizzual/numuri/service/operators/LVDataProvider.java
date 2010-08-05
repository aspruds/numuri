/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.operators;

import android.text.Html;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lv.vizzual.numuri.R;
import lv.vizzual.numuri.service.error.RequestLimitReachedException;
import lv.vizzual.numuri.service.http.HttpProvider;
import lv.vizzual.numuri.service.http.Request;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class LVDataProvider implements DataProvider {
    private static final String TAG = "LVDataProvider";
    private static final String PREFIX_LV = "+371";
    private static final String URL = "http://www.numuri.lv/default.aspx";
    private static final int SERVICE_SLEEP_TIMEOUT = 14 * 1000;
    private HttpProvider httpProvider = new HttpProvider();
    private ChallengeProvider challenge;

    public String getNetworkProviderName(String number) throws IOException {
        if(challenge == null) {
            challenge = new ChallengeProvider();
        }

        String networkProvider = null;

        Request req = getRequest(number);
        httpProvider.processRequest(req);
        if(req.isSuccess()) {
            String response = req.getResult();
            networkProvider = extractNetworkProvider(response);

            try {
                Thread.sleep(SERVICE_SLEEP_TIMEOUT);
            }
            catch(InterruptedException ignored) {}
        }
        else {
            String message = "request failed";
            Log.d(TAG, message);
            throw new IOException(message);
        }

        return networkProvider;
    }

    public int getNetworkProviderIcon(String provider) {
        int icon = -1;

        if(provider.equalsIgnoreCase("Latvijas Mobilais Telefons")) {
            icon = R.drawable.logo_lmt;
        }
        else if(provider.equalsIgnoreCase("BITE Latvija")) {
            icon = R.drawable.logo_bite;
        }
        else if(provider.equalsIgnoreCase("IZZI")) {
            icon = R.drawable.logo_izzi;
        }
        else if(provider.equalsIgnoreCase("Lattelecom")) {
            icon = R.drawable.logo_lattelecom;
        }
        else if(provider.equalsIgnoreCase("Tele2")) {
            icon = R.drawable.logo_tele2;
        }
        else if(provider.equalsIgnoreCase("Telekom Baltija")) {
            icon = R.drawable.logo_triatel;
        }
        else if(provider.equalsIgnoreCase("ZetCOM")) {
            icon = R.drawable.logo_amigo;
        }

        return icon;
    }

    public String cleanNumber(String number) {
        if(number != null) {
            number = number.replace(" ", "");
            number = number.replace("-", "");
            number = number.replace(".", "");
        }
        return number;
    }
    
    private String extractNetworkProvider(String page) {
        String provider = null;

        if(page.contains("Minūtes laikā var veikt tikai 5 pieprasījumus!")) {
            throw new RequestLimitReachedException();
        }
        
        String exp = "Pakalpojuma nodrošinātājs </td><td><b>(.*?)</b>";
        Pattern pattern = Pattern.compile(exp, Pattern.DOTALL | Pattern.UNIX_LINES);
        Matcher matcher = pattern.matcher(page);
        if(matcher.find()) {
            String providerHTML = matcher.group(1);
            provider = Html.fromHtml(providerHTML).toString();
        }

        return cleanNetworkProvider(provider);
    }


    private Request getRequest(String number) {
        number = number.replace(PREFIX_LV, "").trim();
        number = cleanNumber(number);

        Request req = new Request();
        req.setUrl(URL);
        req.setType(Request.TYPE_POST);

        List <NameValuePair> params = new ArrayList <NameValuePair>();
            params.add(new BasicNameValuePair("__VIEWSTATE", "DAwADgEBDW51c2E6a2V5RmllbGQA"));
            params.add(new BasicNameValuePair("nusa:id_txtBox_" + challenge.getChallengeField(), number));
            params.add(new BasicNameValuePair("nusa:id_txtBox_" + challenge.getChallengeAnswer(), number));
            params.add(new BasicNameValuePair("nusa:keyField", challenge.getChallengeKey()));
            params.add(new BasicNameValuePair("__EVENTTARGET", "nusa:nusaBtnDetermineNumber"));
            params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        req.setData(params);

        List <NameValuePair> headers = new ArrayList <NameValuePair>();
            headers.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; lv-LV; rv:1.9.2.8) Gecko/20100723 Ubuntu/10.04 (lucid) Firefox/3.6.8"));
            headers.add(new BasicNameValuePair("Host", "www.numuri.lv"));
            headers.add(new BasicNameValuePair("Referer", URL));
        req.setHeaders(headers);

        return req;
    }

    private String cleanNetworkProvider(String provider) {
        if(provider != null) {
            provider = provider.replace("A/S \"", "");
            provider = provider.replace("SIA \"", "");
            provider = provider.replace("Filiāle \"", "");

            if(provider.endsWith("\"") && provider.length() > 1) {
                provider = provider.substring(0, provider.length()-1);
            }
        }
        return provider;
    }

    public boolean canProcessNumber(String number) {
        boolean canProcess = true;

        // ignore if does not start with country code for Latvia
        if(number.startsWith(PREFIX_PLUS)) {
            if(!number.startsWith(PREFIX_LV)) {
                canProcess = false;
            }
        }
        else {
            number.replace(PREFIX_LV, "").trim();
            number = cleanNumber(number);
            // in Latvia numbers are shorter than 8 signs
            if(number.length() > 8) {
                canProcess = false;
            }
        }
        return canProcess;
    }

    private class ChallengeProvider {
        private String challengeField = null;
        private String challengeKey = null;
        private String challengeAnswer = null;

        public ChallengeProvider() throws IOException {
                    // initialize challenge
                Request req = getChallengeRequest();
                httpProvider.processRequest(req);

                if(req.isSuccess()) {
                    String response = req.getResult();

                    challengeField = retrieveChallengeField(response);
                    challengeKey = retrieveChallengeKey(response);
                    challengeAnswer = retrieveChallengeAnswer(challengeKey);
                }
                else {
                    String message = "challenge request failed";
                    Log.d(TAG, message);
                    throw new RuntimeException(message);
                }
        }

        private Request getChallengeRequest() {
            Request req = new Request();
            req.setUrl(URL);
            req.setType(Request.TYPE_GET);
            return req;
        }

        private String retrieveChallengeAnswer(String key) {
            int magic = 43;
            int fieldCount = 20;

            for(int i=0; i < key.length(); i++){
                    magic+=key.charAt(i);
            }
            return String.valueOf(magic % fieldCount);
        }

        private String retrieveChallengeField(String page) {
            String field = null;

            String exp = "\\Qr( document.getElementById('nusa_id_txtBox_\\E(.*?)\\'";
            Pattern pattern = Pattern.compile(exp, Pattern.DOTALL | Pattern.UNIX_LINES);
            Matcher matcher = pattern.matcher(page);
            if(matcher.find()) {
                field = matcher.group(1);
            }

            return field;
        }

        private String retrieveChallengeKey(String page) {
            String key = null;

            String exp = "id=\"nusa_keyField\" type=\"hidden\" value=\"(.*?)\"";
            Pattern pattern = Pattern.compile(exp, Pattern.DOTALL | Pattern.UNIX_LINES);
            Matcher matcher = pattern.matcher(page);
            if(matcher.find()) {
                key = matcher.group(1);
            }

            return key;
        }

        public String getChallengeField() {
            return challengeField;
        }

        public String getChallengeKey() {
            return challengeKey;
        }

        public String getChallengeAnswer() {
            return challengeAnswer;
        }
    }
}
