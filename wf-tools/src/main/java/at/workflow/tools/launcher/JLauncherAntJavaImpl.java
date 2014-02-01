package at.workflow.tools.launcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;


public class JLauncherAntJavaImpl extends Java implements JLauncher {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String className;
    
    public JLauncherAntJavaImpl() {
        Project myProject = new Project();
        myProject.init();
        
        this.setProject(myProject);
        this.setTaskName("java");
        this.setTaskType("java");
        this.setOwningTarget(new Target());
    }
    
    public void configure(String className, String[] args) {
        this.className = className;
        this.setClassname(className);
        for (int i=0;i<args.length;i++)
            this.createArg().setValue(args[i]);
        this.createClasspath();
        this.createBootclasspath();
        this.createJvmarg();
        
        // this jvm property is important
        // it tells the jvm NOT to terminate
        // when the actual NT user logs out!
        this.setJvmargs("-Xrs -Xmx128M");
        
        this.setTimeout(null);
        
        Path path = new Path(this.getProject());  
        
        path.setPath(System.getProperty("java.class.path"));
        this.setClasspath(path);
       
        // set java.library path -> important von jniwrapper
        Variable myVar = new Variable();
        myVar.setKey("java.library.path");
        myVar.setValue(System.getProperty("java.library.path"));
        this.addSysproperty(myVar);
        
        
        this.setFork(true);
        this.setLocation(Location.UNKNOWN_LOCATION);
        this.setNewenvironment(true);
    }
    
    public void execute() {
        try {
            super.execute();
        } catch (Exception e) {
            this.logger.error("could not start class " + this.className + " in external process");
        }
    }

}
