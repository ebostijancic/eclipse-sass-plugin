package at.workflow.webdesk.tools;

import junit.framework.TestCase;

/**
 * @author fritzberger 21.11.2012
 * @author fritzberger 10.05.2013
 */
public class WTestTextUtils extends TestCase {

	public void testCutOrFillUp()	{
		String s;
		
		s = "Formatierter Text"; 
		assertEquals("Forma", TextUtils.cutOrFillup(s, 5, ' ', true)); 
		assertEquals("Forma", TextUtils.cutOrFillup(s, 5, ' ', false)); 
		
		s = "Text"; 
		assertEquals("Text ", TextUtils.cutOrFillup(s, 5, ' ', true)); 
		assertEquals(" Text", TextUtils.cutOrFillup(s, 5, ' ', false)); 
		
		s = null;
		assertNull(TextUtils.cutOrFillup(s, 5, ' ', true));
		
		s = "";
		assertEquals("     ", TextUtils.cutOrFillup(s, 5, ' ', true));
	}
	
	public void testCamelCaseToWords()	{
		String s;
		
		s = "lastName"; 
		assertEquals("Last Name", TextUtils.camelCaseToWords(s));
		
		s = "taID"; 
		assertEquals("Ta ID", TextUtils.camelCaseToWords(s));
		
		s = "DVDAndCDPlayer"; 
		assertEquals("DVD And CD Player", TextUtils.camelCaseToWords(s));
		
		s = "isDVDPlayer"; 
		assertEquals("Is DVD Player", TextUtils.camelCaseToWords(s));
		
		s = "playsDVDAndCD";
		assertEquals("Plays DVD And CD", TextUtils.camelCaseToWords(s));
	}

	public void testDottedCamelCaseToWords()	{
		String s;
		
		s = "person.lastName"; 
		assertEquals("Person Last Name", TextUtils.dottedCamelCaseToWords(s));
		
		s = "person.taID"; 
		assertEquals("Person Ta ID", TextUtils.dottedCamelCaseToWords(s));
	}
	
	public void testLastDottedCamelCaseToWords()	{
		String s;
		
		s = "person.lastName"; 
		assertEquals("Last Name", TextUtils.lastOfDottedCamelCaseToWords(s));
		
		s = "person.taID"; 
		assertEquals("Ta ID", TextUtils.lastOfDottedCamelCaseToWords(s));
	}

	public void testGetSingularFirstCharUpperCasePropertyName()	{
		String s;
		
		s = "jobSkills";
		assertEquals("jobSkill", TextUtils.getSingularPropertyName(s));
		
		s = "jobFamilies";
		assertEquals("jobFamily", TextUtils.getSingularPropertyName(s));
		
		s = "jobTasks";
		assertEquals("jobTask", TextUtils.getSingularPropertyName(s));
	}
	
	public void testGetFirstAndNextPart()	{
		String s;
		String [] result;
		
		s = "person";
		result = TextUtils.getFirstAndNextPart(s);
		assertEquals(2, result.length);
		assertEquals("person", result[0]);
		assertNull(result[1]);
		
		s = "personGroup.person.lastName";
		result = TextUtils.getFirstAndNextPart(s);
		assertEquals(2, result.length);
		assertEquals("personGroup", result[0]);
		assertNotNull(result[1]);
		assertEquals("person.lastName", result[1]);
	}
	
	public void testGetRightmostPropertyName()	{
		String s;
		
		s = "personGroup.person.lastName";
		assertEquals("lastName", TextUtils.getRightmostPropertyName(s));		
	}

	private class MyInnerClass
	{
	}
	
	public void testGetSimpleClassname()	{
		assertEquals(getClass().getSimpleName()+"$"+MyInnerClass.class.getSimpleName(), TextUtils.getSimpleClassName(MyInnerClass.class.getName()));
		
		assertEquals("D", TextUtils.getSimpleClassName("D"));
		assertNull(TextUtils.getSimpleClassName("d"));
		assertEquals("D", TextUtils.getSimpleClassName("a.b.c.D"));
		assertNull(TextUtils.getSimpleClassName("a.b.c.d"));
		assertEquals("D", TextUtils.getSimpleClassName("a.b.C.D"));
		assertEquals("C$D", TextUtils.getSimpleClassName("a.b.C$D"));	// inner classes
	}
	
	public void testGetEnclosingSimpleClassname()	{
		assertEquals(getClass().getSimpleName(), TextUtils.getEnclosingSimpleClassname(MyInnerClass.class.getName()));
		
		assertEquals("D$E", TextUtils.getEnclosingSimpleClassname("a.b.c.D$E$F"));	// inner classes
		assertNull(TextUtils.getEnclosingSimpleClassname("a.b.c.D"));
	}

}
