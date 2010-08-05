/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import lv.vizzual.numuri.R;
import lv.vizzual.numuri.service.operators.DataProviderFactory;
import lv.vizzual.numuri.service.storage.NumberAdapter;

public class CallListener extends BroadcastReceiver {

    private static String TAG = "CallListener";
    String number;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_NEW_OUTGOING_CALL)) {
            number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "searching network provider for " + number);
            
            NumberAdapter db = new NumberAdapter(context);
            try {
                db.open();

                String provider = db.getNetworkProvider(number);
                if (provider != null) {
                    showToast(context, provider);
                    Log.d(TAG, "found network provider" + provider + " for number " + number);
                }
                else {
                    Log.d(TAG, "no network provider found for " + number);
                }
            }
            finally {
                try {
                    db.close();
                }
                catch(RuntimeException ex) {
                    throw ex;
                }
            }
            return;
        }
    }

    public void showToast(Context context, String networkProvider) {
        Toast toast = Toast.makeText(context, networkProvider, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 30);
        View vw = toast.getView();
        Resources resources = vw.getResources();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.toast, null);

        ImageView imageView = (ImageView) layout.findViewById(R.id.toast_image);
        int resLogo = DataProviderFactory.getDataProvider().getNetworkProviderIcon(networkProvider);
        if (resLogo != -1) {
            imageView.setImageDrawable(resources.getDrawable(resLogo));
        } else {
            imageView.setVisibility(View.GONE);
        }

        TextView operatorView = (TextView) layout.findViewById(R.id.networkOperatorValue);
        operatorView.setText(networkProvider);

        toast.setView(layout);
        toast.show();
    }
}

