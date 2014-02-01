/*
 * Created on 12.12.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertiesWithOrder implements Serializable {

    private Log logger = LogFactory.getLog(this.getClass());
    private List props = new ArrayList();
    
    public void load(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = "";
        try {
            while ((s=br.readLine())!=null) {
                if (!s.startsWith("#")) {
                    if (s.indexOf("=")!=-1) {
                        // a property 
                        PropertiesObject po = new PropertiesObject();
                        po.setKey(s.substring(0,s.indexOf("=")));
                        po.setValue(s.substring(s.indexOf("=")+1,s.length()));
                        props.add(po);
                        logger.debug("added " + po);
                    }
                } else {
                    // comment 
                    PropertiesObject po = new PropertiesObject();
                    po.comment = s;
                    props.add(po);
                    logger.debug("added " + po);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void setProperty(String key, String value) {
        if (this.contains(key)) {
            this.remove(key);
        }
        
        PropertiesObject po = new PropertiesObject();
        po.setKey(key);
        po.setValue(value);
        props.add(po);
        
    }
    
    public String getProperty(String key) {
        if (this.contains(key)) {
            return ((PropertiesObject) props.get(this.indexOf(key))).getValue();
        } else
            return null;
    }
    
    /**
     * @param key the key used to compare.
     * @return a <code>List</code> of <code>String</code>s representing the 
     * <code>keys</code> of the <code>PropertiesObject</code> that have matched 
     * the given <code>key</code> (<code>PropertiesObject</code>.getKey().indexOf(key)==0)
     */
    public List getKeysOfProperties(String key) {
    	List res = new ArrayList();
    	Iterator i = props.iterator();
    	while (i.hasNext()) {
    		PropertiesObject o = (PropertiesObject) i.next();
    		if (o.getKey()!=null && o.getKey().indexOf("key")==0)
    			res.add(o.getKey());
    	}
    	return res;
    }
    
    
    /**
     * 
     * This function is kind of wired. Use <code>getProperty</code> instead.
     * 
     * TODO -> without getValue it returns a PropertiesObject which is not visible! 
     * 
     * @param key
     * @return 
     */
    public PropertiesObject get(String key) {
        if (this.contains(key)) {
            return (PropertiesObject) props.get(this.indexOf(key));
        } else
            return null;
    }
    
    public boolean store(FileOutputStream os, String text) {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        Iterator i = props.iterator();
        try {
            while (i.hasNext()) {
                PropertiesObject po = (PropertiesObject) i.next();
                if (po!=null) {
                    if (po.comment == null || po.comment.equals(""))
                            bw.write(po.key + "=" + po.value);
                    else
                        bw.write(po.comment);
                    
                    bw.newLine();
                }
            }
            bw.close();
            return true;
        } catch (IOException e) {
            logger.error("Couldn't write Outputstream");
            return false;
        }
    }
    
    
    /**
     * Replaces properties of the current object with properties contained in the source object
     * 
     * 
     * @param source
     * @return
     */
    public PropertiesWithOrder replaceExistingProperties(PropertiesWithOrder source) {
        Iterator propsI = props.iterator();
        Map toAdd = new HashMap();
        List toRemove = new ArrayList();
        while (propsI.hasNext()) {
            PropertiesObject po = (PropertiesObject) propsI.next();
            if (source.contains(po.getKey())) {
                int index = this.props.indexOf(po);
                toRemove.add(po);
                toAdd.put(new Integer(index), source.get(po.getKey()));
            }
        }
        
        props.removeAll(toRemove);
        
        Iterator i = toAdd.keySet().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            int index = ((Integer)o).intValue();
            if (this.props.size()>index)
            	this.props.add(index, toAdd.get(o));
            else
            	this.props.add(toAdd.get(o));
        }
        
        return this;        
    }
    
    
    
    public boolean contains(String key) {
        Iterator i = props.iterator();
        while (i.hasNext()) {
            PropertiesObject po = (PropertiesObject) i.next(); 
            if (po!=null && po.key!=null && po.key.equals(key))
                return true;
        }
        return false;
    }
    
    public boolean remove(String key) {
        Iterator i = props.iterator();
        int index=0;
        int toRemove=-1;
        while (i.hasNext()) {
            PropertiesObject po = (PropertiesObject) i.next(); 
            if (po.key!=null && po.key.equals(key))
                toRemove = index;
            index ++;
        }
        if (toRemove!=-1) {
            this.props.remove(toRemove);
            return true;
        } else 
            return false;
    }
    
    public int indexOf(String key) {
        Iterator i = props.iterator();
        int index=0;
        while (i.hasNext()) {
            PropertiesObject po = (PropertiesObject) i.next(); 
            if (po.key!=null && po.key.equals(key))
                return index;
            index ++;
        }
        return -1;
    }
    
    
    /* ----------------Properties Object ------------------------------*/ 
    
    private class PropertiesObject {
        private String key;
        
        private String value;
        
        public String comment; 
        
       

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
        public String toString() {
            if (this.comment==null || this.comment.equals("")) 
                return "PropertiesObject [" +this.key + "="+this.value+"]";
            else
                return "PropertiesObject [" +this.comment+"]";
        }
                
        
        
    }


    public PropertiesWithOrder addNonExistingElements(PropertiesWithOrder propsWOrder) {
        Iterator propsWOrderI = propsWOrder.props.iterator();
        ArrayList al = new ArrayList();
        while (propsWOrderI.hasNext()) {
            PropertiesObject po = (PropertiesObject) propsWOrderI.next();
            if (po.getKey()!=null && !contains(po.getKey())) {
                al.add(propsWOrder.get(po.getKey()));
            }
        }
        
        Iterator i = al.iterator();
        while (i.hasNext()) {
            this.props.add(i.next());
        }
        return this;       
    }


    public void put(String key, String value) {
        this.setProperty(key, value);
        
    }
    
    
    
}
