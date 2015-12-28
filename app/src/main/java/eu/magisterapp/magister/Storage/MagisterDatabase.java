package eu.magisterapp.magister.Storage;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;

import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.Module;
import eu.magisterapp.magisterapi.Utils;

/**
 * Created by max on 11-12-15.
 */
public class MagisterDatabase extends SQLiteOpenHelper
{
    /**
     * Hierin staan subclasses met een represententatie van elke module.
     * Elke subclass (table) bestaat uit de volgende dingen:
     *
     *     - Een Id veld, dit is het Id van elke module.
     *       Door dit op te slaan zorgen we ervoor dat we geen duplicaties krijgen.
     *
     *     - Een 'owner' veld, dit heeft een tag in het formaat school:username oid,
     *       Iets waarmee elke user apart mee kan worden geidentificeerd.
     *       Dit kan uit Sessie.id worden gehaald.
     *
     *       Dit is nodig zodat we mensen hun records op kunnen zoeken dmv hun naam,
     *       en zodat mensen niet dingen van anderen krijgen te zien.
     *
     *     - Enkele belangrijke velden voor de individuele module.
     *       Dit zijn velden zoals Begin, en Einde. Velden waarvan ik denk dat ze
     *       gebruikt worden in het 'where-gedeelte' van een query.
     *
     *     - Een instance veld
     *       Hier staat de geserializede module in.
     *
     */

    public static final int VERSION = 1;

    public static final String DATABASE_NAME = "magisterdb";

    public static class Afspraken
    {
        public static final String TABLE = "afspraken";

        public static final String ID = "Id";
        public static final String OWNER = "owner";
        public static final String START = "Start";
        public static final String EINDE = "Einde";

        public static final String INSTANCE = "instance"; // Dit is het geserializede object.

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + ID + " integer primary key, "
                + OWNER + " text, "
                + START  + " integer, "
                + EINDE  + " integer, "
                + INSTANCE + " BLOB"

                + ")";

        public static final String INSERT_SQL = "INSERT OR REPLACE INTO " + TABLE + " ("
                + ID + ", " + OWNER + ", " + START + ", " + EINDE + ", " + INSTANCE
                + ") VALUES (?, ?, ?, ?, ?)";
    }

    // TODO: fix iets waarmee je elke cijfer collection kan indexen per jaarlaag-owner
    public static class Cijfers
    {
        public static final String TABLE = "cijfers";
        public static final String OWNER = "owner";
        public static final String INSTANCE = "instance";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + OWNER + " text primary key, "
                + INSTANCE + " BLOB"

                + ")";

        public static final String INSERT_SQL = "INSERT OR REPLACE INTO " + TABLE + " ("
                + OWNER + ", " + INSTANCE
                + ") VALUES (?, ?)";

    }

    public static class RecentCijfers
    {
        /**
         * Dit is verneukt. Schoolmaster heeft recente cijfers geen ID gegeven, dus
         * ga ik er vanuit dat docenten niet op precies dezelfde seconde een cijfer
         * invullen, en DatumIngevoerd primary key maken.
         *
         */
        public static final String TABLE = "cijfers_recent";
        public static final String OWNER = "owner";
        public static final String DATUMINGEVOERD = "DatumIngevoerd";
        public static final String INSTANCE = "instance";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + DATUMINGEVOERD + " integer primary key, "
                + OWNER + " text, "
                + INSTANCE + " BLOB"

                + ")";

        public static final String INSERT_SQL = "INSERT OR REPLACE INTO " + TABLE + " ("
                + DATUMINGEVOERD + ", " + OWNER + ", " + INSTANCE
                + ") VALUES (?, ?, ?)";
    }

    public static class Aanmeldingen
    {
        public static final String TABLE = "aanmeldingen";
        public static final String ID = "Id";
        public static final String OWNER = "owner";
        public static final String START = "Start";
        public static final String EINDE = "Einde";

        public static final String INSTANCE = "instance";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + ID + " integer primary key,"
                + OWNER + " text, "
                + START + " integer, "
                + EINDE + " integer, "
                + INSTANCE +  " BLOB"

                + ")";
    }

    public static class Accounts
    {
        public static final String TABLE = "accounts";
        public static final String ID = "Id";
        public static final String OWNER = "owner";
        public static final String INSTANCE = "instance";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + ID + " integer primary key, "
                + OWNER + " text, "
                + INSTANCE + " BLOB"
                + ")";
    }

    public static class Vakken
    {
        public static final String TABLE = "vakken";
        public static final String ID = "Id";
        public static final String OWNER = "owner";
        public static final String INSTANCE = "instance";

        public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE + " ("
                + ID + " integer primary key, "
                + OWNER + " text, "
                + INSTANCE + " BLOB"
                + ")";
    }

    public MagisterDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Aanmeldingen.CREATE_TABLE_SQL);
        db.execSQL(Accounts.CREATE_TABLE_SQL);
        db.execSQL(Afspraken.CREATE_TABLE_SQL);
        db.execSQL(Cijfers.CREATE_TABLE_SQL);
        db.execSQL(RecentCijfers.CREATE_TABLE_SQL);
        db.execSQL(Vakken.CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Nuke de hele db..
        db.execSQL("DROP TABLE IF EXISTS " + Aanmeldingen.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Accounts.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Afspraken.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Cijfers.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + RecentCijfers.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Vakken.TABLE);

        // En maak hem opnieuw.
        onCreate(db);
    }

    private byte[] serialize(Serializable serializable) throws SerializeException
    {
        try
        {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

            objectOutput.writeObject(serializable);
            objectOutput.close();

            return byteOutput.toByteArray();
        }

        catch (IOException e)
        {
            e.printStackTrace();

            throw new SerializeException(e.getMessage());
        }
    }

    private Object deserialize(byte[] serialized) throws SerializeException
    {
        try
        {
            ByteArrayInputStream byteInput = new ByteArrayInputStream(serialized);

            ObjectInputStream objectInput = new ObjectInputStream(byteInput);

            Object object = objectInput.readObject();

            objectInput.close();

            return object;
        }

        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();

            throw new SerializeException(e.getMessage());
        }
    }

    public AfspraakCollection queryAfspraken(String query, String... params) throws IOException
    {
        cleanAfspraken();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT instance FROM " + Afspraken.TABLE + " " + query, params);

        AfspraakCollection collection = new AfspraakCollection();

        while (cursor.moveToNext())
        {
            collection.add((Afspraak) deserialize(cursor.getBlob(cursor.getColumnIndexOrThrow(Afspraken.INSTANCE))));
        }

        cursor.close();

        return collection;
    }

    public void insertAfspraken(String owner, AfspraakCollection afspraken) throws SerializeException
    {
        for (Afspraak afspraak : afspraken)
        {
            insertAfspraak(owner, afspraak);
        }
    }

    public void insertAfspraak(String owner, Afspraak afspraak) throws SerializeException
    {
        SQLiteStatement stmt = getWritableDatabase().compileStatement(Afspraken.INSERT_SQL);

        stmt.bindLong(1, afspraak.Id);
        stmt.bindString(2, owner);
        stmt.bindLong(3, afspraak.Start.getMillis());
        stmt.bindLong(4, afspraak.Einde.getMillis());
        stmt.bindBlob(5, serialize(afspraak));

        stmt.executeInsert();
    }

    public void cleanAfspraken()
    {
        // tyf alles weg wat al afgelopen is.
        SQLiteStatement stmt = getWritableDatabase().compileStatement("DELETE FROM " + Afspraken.TABLE + " WHERE " + Afspraken.EINDE + " < ?");

        stmt.bindLong(1, System.currentTimeMillis());

        stmt.executeUpdateDelete();
    }

    public CijferList queryCijfers(String query, String... params) throws IOException
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT instance FROM " + Cijfers.TABLE + " " + query, params);

        return (CijferList) deserialize(cursor.getBlob(cursor.getColumnIndexOrThrow(Cijfers.INSTANCE)));
    }

    public void insertCijfers(String owner, CijferList cijfers) throws SerializeException
    {
        SQLiteStatement stmt = getWritableDatabase().compileStatement(Cijfers.INSERT_SQL);

        stmt.bindString(1, owner);
        stmt.bindBlob(2, serialize(cijfers));

        stmt.executeInsert();
    }

    public CijferList queryRecentCijfers(String query, String... params) throws IOException
    {
        cleanRecentCijfers();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT instance FROM " + RecentCijfers.TABLE + " " + query, params);

        CijferList cijfers = new CijferList();

        while (cursor.moveToNext())
        {
            cijfers.add((Cijfer) deserialize(cursor.getBlob(cursor.getColumnIndexOrThrow(Cijfers.INSTANCE))));
        }

        cursor.close();

        return cijfers;
    }

    public void insertRecentCijfers(String owner, CijferList cijfers) throws SerializeException
    {
        for (Cijfer cijfer : cijfers)
        {
            insertRecentCijfer(owner, cijfer);
        }
    }

    public void insertRecentCijfer(String owner, Cijfer cijfer) throws SerializeException
    {
        SQLiteStatement stmt = getWritableDatabase().compileStatement(RecentCijfers.INSERT_SQL);

        stmt.bindLong(1, cijfer.DatumIngevoerd.getMillis());
        stmt.bindString(2, owner);
        stmt.bindBlob(3, serialize(cijfer));

        stmt.executeInsert();
    }

    public void cleanRecentCijfers()
    {
        // tyf alles van een week geleden weg.
        SQLiteStatement stmt = getWritableDatabase().compileStatement("DELETE FROM " + RecentCijfers.TABLE
                + " WHERE " + RecentCijfers.DATUMINGEVOERD + " < ?");

        stmt.bindLong(1, Utils.deltaDays(-7).getMillis());

        stmt.executeUpdateDelete();
    }

    public void nuke()
    {
        onUpgrade(getWritableDatabase(), 0, 1);
    }

    public String ms(DateTime time)
    {
        return String.valueOf(time.getMillis());
    }

    public String now()
    {
        return ms(Utils.now());
    }
}
