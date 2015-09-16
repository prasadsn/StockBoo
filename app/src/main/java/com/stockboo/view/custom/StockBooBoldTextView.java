package com.stockboo.view.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class StockBooBoldTextView extends TextView {

	public StockBooBoldTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Brandon_reg.otf");
		setTypeface(font);
		setTypeface(getTypeface(), Typeface.BOLD);
	}

	public StockBooBoldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Verdana_Bold.ttf");
		setTypeface(font);
		setTypeface(getTypeface(), Typeface.BOLD);
	}

	public StockBooBoldTextView(Context context) {
		super(context);
		Typeface font = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/Verdana_Bold.ttf");
		setTypeface(font);
		setTypeface(getTypeface(), Typeface.BOLD);
	}
}