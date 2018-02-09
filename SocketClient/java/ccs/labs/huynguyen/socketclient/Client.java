package ccs.labs.huynguyen.socketclient;

/**
 * Created by huynguyen on 05.02.18.
 */

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends AsyncTask<Void, Void, Integer> {

    private String   response = "";
    private Socket   socket;
    private String   serverAddress;
    private int      serverPort;

    private AsyncResponse callback = null;

    Client(Socket socket, AsyncResponse cb) {
        this.socket = socket;
        this.callback = cb;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
        int bytesRead = -1;
        try {
            byte[] buffer = new byte[1024];

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            InputStream inputStream = socket.getInputStream();

			/*
             * notice: inputStream.read() will block if no data return
			 */
            bytesRead = inputStream.read(buffer, 0, 1024);
            if (bytesRead > 0){
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response = byteArrayOutputStream.toString("UTF-8");
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        }
        return bytesRead;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (callback != null) {
            callback.onTaskComplete(result, response);
        }
    }
}
