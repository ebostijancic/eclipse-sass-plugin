package at.workflow.tools.launcher;

public interface JLauncher {

    public void configure(String className, String[] args);
    
    public void execute();
    
}
