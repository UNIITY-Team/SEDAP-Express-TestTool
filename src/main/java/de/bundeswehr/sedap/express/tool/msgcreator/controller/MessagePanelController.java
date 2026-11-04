/**
 * Note: This license has also been called the “Simplified BSD License” and the “FreeBSD License”.
 *
 * Copyright 2024-2026 UNIITY POC: Volker Voß, Federal Armed Forces of Germany
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSEnARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package de.bundeswehr.sedap.express.tool.msgcreator.controller;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import de.bundeswehr.uniity.sedapexpress.messages.SEDAPExpressMessage;
import de.bundeswehr.uniity.sedapexpress.messages.SEDAPExpressMessage.Acknowledgement;
import de.bundeswehr.uniity.sedapexpress.messages.SEDAPExpressMessage.Classification;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * 
 * @author Volker Voß
 *
 */
public abstract class MessagePanelController {

	public abstract SEDAPExpressMessage createMessage(Byte number, Long time, String sender, Classification classification, Acknowledgement acknowledgement, String mac);

	public abstract boolean isValidFilled();

	public static boolean isValid8BitHex(String input) {
		if (input == null || input.length() != 2) {
			return false;
		}
		try {
			int value = Integer.parseInt(input, 16);
			return value >= 0 && value <= 255;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isValid7BitHex(String input) {
		if (input == null || input.length() != 2) {
			return false;
		}
		try {

			int value = Integer.parseInt(input, 16);
			return value >= 0 && value <= 127;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static TextFormatter<Integer> createHexFormatter() {

		StringConverter<Integer> hexConverter = new StringConverter<Integer>() {
			@Override
			public String toString(Integer object) {
				return object != null ? Integer.toHexString(object).toUpperCase() : "";
			}

			@Override
			public Integer fromString(String string) {
				return string.isEmpty() ? null : Integer.parseInt(string, 16);
			}
		};

		UnaryOperator<TextFormatter.Change> hexFilter = change -> {
			String newText = change.getControlNewText();
			if (newText.matches(SEDAPExpressMessage.NUMBER_MATCHER.pattern()) || newText.equals("")) {
				return change;
			}
			return null;
		};

		return new TextFormatter<>(hexConverter, null, hexFilter);
	}

	public static TextFormatter<String> createRGBAHexFormatter() {

		UnaryOperator<TextFormatter.Change> filter = change -> {
			String newText = change.getControlNewText().toUpperCase();
			if (newText.matches("^#?[0-9A-Fa-f]{0,8}$")) {
				// if (newText.matches(SEDAPExpressMessage.NUMBER_MATCHER.pattern())) {
				change.setText(change.getText().toUpperCase());
				return change;
			}
			return null;
		};

		return new TextFormatter<>(filter);
	}

	public static TextFormatter<Double> createCourseFormatter() {

		Pattern validEditigState = Pattern.compile("^\\d{0,3}(\\.\\d{0,2})?$");

		StringConverter<Double> courseConverter = new StringConverter<Double>() {
			@Override
			public Double fromString(String s) {
				if (s.isEmpty())
					return 0.0;
				return (Double.valueOf(s));
			}

			@Override
			public String toString(Double d) {
				if (d == null)
					return "";
				return String.format("%.2f", d);
			}
		};

		UnaryOperator<TextFormatter.Change> courseFilter = change -> {
			String newText = change.getControlNewText();

			if (validEditigState.matcher(newText).matches()) {
				try {
					double value = Double.parseDouble(newText);
					if (value >= 0.0 && value <= 359.99) {
						return change;
					}
				} catch (NumberFormatException ex) {
					if (newText.isEmpty()) {
						return change;
					}
				}

				return change;
			} else {
				return null;
			}
		};

		return new TextFormatter<>(courseConverter, null, courseFilter);
	}

	public static TextFormatter<Double> createLatitudeFormatter() {

		DecimalFormat format = new DecimalFormat("00.0000");

		StringConverter<Double> latitudeConverter = new StringConverter<Double>() {
			@Override
			public String toString(Double value) {
				if (value == null) {
					return "";
				}
				return format.format(value);
			}

			@Override
			public Double fromString(String string) {
				if (string == null || string.isEmpty()) {
					return null;
				}
				ParsePosition parsePosition = new ParsePosition(0);
				Double result = format.parse(string, parsePosition).doubleValue();
				return parsePosition.getIndex() == string.length() ? result : null;
			}
		};

		UnaryOperator<TextFormatter.Change> latitudeFilter = change -> {
			String newText = change.getControlNewText();

			if (newText.matches("-?\\d*\\.?\\d*")) {
				try {
					double value = Double.parseDouble(newText);
					if (value >= -90 && value <= 90) {
						return change;
					}
				} catch (NumberFormatException e) {
					if (newText.isEmpty() || newText.equals("-")) {
						return change;
					}
				}
			}
			return null;
		};
		return new TextFormatter<>(latitudeConverter, null, latitudeFilter);
	}

	public static TextFormatter<Double> createLongitudeFormatter() {

		DecimalFormat format = new DecimalFormat("000.0000");

		StringConverter<Double> longitudeConverter = new StringConverter<Double>() {
			@Override
			public String toString(Double value) {
				if (value == null) {
					return "";
				}
				return format.format(value);
			}

			@Override
			public Double fromString(String string) {
				if (string == null || string.isEmpty()) {
					return null;
				}
				ParsePosition parsePosition = new ParsePosition(0);
				Double result = format.parse(string, parsePosition).doubleValue();
				return parsePosition.getIndex() == string.length() ? result : null;
			}
		};

		UnaryOperator<TextFormatter.Change> longitudeFilter = change -> {
			String newText = change.getControlNewText();

			if (newText.matches("-?\\d*\\.?\\d*")) {
				try {
					double value = Double.parseDouble(newText);
					if (value >= -180 && value <= 180) {
						return change;
					}
				} catch (NumberFormatException e) {
					if (newText.isEmpty() || newText.equals("-")) {
						return change;
					}
				}
			}
			return null;
		};
		return new TextFormatter<>(longitudeConverter, null, longitudeFilter);
	}

	public static TextFormatter<Double> createBearingFormatter() {

		DecimalFormat format = new DecimalFormat("000.0000");
		format.setDecimalSeparatorAlwaysShown(true);

		StringConverter<Double> bearingConverter = new StringConverter<Double>() {
			@Override
			public String toString(Double value) {
				if (value == null) {
					return "";
				}
				return format.format(value);
			}

			@Override
			public Double fromString(String string) {
				if (string == null || string.isEmpty()) {
					return null;
				}
				ParsePosition parsePosition = new ParsePosition(0);
				Double result = format.parse(string, parsePosition).doubleValue();
				if (parsePosition.getIndex() < string.length() || result < 0 || result > 360) {
					return null;
				}
				return result;
			}
		};

		UnaryOperator<TextFormatter.Change> bearingFilter = change -> {
			String newText = change.getControlNewText();

			if (newText.matches("\\d{0,3}(\\.\\d{0,4})?")) {
				try {
					double value = Double.parseDouble(newText);
					if (value >= 0 && value <= 359.9999) {
						return change;
					}
				} catch (NumberFormatException e) {
					if (newText.isEmpty() || newText.equals("-")) {
						return change;
					}
				}
			}
			return null;
		};
		return new TextFormatter<>(bearingConverter, null, bearingFilter);
	}

	public static TextFormatter<Integer> posIntegerFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, change -> {
		String newText = change.getControlNewText();
		if (newText.matches("([1-9][0-9]*)?")) {
			return change;
		}
		return null;
	});

	// Dimension
	public static final List<String> DimensionsList = new ArrayList<>(Arrays.asList("Unknwon", "Space", "Air", "Surface", "SubSurface", "Ground"));

	public static char getDimensionCharFromIndex(int index) {
		return switch (index) {
		case 0 -> 'z';
		case 1 -> 'p';
		case 2 -> 'a';
		case 3 -> 's';
		case 4 -> 'u';
		case 5 -> 'g';
		default -> 'z';
		};

	}

	// Identity
	public static final List<String> identitiesList = new ArrayList<>(Arrays.asList("Pending", "Unknown", "Hostile", "Suspect", "Neutral", "Assumed friend", "Friend"));

	public static final char getIdentiy(String ident) {
		return switch (ident) {
		case "Pending" -> 'p';
		case "Unknown" -> 'u';
		case "Hostile" -> 'h';
		case "Suspect" -> 's';
		case "Neutral" -> 'n';
		case "Assumed friend" -> 'a';
		case "Friend" -> 'f';
		default -> 'p';
		};
	}
}
