package org.psnbtech;

import org.apache.commons.lang3.StringUtils;

/**
 * Formats numbers for the side panel.
 * Uses Apache Commons Lang (our runtime dependency, managed by Maven).
 */
public class ScoreFormatter {

	/** Pads a number with zeros to 4 digits, e.g. 75 -> "0075". */
	public static String format(int value) {
		return StringUtils.leftPad(String.valueOf(value), 4, '0');
	}
}
