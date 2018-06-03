package org.iesborjamoll.amorenovalls.pang2dcontroller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andretietz.android.controller.ActionView;
import com.andretietz.android.controller.DirectionView;
import com.andretietz.android.controller.InputView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControllerActivity extends AppCompatActivity {
    private Socket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        final EditText editTextWithIPAddress = findViewById(R.id.editText);
        Button b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            clientSocket = new Socket(editTextWithIPAddress.getText().toString(), 1024);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
            }
        });

        ActionView actionView = findViewById(R.id.viewAction);
        actionView.setOnButtonListener(new InputView.InputEventListener() {
            @Override
            public void onInputEvent(View view, int buttons) {
                for (int i = 0; i < 4; i++) {
                    // if the bit on position i is set
                    if (((0x01 << i) & buttons) > 0) {
                        try {
                            new NetworkHandlerTask(clientSocket, "SHOOT").execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // if buttons == 0, the user stopped touching the view
            }
        });

        DirectionView directionView = findViewById(R.id.viewDirection);
        directionView.setOnButtonListener(new InputView.InputEventListener() {
            @Override
            public void onInputEvent(View view, int buttons) {
                switch (buttons & 0xff) {
                    case DirectionView.DIRECTION_LEFT:
                        try {
                            new NetworkHandlerTask(clientSocket, "DIRECTION_LEFT").execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case DirectionView.DIRECTION_RIGHT:
                        try {
                            new NetworkHandlerTask(clientSocket, "DIRECTION_RIGHT").execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                // if buttons == 0, the user stopped touching the view
            }
        });
    }

    private static class NetworkHandlerTask extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private String message;

        NetworkHandlerTask(Socket socket, String message) throws IOException {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                dataOutputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
