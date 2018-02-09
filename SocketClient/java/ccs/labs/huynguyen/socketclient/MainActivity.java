package ccs.labs.huynguyen.socketclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    TextView responseTextView;
    EditText editTextPort, editTextMessage;
    Button buttonConnect, buttonDisconnect, buttonClear, buttonSend;
    RadioButton radioLabAddress, radioHomeAddress;

    boolean isConnected = false;
    Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        radioHomeAddress = findViewById(R.id.homeAddressRadio);
        radioLabAddress = findViewById(R.id.labAddressRadio);
        editTextPort = findViewById(R.id.portEditText);
        editTextMessage = findViewById(R.id.messageEditText);
        buttonConnect = findViewById(R.id.connectButton);
        buttonDisconnect = findViewById(R.id.disconnectButton);
        buttonClear = findViewById(R.id.clearButton);
        buttonSend = findViewById(R.id.sendButton);
        responseTextView = findViewById(R.id.responseTextView);
        radioLabAddress.setChecked(true);

        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String serverAddress;
                            if (radioHomeAddress.isChecked()) {
                                serverAddress = radioHomeAddress.getText().toString();
                            } else {
                                serverAddress = radioLabAddress.getText().toString();
                            }
                            try {
                                socket = new Socket(serverAddress,
                                        Integer.parseInt(editTextPort.getText().toString()));
                                new Client(socket, MainActivity.this).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isConnected) {
                    try {
                        socket.close();
                        responseTextView.setText("Socket closed!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isConnected = false;
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                responseTextView.setText("");
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isConnected) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String str = editTextMessage.getText().toString();
                                PrintWriter out = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket.getOutputStream())),
                                        true);
                                out.println(str);
                                new Client(socket, MainActivity.this).execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                socket.close();
                responseTextView.setText("Socket closed!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskComplete(int retVal, String result) {
        if (retVal == -1) {
            responseTextView.setText("Socket closed!");
            isConnected = false;
        } else {
            responseTextView.setText(result);
            isConnected = true;
        }
    }
}
