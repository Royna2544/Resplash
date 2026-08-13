package com.b_lam.resplash.util.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * Blurs a bitmap by downsampling it and running a stack blur over the result.
 *
 * This replaces `jp.wasabeef:glide-transformations`, whose blur is backed by RenderScript. The
 * RenderScript APIs are deprecated since Android 12 and the support mode was removed from the
 * Android Gradle Plugin, so the blur is done in plain Kotlin instead. Downsampling first keeps it
 * cheap: the images are only used as decorative banners behind other content.
 */
class BlurTransformation(
    private val radius: Int = DEFAULT_RADIUS,
    private val sampling: Int = DEFAULT_SAMPLING
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val scaledWidth = (toTransform.width / sampling).coerceAtLeast(1)
        val scaledHeight = (toTransform.height / sampling).coerceAtLeast(1)

        val bitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawBitmap(
            toTransform,
            Rect(0, 0, toTransform.width, toTransform.height),
            Rect(0, 0, scaledWidth, scaledHeight),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        )

        return stackBlur(bitmap, radius)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("$ID($radius,$sampling)".toByteArray(Key.CHARSET))
    }

    override fun equals(other: Any?) =
        other is BlurTransformation && other.radius == radius && other.sampling == sampling

    override fun hashCode() = ID.hashCode() + radius * 1000 + sampling * 10

    companion object {

        const val DEFAULT_RADIUS = 25
        const val DEFAULT_SAMPLING = 4

        private const val ID = "com.b_lam.resplash.util.glide.BlurTransformation"
    }
}

/**
 * Stack blur by Mario Klingemann, ported to Kotlin. It approximates a Gaussian blur in linear time
 * with respect to the radius, which is what makes it usable on the main thread's worker pool.
 */
private fun stackBlur(bitmap: Bitmap, radius: Int): Bitmap {
    if (radius < 1) return bitmap

    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val wm = width - 1
    val hm = height - 1
    val div = radius + radius + 1

    val r = IntArray(width * height)
    val g = IntArray(width * height)
    val b = IntArray(width * height)
    val vmin = IntArray(maxOf(width, height))

    var divsum = (div + 1) shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    for (i in 0 until 256 * divsum) {
        dv[i] = i / divsum
    }

    val stack = Array(div) { IntArray(3) }
    val r1 = radius + 1

    var yw = 0
    var yi = 0
    for (y in 0 until height) {
        var rsum = 0
        var gsum = 0
        var bsum = 0
        var routsum = 0
        var goutsum = 0
        var boutsum = 0
        var rinsum = 0
        var ginsum = 0
        var binsum = 0
        for (i in -radius..radius) {
            val p = pixels[yi + minOf(wm, maxOf(i, 0))]
            val sir = stack[i + radius]
            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = p and 0x0000ff
            val rbs = r1 - kotlin.math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
        }
        var stackpointer = radius

        for (x in 0 until width) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            var stackstart = stackpointer - radius + div
            var sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (y == 0) {
                vmin[x] = minOf(x + radius + 1, wm)
            }
            val p = pixels[yw + vmin[x]]

            sir[0] = (p and 0xff0000) shr 16
            sir[1] = (p and 0x00ff00) shr 8
            sir[2] = p and 0x0000ff

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi++
        }
        yw += width
    }

    for (x in 0 until width) {
        var rsum = 0
        var gsum = 0
        var bsum = 0
        var routsum = 0
        var goutsum = 0
        var boutsum = 0
        var rinsum = 0
        var ginsum = 0
        var binsum = 0
        var yp = -radius * width
        for (i in -radius..radius) {
            yi = maxOf(0, yp) + x
            val sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            val rbs = r1 - kotlin.math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += width
            }
        }
        yi = x
        var stackpointer = radius
        for (y in 0 until height) {
            // Preserve alpha, the source bitmaps are opaque photos.
            pixels[yi] = -0x1000000 or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum

            val stackstart = stackpointer - radius + div
            var sir = stack[stackstart % div]

            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]

            if (x == 0) {
                vmin[y] = minOf(y + r1, hm) * width
            }
            val p = x + vmin[y]

            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]

            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]

            rsum += rinsum
            gsum += ginsum
            bsum += binsum

            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]

            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]

            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]

            yi += width
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
