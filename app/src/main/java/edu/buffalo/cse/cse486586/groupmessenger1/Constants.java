package edu.buffalo.cse.cse486586.groupmessenger1;

import android.net.Uri;

/**
 * Created by sourabh on 2/20/18.
 * This class contains all constants needed for the program.
 * Idea is to keep constants separate so that there single configuration point for the program that can be changed in a single class
 * rather than changing through out the program.
 */

public class Constants {

    //Starting and Ending port (Other ports are incremented by value of 4)
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT4 = "11124";

    //server port
    static final int SERVER_PORT = 10000;

    //namespace for content provider
    static final String contentProviderURI = "content://edu.buffalo.cse.cse486586.groupmessenger1.provider";

    //Content Uri corresponding to given contentProviderURI
    static final Uri CONTENT_URL = Uri.parse(contentProviderURI);

    //sequence number to keep track of messages (Also, this is also used as a key to be inserted into content value)
    static int SEQUENCE_NUMBER = 0; //sequence number to keep track of messages

    //string constants for key and value
    static final String KEY = "key";
    static final String VALUE = "value";

    //matrix columns used
    static String[] matrixColumns = {KEY, VALUE};


}
