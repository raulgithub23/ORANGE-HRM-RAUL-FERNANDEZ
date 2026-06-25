package utilidades;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * PPT 3.2.1 - Lectura de datos desde archivos Excel (.xlsx) con Apache POI.
 *
 * Diseño: instancia en lugar de estático para evitar condiciones de carrera
 * cuando dos step-classes distintas cargan hojas diferentes en la misma suite.
 * Convención de filas: fila 0 = cabecera, fila 1 = primer dato.
 *
 * Convención de columnas usada en los Excel de este proyecto: la columna 0
 * es siempre "NroFila" (un número de referencia visual para quien edita el
 * Excel a mano, no se usa en el código), por eso los datos reales empiezan
 * en la columna 1 en adelante.
 *
 * Uso recomendado:
 *   ExcelUtils excel = new ExcelUtils("src/test/resources/testData/dataEmpleados.xlsx", "Empleados");
 *   String nombre = excel.getCellData(1, 1); // fila 1 = primer dato, columna 1 = "Nombre"
 */
public class ExcelUtils {

    private final Sheet sheet;

    /**
     * Carga una hoja específica de un archivo Excel en el momento de construcción.
     * El workbook se abre, se extrae la hoja y se cierra el stream de inmediato
     * para no dejar el archivo bloqueado durante la ejecución del escenario.
     *
     * @param rutaArchivo  Ruta relativa al archivo, p.ej. "src/test/resources/testData/dataEmpleados.xlsx"
     * @param nombreHoja   Nombre de la hoja dentro del Excel
     * @throws IOException              si el archivo no existe o no se puede leer
     * @throws IllegalArgumentException si la hoja no existe dentro del archivo
     */
    public ExcelUtils(String rutaArchivo, String nombreHoja) throws IOException {
        try (FileInputStream fis = new FileInputStream(rutaArchivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet hojaEncontrada = workbook.getSheet(nombreHoja);
            if (hojaEncontrada == null) {
                throw new IllegalArgumentException(
                    "Hoja '" + nombreHoja + "' no encontrada en: " + rutaArchivo);
            }
            // Se copia la referencia a la hoja; el workbook cierra al salir del try-with-resources
            // pero la Sheet sigue siendo accesible porque POI mantiene los datos en memoria
            this.sheet = hojaEncontrada;
        }
    }

    /**
     * Obtiene el valor de una celda como String.
     * Usa DataFormatter para que números, fechas y texto se devuelvan
     * en el mismo formato visual que muestra Excel (p.ej. "01/05/2025" en vez de un long).
     *
     * @param fila  Número de fila (1 = primera fila de datos, saltando la cabecera en fila 0)
     * @param col   Número de columna base-0 (0 = primera columna)
     * @return      Valor de la celda como texto recortado, o "" si la fila/celda no existe
     */
    public String getCellData(int fila, int col) {
        Row row = sheet.getRow(fila);
        if (row == null) return "";
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return new DataFormatter().formatCellValue(cell).trim();
    }
}