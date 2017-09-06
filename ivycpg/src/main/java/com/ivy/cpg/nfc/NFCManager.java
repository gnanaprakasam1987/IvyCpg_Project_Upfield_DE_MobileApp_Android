package com.ivy.cpg.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import com.ivy.cpg.nfc.NFCWriteException.NFCErrorType;
import com.ivy.sd.png.util.Commons;

import java.io.IOException;

public class NFCManager {

    private NfcAdapter nfcAdapter;
    private final Activity activity;
    private PendingIntent pendingIntent;

    private TagReadListener onTagReadListener;

    private String writeText = null;

    public static final int NFC_REQUEST_CODE = 1001;
    public static final int NFC_CODE_MATCHED = 1;
    public static final int NFC_CODE_SELECTING_REASON = 2;

    public NFCManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Sets the listener to read events
     */
    public void setOnTagReadListener(TagReadListener onTagReadListener) {
        this.onTagReadListener = onTagReadListener;
    }

    /**
     * To be executed on OnCreate of the activity
     * @return true if the device has nfc capabilities
     */
    public boolean onActivityCreate() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        return nfcAdapter!=null;
    }

    /**
     * To be executed on onResume of the activity
     */
    public void onActivityResume() {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        }
    }

    /**
     * To be executed on onPause of the activity
     */
    public void onActivityPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    /**
     * To be executed on onNewIntent of activity
     * @param intent
     */
    public void onActivityNewIntent(Intent intent) {
        if (writeText == null)
            readTagFromIntent(intent);
        else {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            try {
                writeTag(tag, writeText);
            } catch (NFCWriteException exception) {
                Commons.printException(""+exception);
            } finally {
                writeText = null;
            }
        }
    }

    /**
     * Reads a tag for a given intent and notifies listeners
     * @param intent
     */
    private void readTagFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            //Read ID
            onTagReadListener.onTagRead(byteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
        }
    }

    private String byteArrayToHexString(byte [] inarray) {
        int i;
        int j;
        int in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * Writes a text to a tag
     * @param tag
     * @param data
     * @throws NFCWriteException
     */
    private void writeTag(Tag tag, String data) throws NFCWriteException {
        // Record with actual data we care about
        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, data.getBytes());


        // Complete NDEF message with both records
        NdefMessage message = new NdefMessage(new NdefRecord[] { relayRecord });

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            // If the tag is already formatted, just write the message to it
            try {
                ndef.connect();
            } catch (IOException e) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.unknownError);
            }
            // Make sure the tag is writable
            if (!ndef.isWritable()) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.ReadOnly);
            }

            // Check if there's enough space on the tag for the message
            int size = message.toByteArray().length;
            if (ndef.getMaxSize() < size) {
                throw new NFCWriteException(NFCErrorType.NoEnoughSpace);
            }

            try {
                // Write the data to the tag
                ndef.writeNdefMessage(message);
            } catch (TagLostException tle) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.tagLost, tle);
            } catch (IOException |FormatException fe) {
                throw new NFCWriteException(NFCErrorType.formattingError, fe);// nfcFormattingErrorTitle
            }
        } else {
            // If the tag is not formatted, format it with the message
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                try {
                    format.connect();
                    format.format(message);
                } catch (TagLostException tle) {
                    throw new NFCWriteException(NFCErrorType.tagLost, tle);
                } catch (IOException  | FormatException fe) {
                    throw new NFCWriteException(NFCErrorType.formattingError, fe);
                }
            } else {
                throw new NFCWriteException(NFCErrorType.noNdefError);
            }
        }

    }

    public interface TagReadListener {
         void onTagRead(String tagRead);
    }

}
