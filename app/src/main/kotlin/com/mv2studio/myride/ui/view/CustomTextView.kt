package com.mv2studio.myride.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.CheckedTextView
import android.widget.TextView
import com.mv2studio.myride.R
import com.mv2studio.myride.extensions.assets
import java.util.*

/**
 * Created by matej on 21/11/2016.
 */
open class CustomTextView(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : CheckedTextView(context, attrs, defStyle) {

    companion object {
        val cache = HashMap<String, Typeface>()

        init {
            Font.values().forEach { cache.put(it.fileName, Typeface.createFromAsset(assets, "fonts/${it.fileName}.ttf"))}
        }
    }

    enum class Font(val fileName: String) {
        LatoBlack("latoBlack"),
        LatoLight("latoLight"),
        LatoRegular("latoRegular"),
        RalewayBlack("ralwayBlack"),
        RalewayRegular("ralewayRegular");

        val typeface = cache[fileName]
    }

    constructor(context: Context?) : this(context, null as AttributeSet?, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        if (attrs != null) {
            context?.obtainStyledAttributes(attrs, R.styleable.CustomTextView)?.apply {
                val fontName = getString(R.styleable.CustomTextView_font)
                typeface = cache[fontName]
                recycle ()
            }
        }
    }

    fun setTypeface(font: Font) {
        typeface = font.typeface
    }
}