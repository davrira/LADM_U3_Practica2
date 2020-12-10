package mx.tecnm.proyectopivote

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE Agenda(" +
                    "idAgenda INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Lugar VARCHAR (200), " +
                    "Hora VARCHAR (200), " +
                    "Fecha DATETIME, " +
                    "Descripcion VARCHAR(200), " +
                    "Firebase INTEGER, " +
                    "UpdateFirebase INTEGER"+
                    ");"
        )//execSQL

        db.execSQL(
                "CREATE TABLE Borrado(" +
                        "idBorrado INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idAgendaDelete INTEGER"+
                        ");"
        )


    }//onCreate

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }//onUpgrade


}//class