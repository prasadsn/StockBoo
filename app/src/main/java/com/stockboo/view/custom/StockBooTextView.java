package com.stockboo.view.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class StockBooTextView extends TextView {

	public StockBooTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Brandon_reg.otf");
		setTypeface(font);
	}

	public StockBooTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Verdana.ttf");
		setTypeface(font);
	}

	public StockBooTextView(Context context) {
		super(context);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Verdana.ttf");
		setTypeface(font);
	}
}