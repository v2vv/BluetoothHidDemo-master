package de.dbeppler.demo;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import de.dbeppler.demo.bluetooth.HidDataSender;
import de.dbeppler.demo.input.KeyboardHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothHidDemo";
    private static final String TARGET_DEVICE_NAME = "IPC6308"; // insert target device here

    private HidDataSender hidDataSender;
    private KeyboardHelper keyboardHelper;

    private final HidDataSender.ProfileListener profileListener =
            new HidDataSender.ProfileListener() {
                @Override
                @MainThread
                public void onDeviceStateChanged(BluetoothDevice device, int state) {
                    // 0 = disconnected, 1 = connecting, 2 = connected
                    Log.d(TAG, "device state changed to " + state);
                }

                @Override
                @MainThread
                public void onAppUnregistered() {
                    Log.v(TAG, "app unregistered");
                }

                @Override
                @MainThread
                public void onServiceStateChanged(BluetoothProfile proxy) {
                    Log.v(TAG, "service state changed to" + proxy.toString());
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hidDataSender = HidDataSender.getInstance();
        hidDataSender.register(getApplicationContext(), profileListener);

        keyboardHelper = new KeyboardHelper(hidDataSender);
    }

    public void sendMessage(View view) {
        String message = "TestMessage";

        if (hidDataSender.isConnected()) {
            Log.d(TAG, "Sending message: " + message);
            sendString(message);
        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices())
                if (TARGET_DEVICE_NAME.equals(device.getName())) {
                    Log.d(TAG, "Requesting connection to " + device.getName());
                    hidDataSender.requestConnect(device);
                }
        }
    }

    /*
     * helper method to send multiple characters
     */
    private void sendString(String string) {
        if (keyboardHelper != null)
            for (char c : string.toCharArray())
                keyboardHelper.sendChar(c);
    }
}
