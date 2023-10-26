package com.omarismayilov.exceltask

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.Barcode128
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPRow
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfPTableEventAfterSplit
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class PdfTableConverter<T>(
    private val context: Context,
    private val pdfName: String,
    private val pdfDirectory: File,
) {
    private val document = Document()
    private lateinit var pdfFile: File
    private lateinit var pdfStream: FileOutputStream
    private lateinit var pdfWriter: PdfWriter

    init {
        createPdf()
    }

    private fun createPdf() {
        try {
            pdfFile = File(pdfDirectory, "$pdfName.pdf")
            pdfStream = FileOutputStream(pdfFile)
            pdfWriter = PdfWriter.getInstance(document, pdfStream)
            document.open()
        } catch (e: Exception) {
            Log.e("PDF_TABLE_CONVERTER", "$e")
        }

    }


    fun addPrimaryLogo(drawableResId: Int, size: FloatArray? = null) {
        val imageDrawable = ContextCompat.getDrawable(context, drawableResId)
        val imageBitmap = (imageDrawable as BitmapDrawable).bitmap

        var imageWidth = 55f
        var imageHeight = 55f

        size?.let {
            imageWidth = size[0]
            imageHeight = size[1]
        }

        val docHeight = document.pageSize.height
        val paddingTop = 40f

        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageBytes = stream.toByteArray()
        val image = Image.getInstance(imageBytes)

        image.scaleAbsolute(imageWidth, imageHeight)

        val x = (document.pageSize.width - imageWidth) / 2
        val y = docHeight - imageHeight - paddingTop

        image.setAbsolutePosition(x, y)
        document.add(image)

    }

    fun addCompanyName(name: String) {
        val companyName = Paragraph(name)
        document.add(companyName)
    }

    fun addInfoTable(infoMap: Map<String, String>) {
        val table = PdfPTable(4)
        val maxLengthForColspan = 20

        val event = BorderEvent()
        table.tableEvent = event
        table.widthPercentage = 100f
        table.defaultCell.border = Rectangle.NO_BORDER
        table.isSplitLate = false

        table.spacingBefore = 60f

        for ((key, value) in infoMap) {
            val keyCell = PdfPCell(Phrase(key))
            keyCell.border = PdfPCell.BOTTOM
            keyCell.setPadding(5f)
            keyCell.horizontalAlignment = PdfPCell.ALIGN_RIGHT
            keyCell.minimumHeight = 10f

            val valueCell = PdfPCell(Phrase(":  $value"))
            valueCell.border = PdfPCell.BOTTOM
            valueCell.minimumHeight = 20f

            if (value.length > maxLengthForColspan) {
                valueCell.colspan = 3
            }


            table.addCell(keyCell)
            table.addCell(valueCell)
        }

        document.add(table)
    }


    internal class BorderEvent : PdfPTableEventAfterSplit {
        protected var bottom = true
        protected var top = true

        override fun splitTable(table: PdfPTable) {
            bottom = false
        }

        override fun afterSplitTable(table: PdfPTable, startRow: PdfPRow, startIdx: Int) {
            top = false
        }

        override fun tableLayout(
            table: PdfPTable, width: Array<FloatArray>, height: FloatArray,
            headerRows: Int, rowStart: Int, canvas: Array<PdfContentByte>,
        ) {
            val widths = width[0]
            val y1 = height[0]
            val y2 = height[height.size - 1]
            val x1 = widths[0]
            val x2 = widths[widths.size - 1]
            val cb = canvas[PdfPTable.LINECANVAS]
            cb.moveTo(x1, y1)
            cb.lineTo(x1, y2)
            cb.moveTo(x2, y1)
            cb.lineTo(x2, y2)
            if (top) {
                cb.moveTo(x1, y1)
                cb.lineTo(x2, y1)
            }
            if (bottom) {
                cb.moveTo(x1, y2)
                cb.lineTo(x2, y2)
            }
            cb.stroke()
            cb.resetRGBColorStroke()
            bottom = true
            top = true
        }
    }


    fun addAdditionalLogo(logoResId: Int, size: FloatArray? = null) {
        val logoDrawable = ContextCompat.getDrawable(context, logoResId)
        val logoBitmap = (logoDrawable as BitmapDrawable).bitmap

        var imageWidth = 55f
        var imageHeight = 55f

        size?.let {
            imageWidth = size[0]
            imageHeight = size[1]
        }

        val pageWidth = document.pageSize.width
        val pageHeight = document.pageSize.height

        val stream2 = ByteArrayOutputStream()
        logoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream2)
        val imageBytes2 = stream2.toByteArray()
        val image2 = Image.getInstance(imageBytes2)

        image2.scaleAbsolute(imageWidth, imageHeight)

        val x2 = pageWidth - imageWidth - 30f
        val y2 = pageHeight - imageHeight - 40f

        image2.setAbsolutePosition(x2, y2)
        document.add(image2)
    }


    fun addBarcode(code: String) {
        val barcode128 = Barcode128()
        val barcodeImage = barcode128.createImageWithBarcode(pdfWriter.directContent, null, null)
        val barcodeY = document.pageSize.height - barcodeImage.height * 2 - 35f

        barcode128.code = code
        barcode128.codeType = Barcode128.CODE128

        barcodeImage.setAbsolutePosition(50f, barcodeY)
        barcodeImage.scalePercent(150f, 100f)
        document.add(barcodeImage)

    }

    fun addBarcode(barcodeDrawableResId: Int) {
        val barcodeImageDrawable = ContextCompat.getDrawable(context, barcodeDrawableResId)
        val barcodeImageBitmap = (barcodeImageDrawable as BitmapDrawable).bitmap
        val barcodeImageWidth = 150f
        val barcodeImageHeight = 50f
        val barcodeStream = ByteArrayOutputStream()
        barcodeImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, barcodeStream)
        val barcodeImageBytes = barcodeStream.toByteArray()
        val barcodeImage = Image.getInstance(barcodeImageBytes)

        val barcodeY = document.pageSize.height - barcodeImageHeight * 2
        barcodeImage.scaleAbsolute(barcodeImageWidth, barcodeImageHeight)
        barcodeImage.setAbsolutePosition(25f, barcodeY)
        document.add(barcodeImage)

    }


    fun addDataTable(
        data: List<T>,
        columnNames: Array<String>,
        cellProviders: List<(T) -> String>,
    ) {
        val columnCount = columnNames.size
        val table = PdfPTable(columnCount)

        table.spacingBefore = 40f

        columnNames.forEach { columnName ->
            val cell = PdfPCell(Phrase(columnName))
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            table.addCell(cell)
        }


        data.forEach { item ->
            cellProviders.forEach { cellProvider ->
                val cell = PdfPCell(Phrase(cellProvider(item)))
                table.addCell(cell)
            }
        }

        document.add(table)
    }

    fun closePdf() {
        document.close()
        pdfStream.close()
        pdfWriter.close()
    }

}
