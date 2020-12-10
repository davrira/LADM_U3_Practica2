package mx.tecnm.proyectopivote

import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {

    val nombreBase = "citas"
    var baseDatos = BaseDatos(this, nombreBase, null, 1)
    var idActualizar = ""
    var fireBaseInsert = 0
    var firebaseUpdate = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extra = intent.extras
        idActualizar = extra!!.getString("idActualizar")!!

        VtnTeViIdAgenda.setText("Modificando ${idActualizar}")

        //cargarDatos
        try {

            var base = baseDatos.readableDatabase
            var respuesta = base.query("Agenda", arrayOf("*"),"idAgenda = ?", arrayOf(idActualizar),null,null,null)

            if (respuesta.moveToFirst()){

                VtnEditarEdTeLugar.setText(respuesta.getString(1))
                VtnEditarEdTeHora.setText(respuesta.getString(2))
                VtnEditarEdTeFecha.setText(respuesta.getString(3))
                VtnEditarEdTeDescripcion.setText(respuesta.getString(4))

                fireBaseInsert = respuesta.getInt(5)

            }else{
                dialogo("Atencion", "No se encontro el id")
            }

            base.close()

        }catch (e:SQLiteException){
            dialogo("Atencion", e.message.toString())
        }//try-catch
        //cargarDatos

        VtnEditarbtnActualizar.setOnClickListener {

            var lugarUpdate = VtnEditarEdTeLugar.text.toString()
            var horaUpdate = VtnEditarEdTeHora.text.toString()
            var fechaUpdate = VtnEditarEdTeFecha.text.toString()
            var descripcionUpdate = VtnEditarEdTeDescripcion.text.toString()

            var agenda = Agenda(lugarUpdate, horaUpdate, fechaUpdate, descripcionUpdate, fireBaseInsert, firebaseUpdate)

            agenda.asignarPuntero(this)

            var resultado = agenda.actualizar(idActualizar)

            if (resultado){
                mensaje("Actualizacion exitosa")
                finish()
            }else{

                when(agenda.error){
                    1->{dialogo("Atencion", "Error con la tabla, no se creo o no se conecta")}
                    6->{dialogo("Atencion", "Error al actualizar")}
                }//when

            }//else

        }//btnActualizar

        VtnEditarbtnSalir.setOnClickListener {
            finish()
        }//btnSalir

    }//onCreate


    fun dialogo(titulo:String, Mensaje:String){

        AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(Mensaje)
                .setPositiveButton("Ok"){d, i->d.dismiss()}
                .show()

    }//dialogo

    fun mensaje(mensaje:String){
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }//mensaje

}//class