package at.workflow.tools.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class JLauncherWrapperImpl implements JLauncher {
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String className;
    private String argString="";
    private String pathToWrapper;
    private String pathToConf;
    
    public JLauncherWrapperImpl(String pathToWrapper, String pathToConf) {
        
        this.pathToConf = pathToConf;
        this.pathToWrapper = pathToWrapper;
    }

    public void configure(String className, String[] args) {
        this.className = className;
        for(int i=0;i<args.length;i++)
            this.argString += " wrapper.app.parameter." + (i+2) + "=" + args[i];
        
    }
    
    public void execute() {
        try {
            Resource absoluteWrapper = new ClassPathResource(this.pathToWrapper);
            Resource absoluteConf = new ClassPathResource(this.pathToConf);
            String executionString = 
                absoluteWrapper.getFile().getAbsolutePath() + " -c " + 
                absoluteConf.getFile().getAbsolutePath() + 
                " wrapper.app.parameter.1=" + 
                this.className + this.argString;
            
            this.logger.info("start RMI Server via this execute-string: " + executionString);
            Process proc = Runtime.getRuntime().exec(executionString);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String inputLine;

            String errorString="";
            while ((inputLine = in.readLine()) != null) 
                errorString += inputLine;
            in.close();
            
            if (errorString.length()>0)
                throw new RuntimeException("External started process returned errormessage: " + errorString);
            
            
        } catch (IOException e) {
            this.logger.error("could not start external class in seperate process" ,e);
        }
    }



}
