package com.ronyehezkel.wantedapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import java.io.IOException


class Printer {

    @Throws(IOException::class)
    fun drawCenteredImage(
        backgroundImage: Bitmap,
        overlayImage: Bitmap,
        sizeFactor:Float,
        level: Int
    ) {
        val canvas = Canvas(backgroundImage)
        val bounds = Rect()
        val boundsF = RectF()

        val faceHeight = overlayImage.height * sizeFactor
        val faceWidth = overlayImage.width * sizeFactor
        val x = (backgroundImage.width - faceWidth) / 2.toFloat()
        val y = (backgroundImage.height * level) / 100.toFloat()

        boundsF.set(x, y, x + faceWidth, y + faceHeight)
        boundsF.round(bounds)

        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Converts the image to grayscale
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        // Draw the overlay image with the black and white filter
        canvas.drawBitmap(overlayImage, null, bounds, paint)
    }

    @Throws(IOException::class)
    fun drawCenteredString(
        context: Context,
        bitmap: Bitmap,
        text: String,
        fontSize: Float,
        level: Int
    ) {
        val typeface = ResourcesCompat.getFont(context, R.font.bleeding)
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = fontSize
            typeface?.let { setTypeface(it) }
            textAlign = Paint.Align.CENTER
        }

        val canvas = Canvas(bitmap)
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val x = bitmap.width / 2
        val y = (bitmap.height * level) / 100 + bounds.height() / 2

        canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
    }

    fun print(bitmap: Bitmap, context: Context, imageView: ImageView) {
        val image = (getDrawable(context, R.drawable.wanted_org) as BitmapDrawable).bitmap
        image?.let {
            val mutableBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            drawCenteredString(context, mutableBitmap, "Dead or Live", 350f, 28)
            drawCenteredImage(mutableBitmap, bitmap, 1.3f, 32)
            drawCenteredString(context, mutableBitmap, "Ben-Dod", 450f, 67)
//            drawCenteredString(context, mutableBitmap, "For", 300f, 78)
            drawCenteredString(context, mutableBitmap, "Haamast Geveret metunefet in a middle of a sunny day", 220f, 75)
            drawCenteredString(context, mutableBitmap, "1,000,000$ reward", 300f, 88)

            val outputStream = context.openFileOutput("test.png", Context.MODE_PRIVATE)
            mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            imageView.setImageBitmap(mutableBitmap)

        }

    }

}