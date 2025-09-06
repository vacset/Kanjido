package me.seta.vacset.kanjido.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtil {

    fun createShareableImage(
        context: Context,
        title: String,
        subtitle: String?,
        qrBitmap: Bitmap,
        footer: String? = null
    ): Bitmap {
        // --- Dimensions and Padding ---
        val padding = 60f
        val textLineHeightLarge = 80f // For title
        val textLineHeightSmall = 60f // For subtitle/footer
        val spaceBetweenElements = 30f

        // --- Calculate Bitmap Size ---
        val qrEffectiveHeight = qrBitmap.height.toFloat()
        var calculatedHeight = padding // Top padding

        // Title
        calculatedHeight += textLineHeightLarge
        // Subtitle (if exists)
        if (subtitle != null) {
            calculatedHeight += textLineHeightSmall
        }
        // Space before QR
        calculatedHeight += spaceBetweenElements
        // QR Code
        calculatedHeight += qrEffectiveHeight
        // Space after QR (if footer exists)
        if (footer != null) {
            calculatedHeight += spaceBetweenElements
            calculatedHeight += textLineHeightSmall
        }
        // Bottom padding
        calculatedHeight += padding

        val outputWidth = (qrBitmap.width + 2 * padding).toInt()
        val outputHeight = calculatedHeight.toInt()

        val outputBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        // --- Background ---
        canvas.drawColor(Color.WHITE)

        // --- Paint for Text ---
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            // Consider context.resources.displayMetrics.density for dynamic text sizing
        }

        var currentY = padding

        // --- Draw Title ---
        textPaint.textSize = 70f // Larger for title
        currentY += textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent // Proper way to advance Y for text
        canvas.drawText(title, outputWidth / 2f, currentY, textPaint)

        // --- Draw Subtitle (if exists) ---
        if (subtitle != null) {
            textPaint.textSize = 55f // Slightly smaller for subtitle
            currentY += textLineHeightSmall
            canvas.drawText(subtitle, outputWidth / 2f, currentY, textPaint)
        }

        // --- Draw QR Code ---
        currentY += spaceBetweenElements
        val qrX = (outputWidth - qrBitmap.width) / 2f
        canvas.drawBitmap(qrBitmap, qrX, currentY, null)
        currentY += qrEffectiveHeight

        // --- Draw Footer (if exists) ---
        if (footer != null) {
            currentY += spaceBetweenElements
            textPaint.textSize = 40f // Smaller for footer
            currentY += textLineHeightSmall / 1.5f // Adjust Y for footer baseline
            canvas.drawText(footer, outputWidth / 2f, currentY, textPaint)
        }

        return outputBitmap
    }

    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        shareTitleDialog: String = "Share Page Content"
    ) {
        val imagesDir = File(context.cacheDir, "images")
        try {
            imagesDir.mkdirs() // Create the directory if it doesn't exist
            val file = File(imagesDir, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            // Ensure authority matches AndroidManifest.xml
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, shareTitleDialog))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing image: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
