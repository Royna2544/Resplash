package com.b_lam.resplash.util

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.widget.AspectRatioImageView
import com.b_lam.resplash.util.glide.BlurTransformation
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions

const val CROSS_FADE_DURATION = 350

private const val BLUR_HASH_SIZE = 32

fun ImageView.loadPhotoUrl(
    url: String,
    colorInt: Int? = null,
    colorString: String? = null,
    requestListener: RequestListener<Drawable>? = null
) {
    colorInt?.let { background = ColorDrawable(it) }
    colorString?.let { background = ColorDrawable(Color.parseColor(it)) }
    Glide.with(context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
        .addListener(requestListener)
        .into(this)
        .clearOnDetach()
}

fun ImageView.loadPhotoUrlWithThumbnail(
    url: String,
    thumbnailUrl: String,
    color: String?,
    blurHash: String? = null,
    centerCrop: Boolean = false,
    requestListener: RequestListener<Drawable>? = null
) {
    color?.let { background = ColorDrawable(Color.parseColor(it)) }
    Glide.with(context)
        .load(url)
        .placeholder(blurHash.toBlurHashDrawable(resources))
        .thumbnail(
            if (centerCrop) {
                Glide.with(context).load(thumbnailUrl).centerCrop()
            } else {
                Glide.with(context).load(thumbnailUrl)
            }
        )
        .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
        .addListener(requestListener)
        .into(this)
        .clearOnDetach()
}

/**
 * Turn Unsplash's `blur_hash` into a placeholder drawable.
 *
 * The hash travels with the photo metadata, so a rough version of the image can be shown while the
 * real one is still downloading instead of a flat block of colour. Decoding is done at a fixed
 * small size - a blur hash only carries a handful of components, so a larger bitmap costs time
 * without adding detail, and the ImageView stretches it back up.
 */
private fun String?.toBlurHashDrawable(resources: Resources): Drawable? {
    if (isNullOrBlank()) return null
    val bitmap = BlurHashDecoder.decode(this, BLUR_HASH_SIZE, BLUR_HASH_SIZE) ?: return null
    return BitmapDrawable(resources, bitmap)
}

fun ImageView.loadBlurredImage(
    url: String,
    color: String? = null,
    requestListener: RequestListener<Drawable>? = null
) {
    color?.let { background = ColorDrawable(Color.parseColor(it)) }
    Glide.with(context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
        .addListener(requestListener)
        .apply(RequestOptions.bitmapTransform(BlurTransformation()))
        .into(this)
        .clearOnDetach()
}

fun ImageView.loadProfilePicture(user: User) {
    loadProfilePicture(user.profile_image?.large)
}

fun ImageView.loadProfilePicture(url: String?) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.user_profile_picture_small_placeholder)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
        .into(this)
        .clearOnDetach()
}

fun AspectRatioImageView.setAspectRatio(width: Int?, height: Int?) {
    if (width != null && height != null) {
        aspectRatio = height.toDouble() / width.toDouble()
    }
}