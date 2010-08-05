/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.storage.contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.Contacts.People;
import java.util.ArrayList;
import java.util.List;

public class ContactProviderOldApi extends ContactProvider {
    
    protected ContactProviderOldApi(ContentResolver cr) {
        super();
        this.contentResolver = cr;
    }

    @Override
    public List<String> getNumbers() {
        List<String> numbers = new ArrayList<String>();
        
        Cursor cur = contentResolver.query(People.CONTENT_URI,
			null, null, null, null);

        int numberColumn = cur.getColumnIndex(People.NUMBER);

        if (cur.getCount() > 0) {
	     while (cur.moveToNext()) {
                String number = cur.getString(numberColumn);
                numbers.add(number);
	     }
        }
        cur.close();

        return getUniqueNumbers(numbers);
    }
}
