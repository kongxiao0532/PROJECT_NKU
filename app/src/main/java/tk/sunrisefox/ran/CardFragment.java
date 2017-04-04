package tk.sunrisefox.ran;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kongx.nkuassistant.R;
import com.kongx.nkuassistant.WelcomeActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardFragment extends Fragment implements NFCCardReader.CardIDCallback {

    private Activity m_activity;
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public NFCCardReader mCardReader;

    private boolean ensureBackup() {
        File brcmBackup = new File(m_activity.getExternalFilesDir("backup"), "libnfc-brcm.conf");
        File nxpBackup = new File(m_activity.getExternalFilesDir("backup"), "libnfc-nxp.conf");
        return !(!brcmBackup.exists() || !nxpBackup.exists());
    }

    private boolean ensureRoot(){
        boolean rooted = false;
        Process suProcess;
        try
        {
            suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
                os.writeBytes("id\n");
                os.flush();
            String currUid = null;
                try {
                    currUid = new Scanner(osRes).nextLine();
                }catch (NoSuchElementException e){ }
                boolean exitSu = false;
                if (currUid == null) {
                    rooted = false;
                    exitSu = false;
                }
                else if (currUid.contains("uid=0")) {
                    rooted = true;
                    exitSu = true;
                }
                else {
                    rooted = false;
                    exitSu = true;
                }
                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
        }
        catch (Exception e) {
            Log.e("SU",Log.getStackTraceString(e));
            rooted = false;
        }
        Log.e("SU",rooted+"");
        return rooted;
    }

    private void restoreBackup() throws IOException, InterruptedException {
        String path = m_activity.getExternalFilesDir("backup").getAbsolutePath();
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        outputStream.writeBytes("mount -o rw,remount,rw /system\n");
        outputStream.writeBytes("cp -f " + path + "/libnfc-brcm.conf /system/etc/ \n");
        outputStream.writeBytes("chmod 644 /system/etc/libnfc-brcm.conf\n");
        outputStream.writeBytes("cp -f " + path + "/libnfc-nxp.conf /system/etc/ \n");
        outputStream.writeBytes("chmod 644 /system/etc/libnfc-nxp.conf\n");
        outputStream.writeBytes("mount -o ro,remount,ro /system\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        process.waitFor();
    }

    private boolean backup(File brcm, File nxp) {
        FileChannel source;
        FileChannel destination;

        File brcmBackup = new File(m_activity.getExternalFilesDir("backup"), "libnfc-brcm.conf");
        File nxpBackup = new File(m_activity.getExternalFilesDir("backup"), "libnfc-nxp.conf");
        try {
            if (!brcmBackup.exists()) {
                if (!brcmBackup.createNewFile())
                    throw new IOException("Could not create backup file.");
                source = new FileInputStream(brcm).getChannel();
                destination = new FileOutputStream(brcmBackup).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            if (!nxpBackup.exists()) {
                if (!nxpBackup.createNewFile())
                    throw new IOException("Could not create backup file.");
                source = new FileInputStream(nxp).getChannel();
                destination = new FileOutputStream(nxpBackup).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void prepareConf() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        Log.e("A","conf");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        if (m_activity.getExternalCacheDir() == null)
            throw new IOException("Could not get external cache dir.");
        String path = m_activity.getExternalCacheDir().getAbsolutePath();

        outputStream.writeBytes("cp -f /system/etc/libnfc-brcm.conf " + path + "\n");
        outputStream.writeBytes("cp -f /system/etc/libnfc-nxp.conf " + path + "\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        process.waitFor();
        Log.e("A","confa");
    }

    private void updateConf(String cardID) throws IOException {
        File brcm = new File(m_activity.getExternalCacheDir(), "libnfc-brcm.conf");
        File nxp = new File(m_activity.getExternalCacheDir(), "libnfc-nxp.conf");
        if (!ensureBackup()) backup(brcm, nxp);

        String brcm_str = new Scanner(new FileInputStream(brcm)).useDelimiter("\\A").next();
        Matcher m = Pattern.compile("NFA_DM_START_UP_CFG=\\{(..).+?((?::33:04:.+?)?\\})").matcher(brcm_str);
        if (!m.find()) throw new IOException("File not supported");
        brcm_str = new StringBuilder(brcm_str).replace(m.start(1), m.end(1), Integer.toHexString(Integer.parseInt(m.group(1), 16) + 6)).toString();
        brcm_str = new StringBuilder(brcm_str).replace(m.start(2), m.end(2), ":33:04:" + cardID.substring(0, 2) + ":" + cardID.substring(2, 4) + ":" + cardID.substring(4, 6) + ":" + cardID.substring(6, 8) + "}").toString();
        new FileOutputStream(brcm, false).write(brcm_str.getBytes());

        String nxp_str = new Scanner(new FileInputStream(nxp)).useDelimiter("\\A").next();
        nxp_str = nxp_str.replaceFirst("(NXP_DEFAULT_SE=).+", "$1\\0x00");
        nxp_str = nxp_str.replaceFirst("(DEFAULT_AID_ROUTE=).+", "$1\\0x00");
        nxp_str = nxp_str.replaceFirst("(DEFAULT_DESFIRE_ROUTE=).+", "$1\\0x00");
        nxp_str = nxp_str.replaceFirst("(DEFAULT_MIFARE_CLT_ROUTE=).+", "$1\\0x00");
        m = Pattern.compile("(?s)NXP_CORE_CONF=\\{(?:[\\w\\d]{2}, ?){2}([\\w\\d]{2}).+?(33, 00,).+?\\}").matcher(nxp_str);
        cardID = cardID.substring(0, 2) + ", " + cardID.substring(2, 4) + ", " + cardID.substring(4, 6) + ", " + cardID.substring(6, 8) + ", ";
        if (!m.find()) {
            m = Pattern.compile("(?s)NXP_CORE_CONF=\\{.+?(33, ?04, ?(?:\\S{3} ?){4}).+?\\}").matcher(nxp_str);
            if (!m.find()) throw new IOException("File not supported");
            nxp_str = new StringBuilder(nxp_str).replace(m.start(1), m.end(1), "33, 04, " + cardID).toString();
        } else {
            nxp_str = new StringBuilder(nxp_str).replace(m.start(1), m.end(1), Integer.toHexString(Integer.parseInt(m.group(1), 16) + 4)).toString();
            nxp_str = new StringBuilder(nxp_str).replace(m.start(2), m.end(2), "33, 04, " + cardID).toString();
        }

        new FileOutputStream(nxp, false).write(nxp_str.getBytes());
    }

    private void writeBackAndRestartNFC() throws IOException, InterruptedException {
        if (!ensureBackup()) throw new IOException("Failed to backup. Stop for security reasons.");
        if (m_activity.getExternalCacheDir() == null)
            throw new IOException("Could not get external cache dir.");
        String path = m_activity.getExternalCacheDir().getAbsolutePath();
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
        DataInputStream inputStream = new DataInputStream(process.getInputStream());
        outputStream.writeBytes("mount -o rw,remount,rw /system\n");
        outputStream.writeBytes("cp -f " + path + "/libnfc-brcm.conf /system/etc\n");
        outputStream.writeBytes("chmod 644 /system/etc/libnfc-brcm.conf\n");
        outputStream.writeBytes("cp -f " + path + "/libnfc-nxp.conf /system/etc\n");
        outputStream.writeBytes("chmod 644 /system/etc/libnfc-nxp.conf\n");
        outputStream.writeBytes("mount -o ro,remount,ro /system\n");
        outputStream.writeBytes("ps | grep -w com.android.nfc\n");
        outputStream.flush();
        Scanner scanner = new Scanner(inputStream);
        scanner.next();
        int PID = scanner.nextInt();
        outputStream.writeBytes("kill " + PID + "\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        process.waitFor();
    }

    private void addTextView(LinearLayout where, String text, Integer color, View.OnClickListener onClickListener) {
        TextView t = new TextView(m_activity);
        t.setText("　　" + text);
        if (color != null)
            t.setTextColor(color);
        if (onClickListener != null) {
            t.setOnClickListener(onClickListener);
            t.setClickable(true);
        }
        where.addView(t);
    }

    LinearLayout mPersistLiner;
    LinearLayout mTempLiner;
    SharedPreferences mSharedPreferences;

    public CardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCardReader = new NFCCardReader(this);

        View v = inflater.inflate(R.layout.fragment_card, container, false);
        mPersistLiner = (LinearLayout) v.findViewById(R.id.info);
        mTempLiner = (LinearLayout) v.findViewById(R.id.liner);
        m_activity = getActivity();
        addTextView(mPersistLiner
                , "如果您的手机具有NFC功能（并处于开启状态），您可以在此读取到您的门禁卡的信息。如果对本程序进行root授权，我们将尝试用您的手机模拟读到的门禁卡（在手机亮屏时，不需打开本界面即可作为门禁卡使用）。"
                , null
                , null);
        addTextView(mPersistLiner
                , "请注意，这是一项测试功能。"
                , null
                , null);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        mTempLiner.removeAllViewsInLayout();
        enableReaderMode();
    }

    @Override
    public void onPause() {
        disableReaderMode();
        super.onPause();
    }

    private void enableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(m_activity);
        if (nfc == null) {
            addTextView(mTempLiner, "NFC Adapter is not found on this device!", 0xFFFF0000, null);
        } else if (!nfc.isEnabled()) {
            addTextView(mTempLiner, "NFC Service is not enabled. Click here to turn it on", 0xFFFF0000, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent("android.settings.NFC_SETTINGS"));
                }
            });
            addTextView(mPersistLiner
                    , "If you are having trouble enabling NFC Service because of this program, click here to restore default configuration"
                    , 0xFF66CCFF
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!ensureBackup()){
                                addTextView(mTempLiner, "No backup files found.", 0xFFFF0000, null);
                                return;
                            }
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        restoreBackup();
                                        m_activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addTextView(mTempLiner, "OK, restored default.", null, null);
                                            }
                                        });

                                    } catch (InterruptedException | IOException e) {
                                        m_activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addTextView(mTempLiner, Log.getStackTraceString(e), 0XFFFF0000, null);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
        } else {
            addTextView(mTempLiner, "NFC works normally. Enabling Reader.", 0xFF000000, null);
            nfc.enableReaderMode(m_activity, mCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(m_activity);
        if (nfc == null || !nfc.isEnabled()) return;
        nfc.disableReaderMode(m_activity);
        Log.e("A","read");
    }

    @Override
    public void onCardIDReady(final String ID) {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTempLiner.addView(new CardTextView(ID, m_activity));
            }
        });
    }

    class CardTextView extends AppCompatTextView {
        public CardTextView(final String ID, Context context) {
            super(context);
            setTextColor(0xFF000000);
            setText("　　Found Card: " + ID + ", click here to emulate(root required).");
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!ensureRoot()){
                        addTextView(mPersistLiner, "Cannot get root permission", 0xFFFF0000, null);
                        return;
                    }
                    addTextView(mTempLiner, "Emulating card " + ID, null, null);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.e("SU","lksdjf+");
                                disableReaderMode();
                                prepareConf();
                                updateConf(ID);
                                writeBackAndRestartNFC();
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addTextView(mPersistLiner, "Succeed. You have to fully restart this program before next scan. To restart, click here.", 0XFFFF2244
                                                , new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent mStartActivity = new Intent(m_activity, WelcomeActivity.class);
                                                        int mPendingIntentId = 123456;
                                                        PendingIntent mPendingIntent = PendingIntent.getActivity(m_activity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                                                        AlarmManager mgr = (AlarmManager)m_activity.getSystemService(Context.ALARM_SERVICE);
                                                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                                        System.exit(0);
                                                    }
                                                });
                                        mTempLiner.removeAllViews();
                                    }
                                });
                            } catch (final IOException | InterruptedException e) {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addTextView(mTempLiner, Log.getStackTraceString(e), 0xFFFF0000, null);
                                        Log.e("A",Log.getStackTraceString(e));
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }
}
