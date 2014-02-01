package at.workflow.webdesk.tools.numbers;

import java.util.Comparator;

/**
 * This is comparator for comparing two numbers, optionally with given tolerance.
 * 
 * Currently the numbers can be Integer, Long, Float and Double.
 * 
 * @author sdzuban 02.04.2013
 */
public class NumberComparator implements Comparator<Number> {

    private Class<? extends Number> numberType;
    private Number tolerance;

    public NumberComparator(Class<? extends Number> numberType,  Number... tolerance) {
        super();
        if (!NumberSumCalculator.SUPPORTED_TYPES.contains(numberType))
            throw new IllegalArgumentException(numberType.getSimpleName() + " is not supported");
        this.numberType = numberType;
        
        if (tolerance != null) {
        	if (tolerance.length == 0) {
        		this.tolerance = getZeroToleranceNumber();
        	} else if (tolerance.length == 1) {
                if (isIntegerOrLong() && tolerance[0].intValue() < 0 ||
                        isFloatOrDouble() && tolerance[0].doubleValue() < 0.0)
                    throw new IllegalArgumentException("Tolerance must be 0 or positiove: " + tolerance[0]);
                this.tolerance = tolerance[0];
            } else if (tolerance.length > 1)
                throw new IllegalArgumentException("More than one value entered as tolerance");
        } else
            this.tolerance = getZeroToleranceNumber();
    }

	/** {@inheritDoc} */
    @Override
    public int compare(Number number1, Number number2) {

        if (isIntegerOrLong()) {

            long difference = number1.longValue() - number2.longValue();
            if (Math.abs(difference) <= tolerance.longValue())
                return 0;
            else if (difference > 0)
                return 1;
            else
                return -1;

        } else if (isFloatOrDouble()) {

            double difference = number1.doubleValue() - number2.doubleValue();
            if (Math.abs(difference) <= tolerance.doubleValue())
                return 0;
            else if (difference > 0)
                return 1;
            else
                return -1;
        } else
            throw new RuntimeException("Unimplemented type " + numberType.getCanonicalName());
    };

    private boolean isIntegerOrLong() {
        return numberType == Integer.class || numberType == Long.class;
    }

    private boolean isFloatOrDouble() {
        return numberType == Float.class || numberType == Double.class;
    }

    private Number getZeroToleranceNumber() {
    	return isIntegerOrLong() ? 0L : 0.0;
    }
}
