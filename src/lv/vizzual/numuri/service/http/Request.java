/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.http;

import java.util.List;
import org.apache.http.NameValuePair;

public class Request {
    public static final int TYPE_POST = 1;
    public static final int TYPE_GET = 2;

    private String url;
    private List<NameValuePair> data;
    private int type = TYPE_GET;
    private boolean success;
    private String result;
    private List<NameValuePair> headers;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the data
     */
    public List<NameValuePair> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<NameValuePair> data) {
        this.data = data;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the headers
     */
    public List<NameValuePair> getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(List<NameValuePair> headers) {
        this.headers = headers;
    }
}
