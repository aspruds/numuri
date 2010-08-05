/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.model;

import java.util.Date;

public class PhoneNumber {
    private String phoneNumber;
    private String networkProvider;
    private Date lastUpdated;

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the networkProvider
     */
    public String getNetworkProvider() {
        return networkProvider;
    }

    /**
     * @param networkProvider the networkProvider to set
     */
    public void setNetworkProvider(String networkProvider) {
        this.networkProvider = networkProvider;
    }

    /**
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
