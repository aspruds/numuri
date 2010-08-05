/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.operators;

import java.io.IOException;

public interface DataProvider {
    public static final String PREFIX_PLUS = "+";

    public String getNetworkProviderName(String number) throws IOException;
    public boolean canProcessNumber(String number);
    public int getNetworkProviderIcon(String provider);
    public String cleanNumber(String number);
}
