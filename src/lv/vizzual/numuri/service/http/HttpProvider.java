/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.http;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

public class HttpProvider {
    private static final String TAG = "HttpProvider";
    private static final int CONNECTION_TIMEOUT = 25 * 1000;
    private HttpClient client;

    public HttpProvider() {
        init();
    }

    private void init() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);

        client = new DefaultHttpClient(httpParameters);
    }

    public void processRequest(Request req) throws IOException {
        if (req.getType() == Request.TYPE_GET) {
            processGet(req);
        } else {
            processPost(req);
        }
    }

    private void processGet(Request req) {
        HttpGet get = null;
        String result = null;
        HttpResponse response = null;

        try {
            get = new HttpGet(new URI(req.getUrl()));
            response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                result = convertStreamToString(is);

                req.setResult(result);
                req.setSuccess(true);
            }
        } catch (IOException ex) {
            Log.d(TAG, "request failed", ex);
            req.setSuccess(false);
        }
        catch (URISyntaxException ex) {
            Log.d(TAG, "uri invalid", ex);
            req.setSuccess(false);
        }
    }

    private void processPost(Request req) throws IOException {
        HttpPost post = null;
        String result = null;
        HttpResponse response = null;

        try {
            post = new HttpPost(new URI(req.getUrl()));
            for(NameValuePair nv: req.getHeaders()) {
                post.setHeader(nv.getName(), nv.getValue());
            }
            
            post.setEntity(new UrlEncodedFormEntity(req.getData(), HTTP.UTF_8));
            response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                result = convertStreamToString(is);

                req.setResult(result);
                req.setSuccess(true);
            }
        } catch (IOException ex) {
            Log.d(TAG, "request failed", ex);
            req.setSuccess(false);
        }
        catch (URISyntaxException ex) {
            Log.d(TAG, "uri invalid", ex);
            req.setSuccess(false);
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8192);
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}
