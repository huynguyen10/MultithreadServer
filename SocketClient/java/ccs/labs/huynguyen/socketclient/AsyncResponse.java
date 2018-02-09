package ccs.labs.huynguyen.socketclient;

/**
 * Created by huynguyen on 08.02.18.
 */

public interface AsyncResponse {
    void onTaskComplete(int retVal, String result);
}
