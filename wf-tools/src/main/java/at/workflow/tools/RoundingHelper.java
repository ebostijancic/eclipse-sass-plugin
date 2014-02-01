package at.workflow.tools;

import java.math.BigDecimal;

/**
 * Convenience class to round decimals which uses BigDecimal internally to avoid nasty
 * rounding errors. Rounding is done commercially (>=0,5 UP, otherwise DOWN) 
 * 
 * @author ggruber
 */
public abstract class RoundingHelper {
	
	public static double roundValue(double value, double roundingPrecision) {
		int decimalPlace = roundingPrecision2DecimalPlaces(roundingPrecision);
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(decimalPlace , BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	private static int roundingPrecision2DecimalPlaces(double roundingPrecision) {
		int rndPrcInt = (int) roundingPrecision;
		String rndPrcStr = Integer.toString( rndPrcInt );
		int decimalPlace = rndPrcStr.length() - 1;
		return decimalPlace;
	}
	
	public static double roundValueScale2(double value) {
		return roundValue(value, 100.0);
	}
	
	public static double roundValueWithScale(double value, int decimalPlaces) {
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(decimalPlaces , BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
	
}
