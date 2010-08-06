/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lv.vizzual.numuri.C;
import lv.vizzual.numuri.R;
import lv.vizzual.numuri.model.PhoneNumber;
import lv.vizzual.numuri.service.error.RequestLimitReachedException;
import lv.vizzual.numuri.service.operators.DataProvider;
import lv.vizzual.numuri.service.operators.DataProviderFactory;
import lv.vizzual.numuri.service.storage.NumberAdapter;
import lv.vizzual.numuri.service.storage.contacts.ContactProvider;

public class MainActivity extends Activity {
    private static String TAG = "MainActivity";
    
    private AlertDialog startUpdateDialog;
    private AlertDialog networkErrorDialog;
    private AlertDialog updateCompleteDialog;
    private ProgressDialog updateProgressDialog;

    private UpdateThread updateProgressThread;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        showDialog(C.dialog.START_UPDATE_DIALOG);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case C.dialog.START_UPDATE_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setMessage(R.string.update_start_desc);
                builder.setTitle(R.string.update_start_title);
                builder.setPositiveButton(R.string.continue_download,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            showDialog(C.dialog.UPDATE_PROGRESS_DIALOG);

                            updateProgressThread = new UpdateThread(updateHandler);
                            updateProgressThread.start();
                        }
                    });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                });

                startUpdateDialog = builder.create();
                return startUpdateDialog;
            }
            case C.dialog.UPDATE_PROGRESS_DIALOG: {
                updateProgressDialog = new ProgressDialog(MainActivity.this);
                updateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                updateProgressDialog.setMessage(getString(R.string.updating_data));
                updateProgressDialog.setCancelable(true);
                updateProgressDialog.setButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            updateProgressThread.setState(UpdateThread.STATE_DONE);
                            finish();
                        }
                    });
                updateProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        updateProgressThread.setState(UpdateThread.STATE_DONE);
                        return;
                    }
                });
                return updateProgressDialog;
            }
            case C.dialog.UPDATE_ERROR_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.update_failed_desc))
                       .setTitle(getString(R.string.update_failed_title))
                       .setCancelable(false)
                       .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        });
                networkErrorDialog = builder.create();

                return networkErrorDialog;
            }
           case C.dialog.UPDATE_COMPLETE_DIALOG: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.update_complete_desc))
                       .setTitle(getString(R.string.update_complete_title))
                       .setCancelable(false)
                       .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                                finish();
                            }
                        });
                updateCompleteDialog = builder.create();

                return updateCompleteDialog;
            }
            default:
                return null;
        }
    }

    final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int status = msg.getData().getInt(UpdateThread.KEY_STATUS);
            switch(status) {
                case UpdateThread.STATUS_COMPLETE: {
                    removeDialog(C.dialog.UPDATE_PROGRESS_DIALOG);
                    showDialog(C.dialog.UPDATE_COMPLETE_DIALOG);
                    break;
                }
                case UpdateThread.STATUS_ERROR: {
                    removeDialog(C.dialog.UPDATE_PROGRESS_DIALOG);
                    showDialog(C.dialog.UPDATE_ERROR_DIALOG);
                    break;
                }
                case UpdateThread.STATUS_NUMBERS_FOUND: {
                    int number = msg.getData().getInt(UpdateThread.KEY_NUMBERS);
                    updateProgressDialog.setMax(number);
                }
                case UpdateThread.STATUS_PROCESSING_NUMBER: {
                    updateProgressDialog.incrementProgressBy(1);
                    break;
                }
            }
        }
    };

    /** Nested class that performs progress calculations (counting) */
    private class UpdateThread extends Thread {
        Handler mHandler;

        final static String KEY_STATUS  = "status";
        final static String KEY_NUMBERS  = "numbers";

        final static int STATUS_COMPLETE = 1;
        final static int STATUS_ERROR = 2;
        final static int STATUS_NUMBERS_FOUND = 3;
        final static int STATUS_PROCESSING_NUMBER = 4;

        final static int STATE_RUNNING = 1;
        final static int STATE_DONE = 2;

        private DataProvider dataProvider;
        private NumberAdapter db;
        private int state;
        
        UpdateThread(Handler h) {
            mHandler = h;
            dataProvider = DataProviderFactory.getDataProvider();
            state = STATE_RUNNING;
        }

        void sendMessage(int status) {
            if(state != STATE_DONE) {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt(KEY_STATUS, status);
                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }

        void sendCustomMessage(Message msg) {
            if(state != STATE_DONE) {
                mHandler.sendMessage(msg);
            }
        }

        public void setState(int state) {
            this.state = state;
        }

        @Override
        public void run() {
            db = new NumberAdapter(getApplicationContext());
            
            try {
                db.open();
                obtainNumbers();
                sendMessage(STATUS_COMPLETE);
            }
            catch(RequestLimitReachedException ex) {
                Log.d(TAG, "request limit", ex);
                sendMessage(STATUS_ERROR);
            }
            catch(Exception ex){
                Log.d(TAG, "service error", ex);
                sendMessage(STATUS_ERROR);
            }
            finally {
                if(db != null) {
                    db.close();
                }
            }
        }

        private void obtainNumbers() throws IOException {
            List<String> numbers = ContactProvider.getInstance(getContentResolver()).getNumbers();

            // set progress bar range
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt(KEY_STATUS, STATUS_NUMBERS_FOUND);
            b.putInt(KEY_NUMBERS, numbers.size());
            msg.setData(b);
            sendCustomMessage(msg);

            for(String number: numbers) {
                if(state == STATE_DONE) {
                    break;
                }
                processNumber(number);
                advanceProgressBar();
            }
        }

        private void processNumber(String number) throws IOException {
            number = dataProvider.cleanNumber(number);
            
            Log.d(TAG, "getting provider for " + number);

            // check if database contains cached data for number
            if(db.hasDataForNumber(number)) {
                Log.d(TAG, "found cached data about " + number);
                return;
            }

            // check if datastore knows how to process number
            // perhaps the number is too short or too long?
            if(!dataProvider.canProcessNumber(number)) {
                Log.d(TAG, "can not understand number " + number);
                return;
            }

            // obtain network provider
            String networkProvider = dataProvider.getNetworkProviderName(number);
            if(networkProvider != null) {
                Log.d(TAG, number + " has provider " + networkProvider);
            }
            else {
                Log.d(TAG, number + " has no provider");
            }

            // save provider
            PhoneNumber  nr = new PhoneNumber();
            nr.setPhoneNumber(number);
            nr.setNetworkProvider(networkProvider);
            nr.setLastUpdated(new Date());
            db.replace(nr);
        }

        private void advanceProgressBar() {
            // update progress bar
            Message progressMessage = mHandler.obtainMessage();
            Bundle progressBundle = new Bundle();
            progressBundle.putInt(KEY_STATUS, STATUS_PROCESSING_NUMBER);
            progressMessage.setData(progressBundle);
            sendCustomMessage(progressMessage);
        }
    }    
}
