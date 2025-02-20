import java.io.File

// Clase para representar una conexión entre dos paradas con su consumo de gasoil
data class Ruta(val origen: String, val destino: String, val gasoil: Double)

// Clase para manejar el grafo de rutas y aplicar el algoritmo de Kruskal
class Grafo {
    private val rutas = mutableListOf<Ruta>() // Lista para almacenar las rutas
    private val conjuntos = mutableMapOf<String, String>() // Mapa para gestionar los conjuntos disjuntos

    // Método para agregar una nueva ruta al grafo
    fun agregarRuta(origen: String, destino: String, gasoil: Double) {
        rutas.add(Ruta(origen, destino, gasoil)) // Añade una nueva ruta a la lista
    }

    // Encuentra la raíz del conjunto al que pertenece un nodo 
    private fun encontrar(vertice: String): String {
        if (conjuntos[vertice] != vertice) {
            conjuntos[vertice] = encontrar(conjuntos[vertice]!!) // Recursión para encontrar la raíz
        }
        return conjuntos[vertice]!! // Devuelve la raíz del conjunto
    }

    // Une dos conjuntos en la estructura de conjuntos disjuntos
    private fun unir(vertice1: String, vertice2: String) {
        val raiz1 = encontrar(vertice1) // Encuentra la raíz del primer vértice
        val raiz2 = encontrar(vertice2) // Encuentra la raíz del segundo vértice
        if (raiz1 != raiz2) conjuntos[raiz1] = raiz2 // Une los conjuntos si son diferentes
    }

    // Implementación del algoritmo de Kruskal para encontrar la ruta óptima minimizando el gasoil
    fun kruskalMST(): List<Ruta> {
        val mst = mutableListOf<Ruta>() // Lista para almacenar el árbol de expansión mínima
        val rutasOrdenadas = rutas.sortedBy { it.gasoil } // Ordenamos las rutas por menor consumo de gasoil

        // Inicializar conjuntos disjuntos para cada ruta
        for (ruta in rutasOrdenadas) {
            conjuntos[ruta.origen] = ruta.origen // Cada nodo es su propio conjunto
            conjuntos[ruta.destino] = ruta.destino
        }

        // Aplicamos el algoritmo de Kruskal para construir el MST (ruta óptima)
        for (ruta in rutasOrdenadas) {
            if (encontrar(ruta.origen) != encontrar(ruta.destino)) { // Si no están en el mismo conjunto
                mst.add(ruta) // Agrega la ruta al MST
                unir(ruta.origen, ruta.destino) // Une los conjuntos de los nodos
            }
        }

        return mst // Devuelve el árbol de expansión mínima
    }
}

// Carga el archivo de entrada, compatible con formato del profesor y del tipo DestinoA-DestinoB-DestinoC
fun cargarGrafoDesdeArchivo(nombreArchivo: String): Grafo {
    val grafo = Grafo() // Crea una nueva instancia del grafo
    val file = File(nombreArchivo) // Crea un objeto File para el archivo de entrada

    if (!file.exists()) {
        println("No se encontró el archivo '$nombreArchivo'.") // Mensaje de error si el archivo no existe
        return grafo // Devuelve un grafo vacío
    }

    val lineas = file.readLines() // Lee todas las líneas del archivo
    if (lineas.size < 4) {
        println(" Archivo con formato incorrecto.") // Mensaje de error si el formato es incorrecto
        return grafo // Devuelve un grafo vacío
    }

    // Mostrar el contenido del archivo txt antes de procesarlo
    println("-Carga del mapa de rutas de la USB:")
    lineas.forEach { println(it) } // Imprime cada línea del archivo

    var indice = 2  
    var formatoB = false // Variable para detectar el formato del archivo

    // Paso 1: Identificar si el archivo está en formato del profesor o del tipo DestinoA-DestinoB-DestinoC
    while (indice < lineas.size && !lineas[indice].startsWith("Bus")) {
        val datos = lineas[indice].split(" ") 
        if (datos.size != 2 || datos[1].toDoubleOrNull() == null) {
            formatoB = true // Si no sigue el formato profesor, asumimos que es DestinoA-DestinoB-DestinoC
        }
        indice++
    }

    // Paso 2: Leer rutas de gasoil
    while (indice < lineas.size) {
        val datos = lineas[indice].split(" ") // Divide la línea en partes
        if (datos.size < 3) {
             indice++ // Si hay menos de 3 datos, continúa a la siguiente línea
            continue
        }

        val paradas = if (formatoB) datos[1].split("-") else datos.slice(1 until datos.size - 1) // Obtiene las paradas
        val consumo = datos.last().replace("X", "").replace("l", "").toDoubleOrNull() // Obtiene el consumo de gasoil

        if (consumo != null && paradas.size > 1) {
            // Agregar cada conexión entre paradas como una arista en el grafo
            for (i in 0 until paradas.size - 1) {
                grafo.agregarRuta(paradas[i], paradas[i + 1], consumo) // Agrega la ruta al grafo
            }
        }

        indice++
    }

    return grafo // Devuelve el grafo cargado
}

// Muestra la ruta óptima calculada con Kruskal
fun mostrarRutaOptima(mst: List<Ruta>) {
    if (mst.isEmpty()) {
        println("\n No se generó ninguna ruta óptima. Revisa el archivo de entrada.") // Mensaje si no hay rutas
        return
    }

    println("\n-Ruta óptima minimizando el consumo de gasoil:")
    // Solo mostrar la primera ruta del MST
    val primeraRuta = mst.first() // Obtiene la primera ruta del MST
    println("${primeraRuta.origen} --(${primeraRuta.gasoil}L)-- ${primeraRuta.destino}") // Imprime la ruta óptima
}

fun main() {
    val grafo = cargarGrafoDesdeArchivo("rutaOptUSB.txt") // Carga el grafo desde el archivo txt

    val mst = grafo.kruskalMST() // Calcula el árbol de expansión mínima
    mostrarRutaOptima(mst) // Muestra la ruta óptima
}