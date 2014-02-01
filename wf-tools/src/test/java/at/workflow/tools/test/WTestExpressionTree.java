package at.workflow.tools.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.tools.expressiontree.ExpressionTree;
import at.workflow.tools.expressiontree.ExpressionTreeService;
import junit.framework.TestCase;

public class WTestExpressionTree extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	private ExpressionTreeService ets = new ExpressionTreeService();
	
	public void testWithBraces() {
		
		
		ExpressionTree myTree = new ExpressionTree();
		myTree=ets.parseString("(( a<b && c<d) || (a<c && b<d))");
        ets.printPrefix(myTree);
		
		myTree=ets.parseString("(( a<b && c<d) || (a<c && b<d)) && e<f");
        ets.printPrefix(myTree);
        //assertEquals("|| = st 10 || = st 11 = st 12 ",ets.getPrefix(myTree));
        
	}
	
	public void testDecimalNumber() {
		ExpressionTree myTree = new ExpressionTree();
        myTree=ets.parseString("pstamm.stwert5>5.0");
        ets.printPrefix(myTree);
        
        assertEquals("> pstamm.stwert5 5.0 ",ets.getPrefix(myTree));
	}
	
	public void testParseExpressionTree() {
		
		try {
			
			ExpressionTree myTree = new ExpressionTree();
			
			myTree=ets.parseString("(stabt = 'G01')");
            ets.printPrefix(myTree);
            assertEquals("= stabt G01 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("stabt='G01'");
            ets.printPrefix(myTree);
            assertEquals("= stabt G01 ",ets.getPrefix(myTree));
			
            myTree=ets.parseString("stpinfo3 = 'CCT-SUHL'");
            ets.printPrefix(myTree);
            assertEquals("= stpinfo3 CCT-SUHL ", ets.getPrefix(myTree));
            
            
            myTree=ets.parseString("stabt = 'G01' && stabt = 'XX'");
            ets.printPrefix(myTree);
            assertEquals("&& = stabt G01 = stabt XX ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("stabt='G01'&&stabt='XX'");
            ets.printPrefix(myTree);
            assertEquals("&& = stabt G01 = stabt XX ",ets.getPrefix(myTree));
            
            
            // added because of bug st=1 || st=2 || st=3
            myTree=ets.parseString("st1=1 || (s2t=2 || s3t=3)");
            ets.printPrefix(myTree);
            assertEquals("|| = st1 1 || = s2t 2 = s3t 3 ",ets.getPrefix(myTree));
            
            
			myTree=ets.parseString("stabt = 'G01' || stpinfo82 = '3'");
			ets.printPrefix(myTree);
            assertEquals("|| = stabt G01 = stpinfo82 3 ",ets.getPrefix(myTree));
             
            myTree=ets.parseString("stabt='G01'||stpinfo82='3'");
            ets.printPrefix(myTree);
            assertEquals("|| = stabt G01 = stpinfo82 3 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("stabt = 'G01' || stpinfo82 = '3' && stporg1 = '55'");
			ets.printPrefix(myTree);
            assertEquals("|| = stabt G01 && = stpinfo82 3 = stporg1 55 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("stabt='G01'||stpinfo82='3'&&stporg1='55'");
            ets.printPrefix(myTree);
            assertEquals("|| = stabt G01 && = stpinfo82 3 = stporg1 55 ",ets.getPrefix(myTree));
            
            
			
            myTree=ets.parseString("( ( stabt = 'G01' || stpinfo82 = '3' ) && stporg1 = '55' ) || stwp = 4");
			ets.printPrefix(myTree);
            assertEquals("|| && || = stabt G01 = stpinfo82 3 = stporg1 55 = stwp 4 ",ets.getPrefix(myTree));
            
            
            myTree=ets.parseString("( ( stabt = 'G01' OR stpinfo82 = '3' ) AND stporg1 = '55' ) OR stwp = 4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = stabt G01 = stpinfo82 3 = stporg1 55 = stwp 4 ",ets.getPrefix(myTree));

            myTree=ets.parseString("((stabt = 'G01' OR stpinfo82 = '3') and stporg1='55') or stwp==4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = stabt G01 = stpinfo82 3 = stporg1 55 = stwp 4 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("( ( stabt = 'G01' OR stpinfo82 = '3' ) AND stporg1 = '55' ) OR stwp<>4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = stabt G01 = stpinfo82 3 = stporg1 55 != stwp 4 ",ets.getPrefix(myTree));
            
            
            myTree=ets.parseString("((stabt='G01'||stpinfo82='3')&&stporg1='55')||stwp=4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = stabt G01 = stpinfo82 3 = stporg1 55 = stwp 4 ",ets.getPrefix(myTree));
			
			myTree=ets.parseString("1.stabt = 'G01'");
			ets.printPrefix(myTree);
            assertEquals("= 1.stabt G01 ",ets.getPrefix(myTree));

            myTree=ets.parseString("1.stabt='G01'");
            ets.printPrefix(myTree);
            assertEquals("= 1.stabt G01 ",ets.getPrefix(myTree));

            
            myTree=ets.parseString("1.stabt = 'G01' && 2.stabt = 'XX'");
			ets.printPrefix(myTree);
            assertEquals("&& = 1.stabt G01 = 2.stabt XX ",ets.getPrefix(myTree));
            
            
            
			myTree=ets.parseString("2.stabt = 'G01' || 3.stpinfo82 = '3'");
			ets.printPrefix(myTree);
            assertEquals("|| = 2.stabt G01 = 3.stpinfo82 3 ",ets.getPrefix(myTree));
            
            
			myTree=ets.parseString("1.stabt = 'G01' || 4.stpinfo82 = '3' && 2.stporg1 = '55'");
			ets.printPrefix(myTree);
            assertEquals("|| = 1.stabt G01 && = 4.stpinfo82 3 = 2.stporg1 55 ",ets.getPrefix(myTree));

            
            
            myTree=ets.parseString("( 1.stabt = 'G01' || 2.stpinfo82 = '3' ) && 1.stporg1 = '55'");
			ets.printPrefix(myTree);
            assertEquals("&& || = 1.stabt G01 = 2.stpinfo82 3 = 1.stporg1 55 ",ets.getPrefix(myTree));

            
            myTree=ets.parseString("( ( 1.stabt = 'G01' || 2.stpinfo82 = '3' ) && 1.stporg1 = '55' ) || 2.stwp = 4");
			ets.printPrefix(myTree);
            assertEquals("|| && || = 1.stabt G01 = 2.stpinfo82 3 = 1.stporg1 55 = 2.stwp 4 ",ets.getPrefix(myTree));
 
            
			
			myTree=ets.parseString("( ( pstamm.stabt = 'G01' || pkont.stpinfo82 = '3' ) && pstamm.stporg1 = '55' ) || pkont.stwp = 4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = pstamm.stabt G01 = pkont.stpinfo82 3 = pstamm.stporg1 55 = pkont.stwp 4 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("((pstamm.stabt='G01'||pkont.stpinfo82='3')&&pstamm.stporg1='55')||pkont.stwp=4");
            ets.printPrefix(myTree);
            assertEquals("|| && || = pstamm.stabt G01 = pkont.stpinfo82 3 = pstamm.stporg1 55 = pkont.stwp 4 ",ets.getPrefix(myTree));
            
            myTree=ets.parseString("st='10' || st='11' || st='12'");
            ets.printPrefix(myTree);
            assertEquals("|| = st 10 || = st 11 = st 12 ",ets.getPrefix(myTree));
            
            
		} catch (Exception e) {
            e.printStackTrace();
			fail("Exception happened: " + e);
		}
		
	}
	
	public void testBasicTest() {
		
		ExpressionTree myTree = ets.parseString("field == 3");
        StringBuffer sb = new StringBuffer();
        
		ets.createJSFromExpressionTree(myTree, "Person", sb, new ArrayList(), 0, 0, new HashMap()); 

		String jsExpression = sb.toString();
		System.out.println(jsExpression);

		assertNotNull(jsExpression);
		// somehow there is < in the 0 position as prefix
		int equals = jsExpression.indexOf("==");
		assertTrue(equals > 0);
		// is there 'field' left of the == sign?
		assertTrue(jsExpression.indexOf("'field'") < equals);
		// is there 3 right of the == sign?
		assertTrue(jsExpression.indexOf("3") > equals);
		
	}

	public void testCurrentDateReplacement() {
		
		ExpressionTree myTree = ets.parseString("steindat < CURRENTDATE");
		StringBuffer sb = new StringBuffer();
		
		ets.createJSFromExpressionTree(myTree, "Person", sb, new ArrayList(), 0, 0, new HashMap()); 
		
		String jsExpression = sb.toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd");
		
		System.out.println(jsExpression);
		
		assertNotNull(jsExpression);
		// somehow there is < in the 0 position as prefix
		int lessThan = jsExpression.indexOf('<');
		assertTrue(lessThan > 0);
		// is there 'steindat' before < sign?
		assertTrue(jsExpression.indexOf("'steindat'") < lessThan);
		// is there current date after the < sign?
		assertTrue(jsExpression.indexOf("new Date(" + sdf.format(new Date()) + ")") > lessThan);
		
	}
	
}
