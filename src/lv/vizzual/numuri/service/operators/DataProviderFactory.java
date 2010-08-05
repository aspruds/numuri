/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.operators;

public class DataProviderFactory {
    public static final class countries {
        public static final int LATVIA = 1;
    }
    
    public static DataProvider getDataProvider() {
        int country = countries.LATVIA;
        
        DataProvider provider = null;
        switch(country) {
            case countries.LATVIA: {
                provider = new LVDataProvider();
                break;
            }
            default:
                throw new RuntimeException("unsupported country");
        }

        return provider;
    }
}