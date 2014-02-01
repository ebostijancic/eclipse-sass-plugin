package at.workflow.tools.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JUnit-4 helper annotation that enables method calls in a certain order.
 * For example see OrderedExampleTest.
 * 
 * @see http://stackoverflow.com/questions/3089151/specifying-an-order-to-junit-4-tests-at-the-method-level-not-class-level
 * @author fritzberger 11.09.2012
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order
{
	/** Represents the integer annotation attribute "order". */
    public int order();

}
