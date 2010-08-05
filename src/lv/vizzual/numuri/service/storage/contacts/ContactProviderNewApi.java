/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.storage.contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.List;

public class ContactProviderNewApi extends ContactProvider {

    protected ContactProviderNewApi(ContentResolver cr) {
        super();
        this.contentResolver = cr;
    }

    @Override
    public List<String> getNumbers() {
        List<String> numbers = new ArrayList<String>();

        Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 		    null, null, null, null);

        int numberColumn = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        while (phones.moveToNext()) {
            String number = phones.getString(numberColumn);
            numbers.add(number);
        }
 	phones.close();

        return getUniqueNumbers(numbers);
    }

}
