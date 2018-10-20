package com.example.marius.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 12;
    private TextView out;
    private BluetoothAdapter adapter;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;


    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your server's MAC address
    private static String address = "58:FB:84:38:A6:E3";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        out = (TextView) findViewById(R.id.adresas);
        setBluetoothData();

        if (Connections.blueTooth()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        out.setText("");
        setBluetoothData();
    }

    private void setBluetoothData() {

        // Getting the Bluetooth adapter
        adapter = BluetoothAdapter.getDefaultAdapter();
        out.append("\nAdapter: " + adapter.toString() + "\n\nName: " + adapter.getName() + "\nAddress: " + adapter.getAddress());

        // Check for Bluetooth support in the first place
        // Emulator doesn't support Bluetooth and will return null

        if (adapter == null) {
            Toast.makeText(this, "Bluetooth NOT supported. Aborting.",
                    Toast.LENGTH_LONG).show();
        }

        // Starting the device discovery
        out.append("\n\nStarting discovery...");
        adapter.startDiscovery();

        out.append("\nDone with discovery...\n");

        // Listing paired devices
        out.append("\nDevices Paired:");
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        for (BluetoothDevice device : devices) {

            out.append("\nFound device: " + device.getName() + " Add: " + device.getAddress());
        }

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        BluetoothDevice bluetoothDevice;
        try {

            bluetoothDevice = adapter.getRemoteDevice(address);

            btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {

            e.printStackTrace();
            // AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }
        adapter.cancelDiscovery();

        try {
            btSocket.connect();
            out.append("\n...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
                //AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }

        }
        out.append("\n...Sending message to server...");
        String message = "Hello from Android.\n";
        out.append("\n\n...The message that we will send to the server is: "+message);

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            //AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
            e.printStackTrace();
            //AlertBox("Fatal Error", msg);
        }



    }
}