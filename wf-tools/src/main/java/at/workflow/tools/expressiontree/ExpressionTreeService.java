package at.workflow.tools.expressiontree;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * FIXME: dependencies on TA -> should not be here!!!
 * 
 * 
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * created at:       Jun 12, 2008<br>
 * package:          at.workflow.webdesk.tools<br>
 * compilation unit: ExpressionTreeService.java<br><br>
 *
 * <p>
 * 
 * This class should become abstract and created via 
 * a Factory. 
 *
 */
public class ExpressionTreeService {
	
	private static Log logger = LogFactory.getLog(ExpressionTreeService.class);
	
	public static short AND_CONJUNCTION=0;
	public static short OR_CONJUNCTION=1;
	
	public static short CONJUNCTION;
	
	public static final short LEFT = 0;
    public static final short RIGHT = 1;
	
	private List operators;
    private List conjunctions;
    private List andConjunctions;
    private List orConjunctions;
	
    
    public ExpressionTreeService() {
	    operators = new ArrayList();
	    conjunctions = new ArrayList();
	    andConjunctions =  new ArrayList();
	    orConjunctions = new ArrayList();
	    // order is important !
	    operators.add("<=");
	    operators.add("==");
	    operators.add("!=");
	    operators.add(">=");
	    operators.add("<");
	    operators.add(">");
	    operators.add("=");
	    
	    // andConjunctions.add("AND");
	    // andConjunctions.add("and");
	    andConjunctions.add("&&");
	    
	    // orConjunctions.add("or");
	    // orConjunctions.add("OR");
	    orConjunctions.add("||");
	    
	    conjunctions.add(this.AND_CONJUNCTION,andConjunctions);
	    conjunctions.add(this.OR_CONJUNCTION, orConjunctions);
    }
    
    
    
    
    private boolean containsConjunction(String s) {
        boolean res = false;
        Iterator i = this.conjunctions.iterator();
        while (i.hasNext()) {
            Iterator j  = ((List) i.next()).iterator();
            while (j.hasNext()) 
                if (s.indexOf((String)j.next()) != -1)
                    res = true;
        }
        return res;
    }
    
    
    private boolean containsOperator(String s) {
        boolean res = false;
        Iterator i = this.operators.iterator();
        while (i.hasNext()) {
            if (s.indexOf((String)i.next())>-1)
                res = true;
        }
        return res;
    }
    
    private boolean isConjunction(String s) {
        Iterator i = conjunctions.iterator();
        while (i.hasNext()) {
            List cons = (List)i.next();
            if (cons.contains(s))
                return true;
        }
        return false;        
    }
    
    private void breakUp(String leftPart, String rightPart,ExpressionTree exp, Map strings) {
        ExpressionTree leftChild = new ExpressionTree();
        exp.setLeftChild(leftChild);
        leftChild.setParent(exp);
        if (leftPart!=null && !leftPart.equals(""))
            this.parseRec(leftPart,leftChild, true, strings);
        else throw new RuntimeException("Tree is not parsable.");
        ExpressionTree rightChild = new ExpressionTree();
        exp.setRightChild(rightChild);
        rightChild.setParent(exp);
        if (rightPart!=null && !rightPart.equals(""))
            this.parseRec(rightPart,rightChild, false, strings);
        else
            throw new RuntimeException("Tree is not parsable");
    }
    
    private ExpressionTree parseRec(String s,ExpressionTree tae, boolean left, Map strings) throws RuntimeException {
    	ExpressionStringTokenizer est = new ExpressionStringTokenizer(s," ");
        int braceCount = 0;
        String leftPart="";
        int counter = 0;
        boolean endReached = false;
        boolean firstBraceRemoved = false;
        
        ExpressionTreeNode node = new ExpressionTreeNode();
        while (est.hasMoreTokens() || !leftPart.equals("")) {
            counter ++;
            if (!est.hasMoreTokens()) {
                est = new ExpressionStringTokenizer(leftPart," ");
                leftPart = "";
                
                // if nothing could be extracted, try to remove braces (only each second time)
                if (!firstBraceRemoved) {
	                endReached = true;
	                firstBraceRemoved = false;
                } else {
                	firstBraceRemoved=false;
                	endReached=false;
                }
            }
            String token = est.nextToken();
            // System.out.println("Token:" +token);
            token = token.replaceAll("\"","");
            token = token.replaceAll("\'","");
            if (token.equals("("))
                braceCount ++;
            if (token.equals(")"))
                braceCount --;
            if (isConjunction(token) && braceCount==0) {
                // break up left an right part
                //logger.debug("conjuntion");
                node.setType(ExpressionTreeNode.CONJUNCTION);
                node.setValue(token);
                tae.setValue(node);
                    
                breakUp(leftPart,est.getTail(),tae, strings);
                break;
            }
            if (operators.contains(token) && braceCount==0) {
                if (!containsConjunction(est.getTail()) 
                ) {
                    // break up left and right part
                    //logger.debug("operator");    
                    node.setType(ExpressionTreeNode.OPERATOR);
                    node.setValue(token);
                    tae.setValue(node);
                    String regexp = token;
                    if (token.equals("||"))
                        regexp = "\\|\\|";
                    breakUp(leftPart,est.getTail().replaceFirst(regexp,""),tae, strings);    
                    break;
                }            
            }        
            

            
            if ( (token.matches("[\\w]+")||
                    token.matches("-[\\w]") || 
                    token.matches("[\\w]+,[\\w]+")||
                    token.matches("-[\\w]+,[\\w]+") || 
                    token.matches("[\\w]+[-]+[\\w]+") || 
                    token.matches("[\\w]+[_]+[\\w]+") ||
                    token.matches("[\\d]+") || token.matches("-[\\d]+") ||
                    token.matches("[\\d],[\\d]") ||
                    token.matches("-[\\d],[\\d]") ||
                    token.matches("[\\d].[\\d]") ||
                    token.matches("-[\\d].[\\d]")
                 )  && !left) {
                // variable
                //logger.debug("variable");
                if (braceCount==0) {
                    if (!containsOperator(est.getTail())) {
                        node.setType(ExpressionTreeNode.VALUE);
                        
                        boolean isString = false;
                        if (token.startsWith("ZZZZZ")) { 
                            token = (String) strings.get(token);
                            isString = true;
                        }
                        
                        if (token!=null && !token.equals("") && (getDate(token)!=null || token.equals("CURRENTDATE"))) {
                            node.setValueType(ExpressionTreeNode.DATEVALUE);
                            if (token.equals("CURRENTDATE"))
                            	node.setValue(new Date());
                            else
                            	node.setValue(getDate(token));
                        } else if (matchesDigit(token) &&!isString) {
                            node.setValueType(ExpressionTreeNode.NUMVALUE);
                            node.setValue(token.replaceAll(",", "."));
                        } else { 
                            node.setValueType(ExpressionTreeNode.STRINGVALUE);
                            node.setValue(token);
                        }

                        
                        tae.setValue(node);
                        break;
                   } 
                }
            }
            
            if (token.matches("[\\w]+\\.[\\w]+") && left
                    || (token.matches("[\\w]+") && left)
                    
            ) {
                // table
                //logger.debug("table");
                if (braceCount==0) {
                    if (!containsOperator(est.getTail())) {
                        node.setType(ExpressionTreeNode.COLUMN);
                        if (token.indexOf(".")!=-1) {
                            node.setTable(token.substring(0,token.indexOf(".")));
                            node.setValue(token.substring(token.indexOf(".")+1));
                        } else {
                            node.setTable("0");
                            node.setValue(token);
                        }
                            
                        tae.setValue(node);
                        break;
                    } 
                }
            }


            // check if braces should be attached or not (special case!) 
            if (!token.equals("(") && !token.equals(")")) 
                leftPart += " "+token;
            else {
                if (endReached) {
                	// remove first and last brace!
                	if (token.equals("(")) {
                		if (!firstBraceRemoved)
                			firstBraceRemoved =true;
                		else
                			leftPart+=" " +token;
                	} 
                	if (token.equals(")")) {
                		if (est.getTail().indexOf(")")>-1)
                			leftPart+=" "+token;
                	}
                	
                } else 
                	leftPart+=" " + token;
            }
            if (counter>1000)
                throw new RuntimeException("Tree is not parsable.");
        }
        return tae;
    }
    
    
    public boolean isBraceCountEqual(String s) {
    	int open=0,close =0;
    	for (int i=0; i<s.length(); i++) {
    		if (s.charAt(i)=='(')
    			open++;
    		if (s.charAt(i)==')')
    			open++;
    	}
    	if (open==close)
    		return true;
    	else
    		return false;
    }
    
    public boolean matchesDigit(String token) {
        if (token.matches("[\\d]+") || token.matches("-[\\d]+") ||
                token.matches("[\\d]+,[\\d]+") ||
                token.matches("-[\\d]+,[\\d]+")||
                token.matches("[\\d]+.[\\d]+") ||
                token.matches("-[\\d]+.[\\d]+"))
            return true;
        else 
            return false;
    }


    /**
     * @param token a String object that should represent a date. (dd.MM.yyyy)
     * @return a IfDate object if the token represents a date, null otherwise.
     */
    private Date getDate(String token) {
        StringTokenizer st = new StringTokenizer(token,".");
        Calendar c= Calendar.getInstance();
        try {
            while (st.hasMoreTokens()) {
                c.set(Calendar.DAY_OF_MONTH, new Integer(st.nextToken()).intValue());
                c.set(Calendar.MONTH, new Integer(st.nextToken()).intValue()-1);
                c.set(Calendar.YEAR, new Integer(st.nextToken()).intValue());
            }
        } catch (Exception e) {
            return null;
        }
        return c.getTime();
    }


    /**
     * This function fills the ExpressionTree object if a parsable<br/>
     * string is passed.<br/>
     * <br/>
     * The following definitions applies:<br/>
     * <br/>
     * <b>column:</b> tablename.column where<br/>
     * tablename is an integer (see ExpressionTreeNode for more information)<br>
     * <b>value:</b> value <br/>
     * <b>operator:</b> one of the given operators.<br/>
     * <b>conjunction:</b> && , || 
     * <br/>
     * <strike>The different nodes have to be seperated by one or more whitespaces.
     * </strike>
     * <br/>
     * <br/> 
     * <b>Example of a parsable expression:</b><br/>
     * (tgwert.twwpnr < 5 && tgwert.twwsnr >= 3)
     * 
     * @param expression
     */
    public ExpressionTree parseString(String expression) throws RuntimeException {
         List specialStrings = new ArrayList();
         specialStrings.add("(");
         specialStrings.add(")");
         // problem with operators < <= > >= -> <= is splittet! -> < = 
         specialStrings.addAll(operators);
         specialStrings.addAll(andConjunctions);
         specialStrings.addAll(orConjunctions);
         
         expression = expression.replaceAll(" AND "," && ");
         expression = expression.replaceAll(" and "," && ");
         expression = expression.replaceAll(" OR ", " || ");
         expression = expression.replaceAll(" or ", " || ");
         expression = expression.replaceAll("<>", "!=");
         expression = expression.replaceAll("==", "=");
         
         
         Map strings = new HashMap();
         expression = insertWhiteSpaces(specialStrings,expression, strings);
         
         expression = expression.replaceAll("<\\s+=", "<=");
         expression = expression.replaceAll(">\\s+=", ">=");
         expression = expression.replaceAll("=\\s+=","==");
         expression = expression.replaceAll("!\\s+=","!=");
         
         expression = expression.replaceAll("\\s+", " ");
         
         ExpressionTree et = new ExpressionTree();
         return parseRec(expression,et, true, strings);
    }
    
    private String insertWhiteSpaces(List l, String expression, Map strings) {
        int index = 0;
        String tempExpression = expression;
        int counter = 0;
        
        
        // fill strings map 
        while ( (index=tempExpression.indexOf("'")) !=-1) {
            String s = tempExpression.substring(index+1);
            if (s.indexOf("'")==-1)
                throw new RuntimeException("Tree is not parseable. ' count is odd.");
            int secondIndex = s.indexOf("'");
            s = s.substring(0, secondIndex);
            tempExpression = tempExpression.substring(index+secondIndex+2);
            strings.put("ZZZZZZ" + counter, s);
            counter++;
        }
        
        counter = 0;
        tempExpression = "";
        while ( (index=expression.indexOf("'")) !=-1) {
            tempExpression += expression.substring(0,index);
            expression = expression.substring(index+1);
            
            if (expression.indexOf("'")==-1)
                throw new RuntimeException("Tree is not parseable. ' count is odd.");
            
            int secondIndex = expression.indexOf("'");
            tempExpression += "ZZZZZZ" + counter;
            counter ++;
            expression = expression.substring(secondIndex+1);

        }
        if (expression.length()>0)
            tempExpression+=expression;
        
        expression = tempExpression;
        
        counter = 0;
        while (expression.matches(".*\'.+\'.*")) { 
            expression = expression.replaceFirst("\'.+\'","ZZZZZZ"+counter);
            counter ++;
        }
        
        Iterator i = l.iterator();
        while (i.hasNext()) {
            // i holds operators, and/or conjunctions, braces
            String str = (String) i.next();
            StringBuffer sb = new StringBuffer();
            // some of them has special meanings when used as regular expressions, thus an escape character
            // is added when appropriate
            char[] ca = str.toCharArray();
            for (int j =0; j<str.length(); j++)
                sb.append("\\"+ ca[j]);
            if (!str.matches("[\\w]+"))
                expression= expression.replaceAll(sb.toString(), " " + str + " ");
            else
                expression = expression.replaceAll(str," " + str+ " " );
        }
        
        return expression;
    }
    
    private String insertStrings(List strings, String expression) {
        Iterator stringsI = strings.iterator();
        while (stringsI.hasNext()) {
            String replaceMent = (String) stringsI.next();
            expression = expression.replaceFirst("ZZZZZZ","'" + replaceMent+"'");
        }
        
        expression = expression.replaceAll("\\s+"," ");
        return expression;
    }
    
    
    public void printPrefix(ExpressionTree et) {
        this.printPrefixRec(et);
    }

    private void printPrefixRec(ExpressionTree et) {
        if (et.getValue()!=null) {
            if (et.getValue().getTable()!=null && !et.getValue().getTable().equals("0"))
                System.out.print(et.getValue().getTable()+ ".");
            System.out.print(et.getValue().getValue().toString()+" ");
        }            
        if (et.getLeftChild()!=null)
        	printPrefixRec(et.getLeftChild());
        if (et.getRightChild()!=null)
        	printPrefixRec(et.getRightChild());
    }
    
    
    
    public String getPrefix(ExpressionTree et) {
    	return getPrefixRec("", et);
    }
    
    private String getPrefixRec(String s,ExpressionTree et) {
        if (et.getValue()!=null) {
            if (et.getValue().getTable()!=null && !et.getValue().getTable().equals("0"))
                s+=et.getValue().getTable()+ ".";
            s+=et.getValue().getValue().toString()+" ";
        }            
        if (et.getLeftChild()!=null)
            s= getPrefixRec(s,et.getLeftChild());
        if (et.getRightChild()!=null)
            s = getPrefixRec(s,et.getRightChild());
        return s;
    }
    
    
    private String getBraceForActNode(int actNode) {
        switch (actNode) {
        case 0: return ""; // node
        case 1: return "("; // leaf (left side)
        case 2: return ")"; // leaf (right side)
        default: return "";
        }
        
    }



	public List getConjunctions() {
		return conjunctions;
	}
    
    
    /**
     * Traverses through the whole tree and returns all column Entries
     * 
     * 
     * @param et an EpressionTree Object
     * @param l an empty Arraylist (After the function is finished, the list contains the 
     * same values as the Return List.
     * @return a list of Objects
     */
    public List getAllColumns(ExpressionTree et, List l) {
        ExpressionTreeNode etn = et.getValue();
        if (etn.getType()==etn.COLUMN)
            l.add(etn.getValue());
        if (et.getRightChild()!=null)
            l = getAllColumns(et.getRightChild(),l);
        if (et.getLeftChild()!=null)
            l = getAllColumns(et.getLeftChild(),l);
        return l;
    }
    
	public int countConjunctionsOfSubTree(ExpressionTree node, short direction, short type) {
		int sum=0;
		ExpressionTree actNode = node;
		
		boolean run = true;
		while (run) {
		
			if (actNode.getParent()!=null) {
				ExpressionTree parent = actNode.getParent();
				ExpressionTree leftChild = parent.getLeftChild();
				ExpressionTree rightChild = parent.getRightChild();
				switch (direction) {
				case ExpressionTreeService.LEFT:
					if (isType(actNode.getValue(), type))
						if (isAndConjunction(actNode.getLeftChild().getValue()) || 
							isOrConjunction(actNode.getLeftChild().getValue()))
							sum++;
				
					if (actNode != leftChild) 
						run = false;
					
					actNode=actNode.getParent();
					break;
				case ExpressionTreeService.RIGHT:
					if (isType(actNode.getValue(), type)) {
						if (isAndConjunction(actNode.getLeftChild().getValue()) ||
								isOrConjunction(actNode.getLeftChild().getValue()))
							sum++;
					}	
					if (actNode != rightChild) 
						run = false;
					actNode = actNode.getParent();
					
					break;
					
					default:
						throw new RuntimeException("direction is not set.");
				}
			} else 
				run = false;
			
		}
		
		return sum;
	}
	
	
	
	private boolean isType(ExpressionTreeNode value, short type) {
		if (type==this.AND_CONJUNCTION)
			if (isAndConjunction(value))
				return true;
		if (type==this.OR_CONJUNCTION)
			if (isOrConjunction(value))
				return true;
		return false;
	}



	
	
	/**
	 * @param et
	 * @param res
	 * @param nameOfMap
	 * @param depth
	 * @param conjunctions
	 * @return
	 * 
	 * 
	 * @deprecated -> this will be replaced with the template method pattern
	 */
	public int createJSFromExpressionTree(ExpressionTree et, StringBuffer res, String nameOfMap, int depth, int conjunctions) {

		String jsDateFormat = "yyyy,MM,dd";
		String shortDF = "dd.MM.yy";
		SimpleDateFormat shortSdf = new SimpleDateFormat(shortDF);
		SimpleDateFormat jsSdf = new SimpleDateFormat(jsDateFormat);
		
        if (et.getLeftChild()!=null) 
            depth = createJSFromExpressionTree(et.getLeftChild(), res,nameOfMap, ++depth,conjunctions);
        ExpressionTreeNode value = et.getValue();
        switch (value.getType()) {
        case ExpressionTreeNode.COLUMN:
           if (depth>1) {
               //for (int i=1; i<depth; i++)
                 //  res.append("(");
           }
           Object extractedValue =value.getValue();
           if (value.getValueType()==ExpressionTreeNode.DATEVALUE && extractedValue!=null && !extractedValue.equals("")) {
               Date date = null;
               try {
            	   date = shortSdf.parse((String) extractedValue);
                   //date = new IfDate((String) extractedValue,IfDate.IFDATEFORMATCH);
                   //date.add(Calendar.MONTH, -1); // Calendar counts months from 0
               } catch (ParseException e) {
                   e.printStackTrace();
               }
               if (date!=null )
                   res.append("new Date("+ jsSdf.format(date)+")");
               else {
                   res.append(nameOfMap+".get('"+extractedValue+"')");
                   logger.warn("could not parse date");
               }
               
           
           } else 
               res.append(nameOfMap+".get('"+extractedValue+"')");
           break;
           
        case ExpressionTreeNode.OPERATOR:
            if (value.getValue().equals("="))
                res.append(" == ");
            else
                res.append(" " +value.getValue()+ " ");
            break;
        case ExpressionTreeNode.CONJUNCTION:
            conjunctions++;
            if (isAndConjunction(value))
                res.append(" && ");
            if (((List)this.conjunctions.get(OR_CONJUNCTION)).contains(value.getValue()))
                res.append(" || "); 
            break;
        case ExpressionTreeNode.VALUE:
            if (value.getValueType()==ExpressionTreeNode.DATEVALUE) {
                res.append("new Date("+jsSdf.format((Date)value.getValue())+")");
            } else {
                
                String val = (String) value.getValue();
                if (val.matches("[\\d]+") && value.getValueType()!=ExpressionTreeNode.STRINGVALUE) {
                	
                	// remove leading zeros
                	while (val.length()>1 && val.startsWith("0"))
                		val = val.substring(1, val.length());
                	
                    res.append(val);
                } else {
                    res.append("'"+val+"'");
                }
                
                if (conjunctions>0) { 
                    if (depth>1 && conjunctions>0) {
                        //res.append(")");
                    }
                    conjunctions--;
                }
            }
            break;
        }
        if (et.getRightChild()!=null) 
            depth = createJSFromExpressionTree(et.getRightChild(),res,nameOfMap,++depth,conjunctions);
        depth --;
        return depth;
        
    }
	
	
	
	/**
	 * @param value
	 * @return true if the node value is an AND-Conjunction
	 */
	public boolean isAndConjunction(ExpressionTreeNode value) {
		
		if (((List)this.conjunctions.get(AND_CONJUNCTION)).contains(value.getValue()))
			return true;
		else
			return false;
	}

	/**
	 * @param value
	 * @return true if the node value is an OR-Conjunction
	 */
	public boolean isOrConjunction(ExpressionTreeNode value) {
		if (((List)this.conjunctions.get(OR_CONJUNCTION)).contains(value.getValue()))
			return true;
		else
			return false;
	}




	/**
	 * 
	 * This one is working 
	 * 
	 * 
	 * template for template method ! 
	 * 
	 * @param et
	 * @param personName
	 * @param res
	 * @param parameters
	 * @param depth
	 * @param conjunctions
	 * @param person
	 * @return
	 */
	public int createJSFromExpressionTree(ExpressionTree et, String personName, StringBuffer res, 
			List parameters,int depth, int conjunctions, Map values) {
		
		String shortDF = "dd.MM.yy";
//		String jsDF = "dd.MM.yy"; requires ' and even than does not work
		String jsDF = "yyyy,MM,dd";
		SimpleDateFormat shortSdf = new SimpleDateFormat(shortDF);
		SimpleDateFormat jsSdf = new SimpleDateFormat(jsDF);

        if (et.getLeftChild()!=null) {
            if (et.getValue().getType()==ExpressionTreeNode.CONJUNCTION)
            	conjunctions++;
        	conjunctions = createJSFromExpressionTree(et.getLeftChild(), personName, 
        			res,parameters,++depth,conjunctions,values);
        }
        ExpressionTreeNode value = et.getValue();
        switch (value.getType()) {
        case ExpressionTreeNode.COLUMN:
        	
           conjunctions=0;
           Object extractedValue = values.get(value.getValue());
           
           
           // for each OR conjunction of the "left-side sub-tree" (we're here at a child of this subtree)
           // a opening brace is needed
           
           if (logger.isDebugEnabled()) 
	           if (et.getParent()!=null && et.getParent().getParent()!=null)
	        	   logger.info(et.getParent().getParent().getValue().getValue());
           
           
   		   int sum = countConjunctionsOfSubTree(et, ExpressionTreeService.LEFT, OR_CONJUNCTION);	
   		   for (int i =0; i<sum; i++) 
   			   res.append("(");
           
           if (extractedValue!=null && !extractedValue.equals("") && et.getParent()!=null && et.getParent().getLeftChild()!=null && 
        		   et.getParent().getLeftChild().getValue()!=null && et.getParent().getRightChild().getValue().getValueType()==ExpressionTreeNode.DATEVALUE) {
               Date date = null;
               try {
                   date = shortSdf.parse((String)extractedValue);
                   //date = new IfDate((String) extractedValue,IfDate.IFDATEFORMATCH);
                   //date.add(Calendar.MONTH, -1); // Calendar counts months from 0
               } catch (ParseException e) {
                   e.printStackTrace();
               }
               if (date!=null )
                   res.append("new Date("+ jsSdf.format(date)+")");
               else {
                   res.append(personName + ".extraFieldValues.get('"+value.getValue()+"')");
                   logger.warn("could not parse date");
               }
               
           
           } else 
               res.append(personName + ".extraFieldValues.get('"+value.getValue()+"')");
           parameters.add(value.getValue());
           break;
           
        case ExpressionTreeNode.OPERATOR:
            if (value.getValue().equals("="))
                res.append(" == ");
            else
                res.append(" " +value.getValue()+ " ");
            break;
        case ExpressionTreeNode.CONJUNCTION:
            if (isAndConjunction(value))
                res.append(" && ");
            if (isOrConjunction(value))
                res.append(" || "); 
            break;
        case ExpressionTreeNode.VALUE:
        	
        		
    			if (value.getValueType()==ExpressionTreeNode.DATEVALUE) {
                    res.append("new Date(" + (jsSdf.format((Date)value.getValue()) ) + ")");
                } else {
                    
                    String val = (String) value.getValue();
                    if (value.getValueType()==ExpressionTreeNode.NUMVALUE) {
                    	
                    	// remove leading zeros
                    	while (val.length()>1 && val.startsWith("0"))
                    		val = val.substring(1, val.length());
                    	
                        res.append(val);
                    } else {
                        res.append("'"+val+"'");
                    }
                }	
    			
    			
    			
    			sum = countConjunctionsOfSubTree(et, ExpressionTreeService.RIGHT, OR_CONJUNCTION);	
    			for (int i =0; i<sum; i++) 
	   			   res.append(")");
        			
    		
        	break;
        }
        if (et.getRightChild()!=null) {
        	/*
        	if (value.getType()==ExpressionTreeNode.CONJUNCTION) {
            	conjunctions--;
            	res.append("(");
            }
        	*/
            conjunctions = createJSFromExpressionTree(et.getRightChild(),personName,res,parameters,++depth,conjunctions,values);
        }
        
        return conjunctions;
        
    }
	
	public String[] getAccountDefinitions(ExpressionTree et) {
		Iterator columnsI = this.getAllColumns(et, new ArrayList()).iterator();
		Set res = new HashSet();
		while (columnsI.hasNext()) {
			String uniqueId = ((String) columnsI.next()).substring(0,1); 
			res.add(uniqueId);
		}
		return (String[]) res.toArray();
	}
	

}
