package id.community.core.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import id.community.core.R

@BindingAdapter(
    "glideSrc",
    "glideCenterCrop",
    "glideCircularCrop"
)
fun ImageView.bindGlideSrc(
    drawableRes: String?,
    centerCrop: Boolean = false,
    circularCrop: Boolean = false
) {
    if (drawableRes == null || drawableRes.isEmpty()) {
        setImageResource(R.drawable.avatar_none)
        return
    }

    createGlideRequest(
        context,
        drawableRes,
        centerCrop,
        circularCrop
    ).into(this)
}

fun createGlideRequest(
    context: Context,
    drawableRes: String,
    centerCrop: Boolean,
    circularCrop: Boolean
): RequestBuilder<Drawable> {
    val req = Glide.with(context).load(drawableRes)
    if (centerCrop) req.centerCrop()
    if (circularCrop) req.circleCrop()
    return req
}

@BindingAdapter("goneIf")
fun View.bindGoneIf(gone: Boolean) {
    visibility = if (gone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}
