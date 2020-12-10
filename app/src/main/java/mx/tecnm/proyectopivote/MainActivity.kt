package mx.tecnm.proyectopivote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var idSeleccionado = -1
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarLista()

        btnInsertar.setOnClickListener {

            var lugarTmp = EdTeLugar.text.toString()
            var horaTmp = EdTeHora.text.toString()
            var fechaTmp = EdTeFecha.text.toString()
            var descripcionTmp = EdTeDescripcion.text.toString()
            var firebaseTmp = 0
            var firebaseUpd = 0

            var agenda = Agenda(lugarTmp, horaTmp, fechaTmp, descripcionTmp, firebaseTmp, firebaseUpd)

            agenda.asignarPuntero(this)

            var resultado = agenda.insertar()

            if (resultado){

                limpiarCampos()
                mensaje("Insercion correcta")

            }else{

                when(agenda.error){

                    1->{dialogo("Atencion", "Error con la tabla, no se creo o no se conecta")}
                    2->{dialogo("Atencion", "Error al insertar")}

                }//when

            }//if-else

            cargarLista()

        }//btnInsertar

        btnSincronizar.setOnClickListener{

            try {

                var agenda = Agenda("","","","",0, 0)
                agenda.asignarPuntero(this)

                var resultadoInsertar = agenda.insertarFirebase()
                var resultadoUpdate = agenda.actualizarFirebase()
                var resultadoDelete = agenda.eliminarFirebase()

                try {

                    if (resultadoInsertar){
                        mensaje("Sincronizacion exitosa")
                    }else{
                        dialogo("Atencion", "Error al sincronizar")
                    }

                    if (resultadoUpdate){
                        mensaje("Se actualizaron los registros en firebase")
                    }else{
                        mensaje("No hay datos que actualizar en la nube")
                    }

                    if (resultadoDelete){
                        mensaje("Se eliminaron registros en firebase")
                    }else{
                        mensaje("No hay registros que eliminar en firebase")
                    }

                }catch (e:Exception){
                    dialogo("Error1", e.message.toString())
                }

            }catch (e:Exception){
                //corregir error -- Si actualiza pero muestra "truena"
                dialogo("Error2", e.message.toString() + "\nSI SE ACTUALIZO")
            }

            cargarLista()

        }//btnSincronizar

    }//onCreate

    fun cargarLista(){

        try {

            var conexion = Agenda("","","","", 0, 0)
            conexion.asignarPuntero(this)
            var data = conexion.mostrarTodos()

            if (data.size == 0){
                if (conexion.error==3){
                    dialogo("Atencion", "No se encontraron datos en la tabla, verificar datos")
                }//
                return
            }//if

            var total = data.size-1
            var vector = Array<String>(data.size, {""})
            listaID.clear()

            (0..total).forEach{

                var vAgenda = data[it]
                var item = "Lugar: ${vAgenda.lugar}"+"\n"+
                        "Hora: ${vAgenda.hora}"+"\n"+
                        "Fecha: ${vAgenda.fecha}"+"\n"+
                        "Descripcion: ${vAgenda.descripcion}"

                listaID.add(vAgenda.id.toString())
                vector[it] = item

            }//forEach

            LiViDatos.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vector)

            this.registerForContextMenu(LiViDatos)

            LiViDatos.setOnItemClickListener { parent, view, position, id ->

                idSeleccionado = position
                mensaje("Se ha seleccionado el id ${idSeleccionado}")

            }//itemClickListener

        }catch (e:Exception){
            dialogo("Error", e.message.toString())
        }//try-catch

    }//cargarLista

    fun mensaje(mensajeP:String){
        Toast.makeText(this, mensajeP, Toast.LENGTH_LONG).show()
    }//mensaje

    fun dialogo(tituloP:String, mensajeP:String){

        AlertDialog.Builder(this)
            .setTitle(tituloP)
            .setMessage(mensajeP)
            .setPositiveButton("Ok"){d, i->}
            .show()

    }//dialogo

    fun limpiarCampos(){

        EdTeLugar.setText("")
        EdTeFecha.setText("")
        EdTeDescripcion.setText("")
        EdTeHora.setText("")

    }//limpiarCampos

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        var inflateDB = menuInflater
        inflateDB.inflate(R.menu.menudeleteupdate, menu)

    }//onCreateContextMenu

    override fun onContextItemSelected(item: MenuItem): Boolean {

        try{

            when(item.itemId){

                R.id.itemActualizar->{

                    var ventanaActualizar = Intent(this, MainActivity2::class.java)
                    ventanaActualizar.putExtra("idActualizar", listaID[idSeleccionado])
                    startActivityForResult(ventanaActualizar, 0)

                }//itemAcutalizar

                R.id.itemEliminar->{

                    var idDelete = listaID[idSeleccionado]
                    AlertDialog.Builder(this)
                            .setTitle("Atencion")
                            .setMessage("Desea eliminar los datos del id: ${idDelete}")
                            .setPositiveButton("Ok"){d, i->
                                var agenda = Agenda("","","","",0, 0)
                                agenda.asignarPuntero(this)

                                if(agenda.eliminar(idDelete)){
                                    mensaje("Se elimino con exito")

                                    if (agenda.insertarEliminados(idDelete)){
                                        mensaje("agregado a la tabla de borrados")
                                    }else{
                                        mensaje("Checar")
                                    }

                                    cargarLista()
                                }else{
                                    dialogo("Atencion", "no se pudo eliminar")
                                }

                            }
                            .setNegativeButton("Cancelar"){d,i->}
                            .show()

                }//itemEliminar

        }//when

        }catch(e:Exception){
            dialogo("Atencion", "Seleccione un item primero")
        }
        return true
    }//onContextItem

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        cargarLista()

    }//onAcivityResult

}//class