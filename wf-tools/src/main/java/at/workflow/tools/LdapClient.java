package at.workflow.tools;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LdapClient {
    
    public static final String C_ERROR_USERNAME_UNKNOWN = "Username not found in Directory!";
    public static final String C_ERROR_FAILED_LDAP_CONNECTION = "Connection to LDAP Server failed!";

    protected Log logger = LogFactory.getLog(this.getClass());
    
    private String ldapUser;
    private String ldapPassword;
    private String ldapProviderUrl;
    
    private boolean cutDomain;
    
    private String ldapSearchQuery="(|(cn={0})(mail={0})(uid={0}))";  // default value
    private boolean ldapSearchSubTree = true;  // default value
    private List<String> ldapBaseDn = new ArrayList<String>();
    
    
    /**
     * @param ldap_password The ldap_password to set.
     */
    public void setLdapPassword(String ldap_password) {
        this.ldapPassword = ldap_password;
    }
    /**
     * @param ldap_provider_url The ldap_provider_url to set.
     */
    public void setLdapProviderUrl(String ldap_provider_url) {
        this.ldapProviderUrl = ldap_provider_url;
    }
    /**
     * @param ldap_user The ldap_user to set.
     */
    public void setLdapUser(String ldap_user) {
        this.ldapUser = ldap_user;
    }
    /**
     * @param ldapBaseDn The ldapBaseDn to set.
     */
    public void setLdapBaseDn(List<String> ldapBaseDn) {
        this.ldapBaseDn = ldapBaseDn;
    }


    /**
     * @param ldapSearchQuery The ldapSearchQuery to set.
     */
    public void setLdapSearchQuery(String ldapSearchQuery) {
        this.ldapSearchQuery = ldapSearchQuery;
    }

    /**
     * @param searchSubTree The searchSubTree to set.
     */
    public void setLdapSearchSubTree(boolean searchSubTree) {
        this.ldapSearchSubTree = searchSubTree;
    }

    
    /**
     * searches Ldap with configured standard values for the specified user
     * 
     * @param username
     * @return Map of Ldap Attributes
     * @throws Exception in case User was not found
     */
    public Map<String,String> searchLdapEntries(String username) throws Exception {
        return searchLdapEntries(username, ldapProviderUrl, ldapUser, ldapPassword, ldapSearchQuery,
                ldapSearchSubTree, ldapBaseDn);
    }
    
    public static String removeDomainFromUserName(String userName) {
    	if (userName.indexOf("\\")==-1)
    		return userName;
    	
        String newUsername = userName.substring(userName.indexOf("\\"),userName.length());
        return newUsername.replaceAll("\\\\","");
    }
    
    /**
     * searches in all the configured ldap base dns with the specified 
     * ldap search query. the search query can have special placeholders where
     * the passed username string will be inserted.
     * 
     * f.i.  (&(cn={0})(uid={0}))
     * means that the passed username will replace the {0} String
     * this example query finds all ldap entries whose cn and uid matches the 
     * given username
     * 
     * @param queryterm
     * @param providerUrl
     * @param user
     * @param password
     * @param searchQuery
     * @param searchSubTree
     * @param baseDNs
     * @return a Map with all public Attributes of the LDAP object
     * @throws Exception
     */
    public Map<String,String> searchLdapEntries(String queryterm, String providerUrl, String user, String password,
            String searchQuery, boolean searchSubTree, List<String> baseDNs) throws Exception {
        
        Attributes attribs=null;
        DirContext ctx = null;
        boolean checkFinished = false;
        SearchResult sr = null;
        String u="";
        String msg="";
        int i=0;
        
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);

        // Authenticate
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.REFERRAL, "follow");
        
        // if list of base dns is empty or null
        // add emtpy entry
        if (baseDNs==null || baseDNs.size()==0) {
            baseDNs = new ArrayList<String>();
            baseDNs.add("");
        }
        
        while (i < baseDNs.size() && !checkFinished) {

            try {
                
                ctx = new InitialLdapContext(env,null);

                // object or subtree scope!
                SearchControls sc = new SearchControls();
                if (searchSubTree)
                    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
                else
                    sc.setSearchScope(SearchControls.OBJECT_SCOPE);
                
                sc.setDerefLinkFlag(true);
                
                // removes the domain, if cut domain = ture
                if ( cutDomain ) {
                    queryterm = removeDomainFromUserName(queryterm);
                }
                else if ( queryterm.indexOf("\\")!=-1 )
                    queryterm = queryterm.replaceAll("\\\\", "\\\\\\\\"); // einen Backslash durch 2 ersetzen
                
                // build search query
                String query = MessageFormat.format(searchQuery, new Object[] { queryterm});
                
                //  search for LDAP entry
                this.logger.debug("search for LDAP Entry with query=" + query + " and dc=" + baseDNs.get(i).toString());
                NamingEnumeration<SearchResult> answer = ctx.search(baseDNs.get(i).toString(),query, sc);
                
                if (answer.hasMore()) {
                    
                    // return only first answer!
                    sr = answer.next();
                    
                    if (this.logger.isDebugEnabled()) {
                    	debugOutputAttributes(sr);
                    }
    
                    // if basedns were used append to name
                    u = sr.getName();
                    if (!baseDNs.get(i).toString().equals(""))
                        u = u + "," + baseDNs.get(i).toString();
                    
                    attribs = sr.getAttributes();
                    checkFinished=true;
                }

            }  catch (CommunicationException comm_e) {
                msg = C_ERROR_FAILED_LDAP_CONNECTION;
                checkFinished = true;
                u = null;
            } catch (NoPermissionException noperm_e) {
                msg = C_ERROR_USERNAME_UNKNOWN;
                u = null;
            } catch (javax.naming.NameNotFoundException name_e) {
                this.logger.warn("Basedn " + baseDNs.get(i).toString() + " was not found on the ldap server!", name_e);
                u = null;
            } catch (Exception e) {
                this.logger.warn(e,e);
                if (e.getMessage()!=null) {
                	msg = e.getMessage().toString();
                } else {
                	msg = "";
                }
                u = null;
            }
            // check if successfull

            if (ctx != null) {              
                try {
                    ctx.close();
                } catch (NamingException e1) {
                }
            }
            i++;

        }
        if (u==null || u.equals("")) {
            if (msg==null || msg.equals(""))
                throw new Exception(C_ERROR_USERNAME_UNKNOWN);
            throw new Exception(msg);
        }

        return convertToMap(attribs);
    }
    
	private Map<String, String> convertToMap(Attributes attribs)
			throws NamingException {
		
		Map<String,String> personMap = new HashMap<String,String>();
        
        Enumeration<? extends Attribute> myEnum = attribs.getAll();
        while (myEnum.hasMoreElements()) {
            Attribute myAttr = myEnum.nextElement();
            personMap.put(myAttr.getID(), myAttr.get(0).toString());
        }
		return personMap;
	}

    private void debugOutputAttributes(SearchResult sr) {
		this.logger.debug("found result=" + sr);
		NamingEnumeration<? extends Attribute> na = sr.getAttributes().getAll(); 
		while (na.hasMoreElements()) {
			Attribute attr = na.nextElement();
			try {
				this.logger.debug(attr.getID() + "=" + attr.get());
			} catch (NamingException e) {
				this.logger.warn("problems while getting attribute " + attr.getID());
			}
		}
	}
    
    public void setCutDomain(boolean cutDomain) {
        this.cutDomain = cutDomain;
    }

	public boolean isCutDomain() {
		return cutDomain;
	}
	public List<String> getLdapBaseDn() {
		return ldapBaseDn;
	}
	public String getLdapPassword() {
		return ldapPassword;
	}
	public String getLdapProviderUrl() {
		return ldapProviderUrl;
	}
	public String getLdapSearchQuery() {
		return ldapSearchQuery;
	}
	public boolean isLdapSearchSubTree() {
		return ldapSearchSubTree;
	}
	public String getLdapUser() {
		return ldapUser;
	}


}




