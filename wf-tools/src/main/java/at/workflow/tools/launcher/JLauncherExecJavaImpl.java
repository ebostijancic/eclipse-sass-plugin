package at.workflow.tools.launcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JLauncherExecJavaImpl implements JLauncher {

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String className;
    private String[] args;
    private String javaCmd;
    
    public JLauncherExecJavaImpl(String javaCmd) {
    	if (javaCmd!=null && !javaCmd.equals(""))
    		this.javaCmd = javaCmd;
    	else
    		this.javaCmd = "java";
	}
	
	public void configure(String className, String[] args) {
        this.className = className;
        this.args = args;
	}

	public void execute() {
        try {
        	
        	String execString;
        	
        	execString = javaCmd + " -classpath " + System.getProperty("java.class.path") 
        		+ " -Djava.library.path=\"" + System.getProperty("java.library.path") + "\""
        		+ " " + this.className;
        	
        	//+ " -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
        	
        	for(int i=0; i < this.args.length; i++)
        		execString += " " + args[i];

        	System.out.println("executionString=" + execString);
        	
        	String[] envParams = { };
        	//String[] envParams = {"PATH=D:\\inter\\inter_v1.60_117\\bin"};
        	
        	Process proc = Runtime.getRuntime().exec(execString, envParams);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String inputLine;

            String errorString="";
            while ((inputLine = in.readLine()) != null) 
                errorString += inputLine;
            in.close();
            
            if (errorString.length()>0)
                System.out.println("External started process returned errormessage: " + errorString);

        } catch (Exception e) {
        	this.logger.error("could not start class " + this.className + " in external process", e);
        }
        
	}

}
