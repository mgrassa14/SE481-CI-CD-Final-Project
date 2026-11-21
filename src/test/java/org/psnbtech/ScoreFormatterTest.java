package org.psnbtech;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScoreFormatterTest {

	@Test
	public void padsSmallScoreToFiveDigits() {
		assertEquals("00075", ScoreFormatter.format(75));
	}

	@Test
	public void zeroBecomesAllZeros() {
		assertEquals("00000", ScoreFormatter.format(0));
	}

	@Test
	public void longScoreIsNotCut() {
		assertEquals("123456", ScoreFormatter.format(123456));
	}
}
