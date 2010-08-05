/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.storage.contacts;

import android.content.ContentResolver;
import android.os.Build;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * There are two versions of Contact API. The new API is not available for
 * older devices, so we must handle both APIs.
 */
public abstract class ContactProvider {
    private static final String TAG = "ContentProvider";
    private static ContactProvider instance;
    protected ContentResolver contentResolver;
    
    public static ContactProvider getInstance(ContentResolver cr) {     
        if (instance == null) {
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);

            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
                instance = new ContactProviderOldApi(cr);
            } else {
                instance = new ContactProviderNewApi(cr);
            }
        }
        return instance;
    }

    public abstract List<String> getNumbers();

    /**
     * Helper function which removes duplicates from List.
     */
    protected List<String> getUniqueNumbers(List<String> numbers) {
        HashSet<String> uniqueNumbers = new HashSet<String>(numbers);
        numbers = new ArrayList(uniqueNumbers);
        return numbers;
    }
}
