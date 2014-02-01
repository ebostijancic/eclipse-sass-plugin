/*
 * Created on 16.03.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.tools;

public class InputReader extends Thread {
        boolean run;
        public InputReader() {
            super();
            run = true;
        }
    
        public void run() throws RuntimeException{
            int c =0;
            try {
                c = System.in.read();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (c==110)
                run = false;
        }
        
        public boolean getRun() {
            return run;
        }
    
}
