package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * <p>
 * Please read:
 * <p>
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * <p>
 * before you start to get yourself familiarized with ContentProvider.
 * <p>
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 *
 * @author stevko
 */
public class GroupMessengerProvider extends ContentProvider {


    static final String TAG = GroupMessengerProvider.class.getSimpleName();

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType() is called ####### ");
        // You do not need to implement this.
        return null;
    }

    /* returns uri after inserting content values
    * Uri : Table in the provider
    * ContentValues : used to store a set of values that the ContentResolver can process.
    * */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         *
         *
         * Reference :
         * [0] : https://developer.android.com/training/data-storage/files.html#WriteInternalStorage
         * [1] : https://developer.android.com/reference/android/content/Context.html
         * [2] : https://developer.android.com/reference/android/content/Context.html#openFileOutput(java.lang.String, int)
         * [3] : https://www.mkyong.com/java/how-to-write-to-file-in-java-fileoutputstream-example/
         * [4] : http://www.java2s.com/Code/Android/Core-Class/ContextopenFileOutput.htm
         */

        //If empty value is coming from argument then return
        if (values.size() == 0) {
            Log.v(TAG, "Empty value coming to insert !!");
            return null;
        }

        //storing data in to a file, where key will be filename and value will be message.
        //getting string content value corresponding to "key" and "value"
        String fileName = values.get(Constants.KEY).toString();
        String data = values.get(Constants.VALUE).toString();

        FileOutputStream outputStream = null;
        try {

            //create the file with file name as "key" (the default mode is used, where the created file can only be accessed by the calling application)
            //Reference : [https://stackoverflow.com/questions/25591066/openfileoutput-method-vs-fileoutputstream-constructor]
            //Using openFileOutput because it is specifically used for writing file into internal storage
            outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);

            //write to file
            outputStream.write(data.getBytes()); //writing the data into the file
            outputStream.flush();
            outputStream.close();


        } catch (FileNotFoundException e) {
            Log.v(TAG, "FileNotFoundException in insert");
        } catch (IOException e) {
            Log.v(TAG, "IOException in insert");
        } finally {
            try {
                if (outputStream != null) {
                    //closing output stream incase no exception occurs
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.v(TAG, "IOException in insert");
            }
        }
        return uri;


    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    /*
    *Returns a cursor that provides read access to the result query
    *Uri : Table in the provider
    *projection : array of string which corresponds to the column which is used to retreive each row
    *selection : element of query selection
    *selectionArgs : argument
    *sortOrder : ordering
    * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {


        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         *
         * Algorithm : to build a cursor, using file storage option
         * 1. Create BufferedReader from input selection (seq no.) which can be stored in a file
         * 2. Create  data (string format) from this BufferedReader into a matrix column as a value corresponding to  a selection
         * 3. add data from 2 to matrixRow
         * 4. Return cursor
         * Reference :
         * [0] : https://developer.android.com/training/data-storage/files.html#java
         * [1] : https://developer.android.com/reference/android/database/MatrixCursor.html
         *
         */

        if (selection != null && uri != null) {

            MatrixCursor matrixCursor = new MatrixCursor(Constants.matrixColumns);
            BufferedReader bufferReader = null;

            try {
                //getting file input stream from selection (get context view only current running activity)
                FileInputStream fis = this.getContext().openFileInput(selection);

                //InputStreamReader isr = new InputStreamReader(new FileInputStream(new getContext().openFileInput(selection)));
                InputStreamReader isr = new InputStreamReader(fis);

                //creating buffer reader from inputStreamFile
                bufferReader = new BufferedReader(isr);

                String[] matrixColumnData = {selection, bufferReader.readLine()};

                //add data to the matrix column
                matrixCursor.addRow(matrixColumnData);

                //close bufferedReader after usage
                bufferReader.close();

                //return matrix column
                return matrixCursor;

            } catch (FileNotFoundException e) {
                Log.v(TAG, "File Not Found Exception");
            } catch (IOException e) {
                Log.v(TAG, "IO Exception");
            } finally {
                try {
                    if (bufferReader != null) {
                        bufferReader.close();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "bufferReader IOException");
                }
            }
        }
        return null;

    }
}
