package at.workflow.webdesk.tools;

import junit.framework.TestCase;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * sdzuban 19.07.2011
 */
/**
 * Test class for speed of javaScript engines Mozilla and the engine of Java 1.6.
 * 
 * @author sdzuban
 *
 */
public class TestMozillaVsJavaJavascriptSpeed extends TestCase {

	public void testSpeed () {

		String javaScript = "new Date(2011,07,18) < new Date(2011,07,19)";
		int count = 1000;
		
		long jsMillis = System.currentTimeMillis();

		for (int i = 0; i < count; i++) {
			mozillaEvaluate(javaScript);
		}
		
		final long mozillaMillis = System.currentTimeMillis() - jsMillis;

		jsMillis = System.currentTimeMillis();
		
		Context cx = Context.enter();
		
		for (int i = 0; i < count; i++) {
			mozillaEvaluate(javaScript, cx);
		}
		
		Context.exit();
		
		final long mozilla2Millis = System.currentTimeMillis() - jsMillis;
		
		jsMillis = System.currentTimeMillis();
		
		cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
		
		for (int i = 0; i < count; i++) {
			mozillaEvaluate(javaScript, cx, scope);
		}

		Context.exit();
		
		final long mozilla3Millis = System.currentTimeMillis() - jsMillis;
		
		jsMillis = System.currentTimeMillis();
		
/* java 6
		for (int i = 0; i < count; i++) {
			javaEvaluate(javaScript);
		}
		
		long javaMillis = System.currentTimeMillis() - jsMillis;
		
		jsMillis = System.currentTimeMillis();
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		for (int i = 0; i < count; i++) {
			javaEvaluate(javaScript, engine);
		}
*/		
		long java2Millis = System.currentTimeMillis() - jsMillis;
		
		System.out.println("Mozilla: " + mozillaMillis + ", Mozilla shared context: " + mozilla2Millis
				+ ", Mozilla schared context and scope: " + mozilla3Millis);
//		System.out.println("java full: " + javaMillis + ", java shared engine: " + java2Millis);
	}

	private void mozillaEvaluate(String javaScript) {

		Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();
        
        Object result = cx.evaluateString(scope, javaScript , "<cmd>", 1, null);
        
        if (result instanceof NativeJavaObject) {
        	result = ((NativeJavaObject)result).unwrap();
        }
		if (result instanceof org.mozilla.javascript.NativeJavaObject)
			result = ((org.mozilla.javascript.NativeJavaObject)result).unwrap();
	}

	private void mozillaEvaluate(String javaScript, Context cx) {
		
		Scriptable scope = cx.initStandardObjects();
		
		Object result = cx.evaluateString(scope, javaScript , "<cmd>", 1, null);
		
		if (result instanceof NativeJavaObject) {
			result = ((NativeJavaObject)result).unwrap();
		}
		if (result instanceof org.mozilla.javascript.NativeJavaObject)
			result = ((org.mozilla.javascript.NativeJavaObject)result).unwrap();
	}

	private void mozillaEvaluate(String javaScript, Context cx, Scriptable scope) {
		
		Object result = cx.evaluateString(scope, javaScript , "<cmd>", 1, null);
		
		if (result instanceof NativeJavaObject) {
			result = ((NativeJavaObject)result).unwrap();
		}
		if (result instanceof org.mozilla.javascript.NativeJavaObject)
			result = ((org.mozilla.javascript.NativeJavaObject)result).unwrap();
	}
	
/* java 6	
	private void javaEvaluate(String javaScript) {

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		// expose object as variable to script
		// engine.put("name", value);

		javaEvaluate(javaScript, engine);
	}
	
	private void javaEvaluate(String javaScript, ScriptEngine engine) {
		// evaluate a script string. 
		try {
			Object result = engine.eval(javaScript);
		} catch (ScriptException e) {
			System.err.println("Java JS Machine reported error " + e);
		}
	}
*/	
}

