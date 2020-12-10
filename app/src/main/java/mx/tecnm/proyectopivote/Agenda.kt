package mx.tecnm.proyectopivote

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Agenda(lugarP: String, horaP: String, fechaP: String, descripcionP: String, firebaseP:Int, firebaseUpdateP:Int) {

    var baseRemota = FirebaseFirestore.getInstance()

    var lugar = lugarP
    var hora = horaP
    var fecha = fechaP
    var descripcion = descripcionP
    var id = 0
    var error = -1
    var firebase = firebaseP
    var firebaseUpdate = firebaseUpdateP


    val nombreBase = "citas"
    var puntero : AppCompatActivity ?= null

    fun asignarPuntero(p: AppCompatActivity){
        puntero = p
    }//asignarPuntero


    /*
    Lista errores
    ------------
    1-> Error en tabla, no se creo o no se pudo conectar
    2-> Error al insertar
    3-> No se puedo realizar consulta/tabla vacia
    4-> No se puedo sincronizar con firebase -- error conexion
    5-> No se pudo actualizar (bandera firebase)
    6-> No se pudo actualizar (SQLite - Local)
    7-> No se pudo eliminar (SQLite - Local)
    8-> No se pudo actualizar la bandera updateFirebae
    9-> no se pudo actualizar los datos en firebase
    10-> No se pudo eliminar los datos de firebase
     */

    fun insertar() : Boolean{

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase
            var datos = ContentValues()

            fecha += " 00:00:0000"
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val fechaEvento = formatoFecha.parse(fecha)


            datos.put("Lugar", lugar)
            datos.put("Hora", hora)
            datos.put("Fecha", formatoFecha.format(fechaEvento))
            datos.put("Descripcion", descripcion)
            datos.put("Firebase", firebase)
            datos.put("UpdateFirebase", firebaseUpdate)

            var respuesta = transaccion.insert("Agenda", "idAgenda", datos)

            if (respuesta == -1L){

                error = 2
                return false

            }//

            transaccion.close()

        }catch (e: SQLiteException){

            error = 1
            return false

        }//try-catch


        return true
    }//insertar

    fun mostrarTodos() : ArrayList<Agenda>{

        var datos = ArrayList<Agenda>()
        error = -1

        try {

            val base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.readableDatabase

            var dato = transaccion.query("Agenda", arrayOf("*"), null, null, null, null, null)

            if (dato.moveToFirst()){

                do {

                    var agendaTmp = Agenda(dato.getString(1),
                                           dato.getString(2),
                                           dato.getString(3),
                                           dato.getString(4),
                                           dato.getInt(5),
                                           dato.getInt(6)
                                    )

                    agendaTmp.id = dato.getInt(0)
                    datos.add(agendaTmp)

                }while (dato.moveToNext())

            }else{
                error=3
            }

            transaccion.close()

        }catch (e: SQLiteException){
            error = -1
        }//try-catch

        return datos
    }//mostrarTodos

    fun insertarFirebase() : Boolean{

        error = -1

        try {

            var baseTmp = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = baseTmp.readableDatabase

            var columnas = arrayOf("idAgenda", "Lugar", "Hora", "Fecha","Descripcion")

            var resultado = transaccion.query("Agenda", columnas, "Firebase = 0", null, null, null, null, null)

            if (resultado.moveToFirst()){

                do {

                    var fechaEventoR = resultado.getString(3)
                    val formatoFecha = SimpleDateFormat("yyyy-MM-dd")
                    var fechaEventoF = formatoFecha.parse(fechaEventoR)

                    var datosInsertar = hashMapOf(
                            "Lugar" to resultado.getString(1),
                            "Hora" to resultado.getString(2),
                            "Fecha" to fechaEventoF,
                            "Descripcion" to resultado.getString(4)
                    )//hashMapOf

                    baseRemota.collection("Agenda")
                            .document(resultado.getString(0))
                            .set(datosInsertar)
                            .addOnSuccessListener {

                                try {
                                    actualizarBanderaInsertarFirebase(resultado.getString(0))
                                }catch (e:Exception){

                                }

                            }
                            .addOnFailureListener {

                            }

                }while (resultado.moveToNext())

            }else{

                error = 4
                return false

            }//if-else

            transaccion.close()

        }catch (e:SQLiteException){

            return false

        }//try-catch

        return true

    }//insertarFirebase

    fun actualizarFirebase() : Boolean{

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.readableDatabase
            var columnas = arrayOf("Lugar", "Hora", "Fecha", "Descripcion")

            var resultado = transaccion.query("Agenda", columnas, "UpdateFirebase = 1", null, null, null, null, null)

            if (resultado.moveToFirst()){

                do {

                    var fechaEvento = resultado.getString(3)
                    val formatoFecha = SimpleDateFormat("yyyy-MM-dd")
                    var fechaEventoN = formatoFecha.parse(fechaEvento)

                    var datosUpdate = hashMapOf(
                            "Lugar" to resultado.getString(1),
                            "Hora" to resultado.getString(2),
                            "Fecha" to fechaEventoN,
                            "Descripcion" to resultado.getString(4)
                    )//datosUpdate

                    baseRemota.collection("Agenda")
                            .document(resultado.getString(0))
                            .update(datosUpdate as Map<String, Any>)
                            .addOnFailureListener {

                                try {
                                    actualizarBanderaUpdateFirabase(resultado.getString(0))
                                }catch (e:Exception){

                                }

                            }//Ok
                            .addOnFailureListener {  }//FAIL

                }while (resultado.moveToNext())

            }else{

                error = 9
                return false

            }//if-else

            transaccion.close()

        }catch (e:SQLiteException){

            error = 9
            return false

        }//try-catch

        return true

    }//actualizarFirebase

    fun eliminarFirebase(): Boolean {

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.readableDatabase

            var columas = arrayOf("*")

            var resultado = transaccion.query("Borrado", columas, null,null,null,null,null)

            if (resultado.moveToFirst()){

                do {

                    baseRemota.collection("Agenda")
                            .document(resultado.getString(1))
                            .delete()

                }while (resultado.moveToNext())

            }else{

                error = 10
                return false

            }

            transaccion.close()

        }catch (e:SQLiteException){

            error = 10
            return false

        }

        return true

    }//eliminarFirebase

    fun actualizar(idActualizarEvento: String): Boolean{

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase
            var datos = ContentValues()

            fecha += " 00:00:0000"
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val fechaEvento = formatoFecha.parse(fecha)

            datos.put("Lugar", lugar)
            datos.put("Hora", hora)
            datos.put("Fecha", formatoFecha.format(fechaEvento))
            datos.put("Descripcion", descripcion)
            datos.put("UpdateFirebase", firebaseUpdate)

            var resultado = transaccion.update("Agenda", datos, "idAgenda = ?", arrayOf(idActualizarEvento))

            if (resultado > 0){
                return true
            }else{
                return false
            }

            transaccion.close()

        }catch (e:SQLiteException) {

            error = 6
            return false

        }//try-catch

        return true

    }//actualizar

    fun eliminar(idEliminarEvento : String) : Boolean {

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase

            var resultado = transaccion.delete("Agenda", "idAgenda = ?", arrayOf(idEliminarEvento))

            if (resultado > 0){

                return true

            }else{
                return false
            }

            transaccion.close()

        }catch (e:SQLiteException){

            error = 7
            return false

        }//try-catch

        return true

    }//eliminar

    fun actualizarBanderaInsertarFirebase (idActualizar:String) : Boolean{

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase

            var valores = ContentValues()
            valores.put("Firebase", 1)

            var resultado = transaccion.update("Agenda",valores, "idAgenda = ?", arrayOf(idActualizar))

            if (resultado > 0){
                return true
            }else{
                return false
            }

            transaccion.close()

        }catch (e:SQLiteException){

            error = 5
            return false

        }//try-catch

        return true

    }//actualizarBanderaInsertarFirebase

    fun actualizarBanderaUpdateFirabase(idActualizar: String) : Boolean{

        error = -1

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase

            var valores = ContentValues()
            valores.put("UpdateFirebase", 0)

            var resultado = transaccion.update("Agenda", valores, "idAgenda = ?", arrayOf(idActualizar))

            if (resultado > 0 ){
                return true
            }else{
                return false
            }

            transaccion.close()

        }catch (e:SQLiteException){

            error = 8
            return false

        }//try-catch

        return true

    }//actualizarBanderaUpdateFirabase

    fun insertarEliminados(idEliminado:String) : Boolean {

        try {

            var base = BaseDatos(puntero, nombreBase, null, 1)
            var transaccion = base.writableDatabase
            var datos = ContentValues()

            datos.put("idAgendaDelete", idEliminado.toInt())

            var respuesta = transaccion.insert("Borrado", "idBorrado", datos)

            if (respuesta == -1L){
                return false
            }

            transaccion.close()

        }catch (e:SQLiteException){

            return false

        }

        return true

    }//insetarEliminados

}//class