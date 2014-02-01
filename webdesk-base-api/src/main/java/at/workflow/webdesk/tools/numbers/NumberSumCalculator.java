package at.workflow.webdesk.tools.numbers;

import java.util.Arrays;
import java.util.List;

/**
 * This calculator sums Numbers.
 * 
 * The numbers can be Integer, Long, Float and Double.
 * 
 * The accumulator used is determined by the resultType.
 * If the result type is Integer or Long the numbers are accumulated in long accumulator.   
 * If the result type is Float or Double the numbers are accumulated in double accumulator.   
 * This is to avoid deteriorating 1234 to e.g. 1233.9999995 or cutting 987.3 to 987.
 * 
 * @author sdzuban 02.04.2013
 */
public class NumberSumCalculator {

    @SuppressWarnings("unchecked")
    public static final List<Class<? extends Number>> SUPPORTED_TYPES =
            Arrays.<Class<? extends Number>>asList(Integer.class, Long.class,Float.class, Double.class);

    private Class<? extends Number> numberType;

    private long longSum;
    private double doubleSum;


    public NumberSumCalculator(Class<? extends Number> numberType) {
        if (!SUPPORTED_TYPES.contains(numberType))
            throw new IllegalArgumentException(getTypeExceptionMessage());
        this.numberType = numberType;
    }

    public void reset() {
        longSum = 0;
        doubleSum = 0.0;
    }

    public void add(Number share) {

        if (numberType == Integer.class || numberType == Long.class) {
        	longSum += share.longValue();
        } else if (numberType == Float.class || numberType == Double.class) {
            doubleSum += share.doubleValue();
        } else
            throw new IllegalArgumentException(getTypeExceptionMessage());
    }

    public Number getResult() {
    	

        if (numberType == Integer.class)
            return new Integer((int) longSum);
        else if (numberType == Long.class)
            return new Long(longSum);
        else if (numberType == Float.class)
            return new Float((float) doubleSum);
        else if (numberType == Double.class)
            return new Double(doubleSum);
        else
            throw new IllegalArgumentException(getTypeExceptionMessage());
    }

    private String getTypeExceptionMessage() {
        return "ResultType is none of " + SUPPORTED_TYPES;
    }

}
