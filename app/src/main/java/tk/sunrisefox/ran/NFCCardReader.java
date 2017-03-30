package tk.sunrisefox.ran;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.lang.ref.WeakReference;

public class NFCCardReader implements NfcAdapter.ReaderCallback{
    private WeakReference<CardIDCallback> mCallBack;

    public interface CardIDCallback {
        public void onCardIDReady(String ID);
    }

    public NFCCardReader(CardIDCallback callback){
        mCallBack = new WeakReference<>(callback);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        NfcA nfcA = NfcA.get(tag);
        if(nfcA == null) return;
        mCallBack.get().onCardIDReady(ByteArrayToHexString(tag.getId()));
    }

    private static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
