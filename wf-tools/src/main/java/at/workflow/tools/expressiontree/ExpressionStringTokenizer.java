/*
 * Created on 17.01.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.tools.expressiontree;

import java.util.StringTokenizer;

public class ExpressionStringTokenizer  {
    private String s;
    private StringTokenizer st;
    private String tail;
    String delimeter;
    
    public ExpressionStringTokenizer(String str) {
        st = new StringTokenizer(str);
        s = str;
        tail = str;
        delimeter = " ";
    }
    
    public ExpressionStringTokenizer(String str,String delimeter) {
        st = new StringTokenizer(str,delimeter);
        s = str;
        tail = str;
        this.delimeter = delimeter;
    }
    
    public boolean hasMoreTokens() {
        return st.hasMoreTokens();
    }
    
    public String nextToken() {
        String actToken = st.nextToken();
        if (st.hasMoreTokens())
            s = s.substring(actToken.length()+delimeter.length());
        else
            s="";
        return actToken;
    }
    
    
    public String getTail() {
        return s;
    }
    

}
