package www.prakash.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.net.URI;
import java.util.HashMap;

public class StudentProvider extends ContentProvider {
    public static String PROVIDER="www.prakash.contentprovider.StudentProvider";
    public static String URL="content://"+PROVIDER+"/student";
    public static Uri CONTENT_URI;
    public static String DATABASE_NAME="student.db";
    public static String TABLE_NAME="student";
    public static String COL_1="ID";
    public static String COL_2="NAME";
    public static String COL_3="GRADE";
   final public static int STUDENT=1;
   final public static int STUDENT_ID=2;
    public static String TABLE_CREATE="CREATE TABLE "+TABLE_NAME+" ("+COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL_2+" TEXT, "+COL_3+" TEXT "+")";
    public static int DATABASE_VERSION=1;
    public static SQLiteDatabase database;
    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    public static UriMatcher uriMatcher;
    static {
        uriMatcher=new UriMatcher(uriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER,"student",STUDENT);
        uriMatcher.addURI(PROVIDER,"student/#",STUDENT_ID);
    }

    static {
        CONTENT_URI = Uri.parse(URL);
    }



    public class DatabseHelper extends SQLiteOpenHelper{

        public DatabseHelper(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        Context context=getContext();
        DatabseHelper db=new DatabseHelper(context);
        database=db.getWritableDatabase();

        return (database==null)? false:true;
    }
    @Override
    public Cursor query( Uri uri,   String[] projection,   String selection,   String[] selectionArgs,   String sortOrder) {

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TABLE_NAME);

            switch (uriMatcher.match(uri)) {
                case STUDENT:
                    qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                    break;

                case STUDENT_ID:
                    qb.appendWhere( COL_1 + "=" + uri.getPathSegments().get(1));
                    break;

                default:
            }

            if (sortOrder == null || sortOrder == ""){
                /**
                 * By default sort on student names
                 */
                sortOrder = COL_2;
            }

            Cursor c = qb.query(database,	projection,	selection,
                    selectionArgs,null, null, sortOrder);
            /**
             * register to watch a content URI for changes
             */
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;

    }

     
    @Override
    public String getType(   Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case STUDENT:
                return "vnd.android.cursor.dir/vnd.example.students";
            /**
             * Get a particular student
             */
            case STUDENT_ID:
                return "vnd.android.cursor.item/vnd.example.students";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

     
    @Override
    public Uri insert(   Uri uri,   ContentValues values) {

        long id=database.insert(TABLE_NAME,null,values);
        if(id>0){
            Uri uri1= ContentUris.withAppendedId(CONTENT_URI,id);
            getContext().getContentResolver().notifyChange(uri,null);
            return uri;
        }
        else {
            throw  new SQLException("Faild to add in row"+uri);
        }

    }

    @Override
    public int delete(   Uri uri,   String selection,   String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case STUDENT:
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = database.delete(TABLE_NAME, COL_1 +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(   Uri uri,   ContentValues values,   String selection,   String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case STUDENT:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            case STUDENT_ID:
                count = database.update(TABLE_NAME, values,
                        COL_1 + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
