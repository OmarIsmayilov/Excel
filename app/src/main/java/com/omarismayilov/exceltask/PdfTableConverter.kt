package com.omarismayilov.exceltask

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PdfTableConverter<T>(private val context: Context) {

    fun createPdfFileFromExcel(
        data: List<T>,
        columnNames: Array<String>,
        cellProviders: List<(T) -> String>,
        onResult: (result: Boolean) -> Unit,
    ) {
        return try {
            val pdfName = "pdf_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                pdfName
            )
            val pdfStream = FileOutputStream(pdfFile)
            val document = Document()
            val pdfWriter = PdfWriter.getInstance(document, pdfStream)
            pdfWriter.pageEvent = helper

            document.open()
            addTableToPdf(document, data, columnNames, cellProviders)
            document.close()
            pdfStream.close()
            onResult(true)
        } catch (e: Exception) {
            Log.e("PDF_TABLE_CONVERTER", "$e")
            onResult(false)
        }
    }

    private fun addImageToPdf(
        document: Document,
        drawableResId: Int,
        x: Float,
        y: Float,
    ) {
        val imageDrawable = ContextCompat.getDrawable(context, drawableResId)
        val imageBitmap = (imageDrawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageBytes = stream.toByteArray()
        val image = Image.getInstance(imageBytes)
        image.scaleAbsolute(50f, 50f)
        image.setAbsolutePosition(x, y)
        document.add(image)
    }

    private val helper = object : PdfPageEventHelper() {
        override fun onEndPage(writer: PdfWriter?, document: Document?) {
            document?.let {
                addImageToPdf(
                    it,
                    R.drawable.logo,
                    20f,
                    document.pageSize.height - 70f,
                )
                addImageToPdf(
                    it,
                    R.drawable.oba_logo,
                    document.pageSize.width - 70f,
                    document.pageSize.height - 70f,
                )
            }
        }
    }

    private fun addTableToPdf(
        document: Document,
        data: List<T>,
        columnNames: Array<String>,
        cellProviders: List<(T) -> String>,
    ) {
        val columnCount = columnNames.size
        val table = PdfPTable(columnCount)

        for (columnName in columnNames) {
            val cell = PdfPCell(Phrase(columnName))
            table.addCell(cell)
        }
        for (item in data) {
            for (cellProvider in cellProviders) {
                val cell = PdfPCell(Phrase(cellProvider(item)))
                table.addCell(cell)
            }
        }

        document.add(table)
    }

}
