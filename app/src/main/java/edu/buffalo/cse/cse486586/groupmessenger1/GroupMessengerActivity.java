package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.telephony.TelephonyManager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {


    //Static variables ( Referenced from PA1)
    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

         /*
         * Calculate the port number that this AVD listens on. (Referenced from PA1)
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        //Creating Server Socket (Referenced from PA1)
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             * AsyncTask is a simplified thread construct that Android provides.
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

         /* Retrieve a pointer to the input box (EditText) defined in the layout
         * XML file (res/layout/main.xml).
         *
         * This is another example of R class variables. R.id.edit_text refers to the EditText UI
         * element declared in res/layout/main.xml. The id of "edit_text" is given in that file by
         * the use of "android:id="@+id/edit_text""
                */
        final EditText editText = (EditText) findViewById(R.id.editText1);


        final Button sendButton = (Button) findViewById(R.id.button4);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //This displays message in textView (just for debugging easy)
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                editText.setTextColor(Color.RED);
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t >>>>" + ">>>" + msg); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.setTextColor(Color.BLUE);
                remoteTextView.append("\n>>>>>");

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */

                //create client Async task, which will accept messages from other clients
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        //Referred from PA1
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            /*
            * Note : Below code is referenced from PA1
            Algorithm :
            * 0. In order to continue accepting more connections, use infinite while loop
            * 1. Listen for a connection to be made to the socket coming  as a param in AsyncTask and accepts it. [ Reference : https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html]
            * 2. Create InputStream form incoming socket
            * 3. To send message to UI thread, call onProgressUpdate with bufferReader.readLine() string value (which runs on UI thread as a result of calling this function)
            * */
            try {

                //this is done to keep reading multiple messages (although grader gives 5 points without this, but is a good practice for a socket to accept client's connection infinitely)
                //at least one time send & receive message
                do {
                    //server is ready to accept data starting
                    Socket socket = serverSocket.accept();

                    //Basic Stream flow in Java : InputStream -> InputStreamReader -> BufferReader -> Java Program [ Reference : https://www.youtube.com/watch?v=mq-f7zPZ7b8  ; https://www.youtube.com/watch?v=BSyTJSbNPdc]
                    //taking input from socket as a stream
                    InputStream inputStreamFromSocket = socket.getInputStream();

                    //creating buffer reader from inputStreamFromSocket (combining InputStreamReader -> BufferReader flow in one statement)
                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStreamFromSocket));

                    //This is invoked in doBackground() to send message to UI thread to call onProgressUpdate (which runs on UI thread as a result of this function calling)
                    //publishing progress with bufferReader.readline() - which returns a line of String which has been read by bufferReader
                    publishProgress(bufferReader.readLine());

                } while (true);
            } catch (IOException e) {
                Log.e(TAG, "Message receive exception");
            }

            return null;
        }

        protected void onProgressUpdate(String... strings) {
            /*
            * This runs on UI thread as a result of publish progress
            * Algorithm :
            * 1. Receive string from argument's first index (i.e strings[0]) and trim it
            * 2. Initialize new content value
            * 3. Fetch Key from sequence number from Constants class (This is used to put key in content value from step 2)
            * 4. Put content value as received string from step 1
            * 5. Initialize content resolver helps to Inserts a row into a table at the given Uri (which is also fetched from Constants class)
            * 6. Insert values to content resolver
            * 7. Increment sequence number for next message to receive
            * Reference :
            * [1] :https://developer.android.com/reference/android/content/ContentResolver.html
            * [2] :https://www.youtube.com/watch?v=IWP2-qkhtiM
            * [3] :http://codetheory.in/android-sharing-application-data-with-content-provider-and-content-resolver/
            * [4] :https://stuff.mit.edu/afs/sipb/project/android/docs/guide/topics/providers/content-provider-basics.html
            * */

            //Receive string from argument's first index (i.e strings[0]) and trim it
            String strReceived = strings[0].trim();


            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");


            //Initialize new content value
            ContentValues keyValueToInsert = new ContentValues();

            //Fetch Key from sequence number from Constants class (This is used to put key in content value from step 2)
            String stringKeySeq = Integer.toString(Constants.SEQUENCE_NUMBER);

            //Put key into content value
            keyValueToInsert.put(Constants.KEY, stringKeySeq);

            //Put content value as received string from step 1
            keyValueToInsert.put(Constants.VALUE, strReceived);

            //Initialize content resolver helps to Inserts a row into a table at the given Uri (which is also fetched from Constants class)
            ContentResolver contentResolver = getContentResolver();

            //Insert values to content resolver
            contentResolver.insert(Constants.CONTENT_URL, keyValueToInsert);

            //Increment sequence number for next message to receive
            Constants.SEQUENCE_NUMBER++;

            return;

        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                /*
                 * Objective : message communication to other clients including self
                 * Algorithm :
                 * 1. Iterate from starting Port until ending Port
                 * 2. Create socket for each port
                 * 3. Create a output stream from the socket coming as a param in AsyncTask
                 * 4. Write incoming socketStream from 1 to a bufferedWriter (Intermediate step of moving outputStream to bufferedWriter is done in BufferedWriter constructor)
                 * 5. Flush and close Buffered writer
                 * 6. Close socket
                 * Reference : [https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html]
                 *           : [https://www.youtube.com/watch?v=mq-f7zPZ7b8]
                 */

                //starting port corresponds to AVD 0 [NOTE: It is very important because test script checks for the same port]
                int startingPort = Integer.parseInt(Constants.REMOTE_PORT0);

                //ending port corresponds to AVD 4  [NOTE: It is very important because test script checks for the same port]
                int endingPort = Integer.parseInt(Constants.REMOTE_PORT4);

                //looping through all ports creating socket for all clients and send messages to all including self
                int currentPort = startingPort;

                //terminate loop when current port reaches ending port (when all ports are exhausted)
                while (currentPort <= endingPort) {

                    //Note : Below code is referenced from PA1

                    //message to be sent to other clients
                    String msgToSend = msgs[0];

                    //creating socket corresponding to current AVD port
                    Socket currentSocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), currentPort);

                    //create a output stream from the socket coming as a param in AsyncTask
                    OutputStream outputStream = currentSocket.getOutputStream();

                    //flowing of bytes is done is following manner :  outputstream -> OutputStreamWriter -> BufferWriter -> program
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                    //write message to buffered writer
                    bufferedWriter.write(msgToSend);

                    //flush & close buffered writer
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    //close the current socket
                    currentSocket.close();

                    //Incrementing by 4 because subsequent AVDs have port difference of 4
                    currentPort = currentPort + 4;
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Client Task Unknown Host Exception");
            } catch (IOException e) {
                Log.e(TAG, "Client Task Socket IOException");
            }

            return null;
        }
    }
}


