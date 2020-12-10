package mx.tecnm.proyectopivote

import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main3.*

class MainActivity3 : AppCompatActivity() {

    var base = BaseDatos(this, "citas",null,1)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        cargar()

        insertarElimnar.setOnClickListener {

            try {

            var idEliminar = vtnBorrarEdTeidAgenda.text.toString()

            var agenda = Agenda("","","","",0,0)
            agenda.asignarPuntero(this)

            var resultado = agenda.insertarEliminados(idEliminar)

            if (resultado){
                Toast.makeText(this,"Ok", Toast.LENGTH_LONG).show()
            }else{
                AlertDialog.Builder(this)
                        .setTitle("Atencion")
                        .setMessage("Error")
                        .show()
            }

            }catch (e:Exception){
                AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(e.message.toString())
                        .show()
            }

            cargar()

        }

    }//onCreate

    fun cargar(){

        try {

            var transaccion = base.readableDatabase
            var borrados = ArrayList<String>()

            var respuesta = transaccion.query("Borrado", arrayOf("*"), null, null, null, null, null)

            if (respuesta.moveToFirst()){

                do {

                    var cadena = "col0 - ${respuesta.getString(0)}-- col1 - ${respuesta.getString(1)}"
                    borrados.add(cadena)

                }while (respuesta.moveToNext())

            }else{
                borrados.add("No hay datos")
            }

            LiViBorrados.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, borrados)

            transaccion.close()

        }catch (e:SQLiteException){
            AlertDialog.Builder(this)
                    .setTitle("Atencion")
                    .setMessage(e.message.toString())
                    .show()
        }

    }

}//classs