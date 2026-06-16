package utilidades;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * PPT 3.2.1 - Lectura de datos desde archivos Excel (.xlsx) con Apache POI.
 * Convención de filas: fila 0 = cabecera, fila 1 = primer dato.
 */
public class ExcelUtils {

    private static Sheet sheet;

    /**
     * Carga una hoja específica de un archivo Excel.
     *
     * @param rutaArchivo  Ruta relativa al archivo, p.ej. "src/test/resources/testData/dataEmpleados.xlsx"
     * @param nombreHoja   Nombre de la hoja dentro del Excel
     */
    public static void setExcelFileSheet(String rutaArchivo, String nombreHoja) throws IOException {
        FileInputStream fis = new FileInputStream(rutaArchivo);
        Workbook workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(nombreHoja);
        if (sheet == null) {
            throw new IllegalArgumentException(
                "Hoja '" + nombreHoja + "' no encontrada en: " + rutaArchivo);
        }
    }

    /**
     * Obtiene el valor de una celda como String.
     *
     * @param fila  Número de fila (1 = primera fila de datos, saltando cabecera)
     * @param col   Número de columna base-0
     * @return      Valor de la celda como texto, o "" si está vacía
     */
    public static String getCellData(int fila, int col) {
        Row row = sheet.getRow(fila);
        if (row == null) return "";
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return new DataFormatter().formatCellValue(cell).trim();
    }
}
