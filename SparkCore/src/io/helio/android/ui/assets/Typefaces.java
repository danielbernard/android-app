package io.helio.android.ui.assets;

import static org.solemnsilence.util.Py.map;

import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;


public class Typefaces {

	// NOTE: this is tightly coupled to the filenames in assets/fonts
	public static enum Style {
		BOLD("roboto_bold.otf"),
		BOLD_ITALIC("roboto_bolditalics.otf"),
		BOOK("roboto_regular.otf"),
		BOOK_ITALIC("roboto_regularitalic.otf"),
		LIGHT("roboto_light.otf"),
		LIGHT_ITALIC("roboto_lightitalic.otf"),
		MEDIUM("roboto_medium.otf"),
		MEDIUM_ITALIC("roboto_mediumitalic.otf");

		public final String fileName;

		private Style(String name) {
			fileName = name;
		}
	}


	private static final Map<Style, Typeface> typefaces = map();


	public static Typeface getTypeface(Context ctx, Style style) {
		Typeface face = typefaces.get(style);
		if (face == null) {
			face = Typeface.createFromAsset(ctx.getAssets(), "fonts/" + style.fileName);
			typefaces.put(style, face);
		}
		return face;
	}

}