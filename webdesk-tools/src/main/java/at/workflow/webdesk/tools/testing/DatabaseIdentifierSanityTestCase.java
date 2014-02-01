package at.workflow.webdesk.tools.testing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.ClassUtils;
import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.TextUtils;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataHibernate;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataUtil;

/**
 * Database identifier sanity checks:
 * (1) no identifier must be longer than 30 chars,
 * (2) none must be a keyword.
 * To use this for a module, just create an empty WTestDatabaseIdentifierSanity class
 * somewhere in the src/test/java directory, and derive the new class from this one.
 * 
 * TODO: did not find PoActionAttributes table UID. PoActionAttributes is nested inside PoAction.hbm.xml as a &lt;map> element.
 * TODO: refactor code duplications in error collecting and reporting.
 * 
 * @author fritzberger 26.11.2012
 */
public class DatabaseIdentifierSanityTestCase extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final int MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER = 30;
	private PersistenceMetadata metaData;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		metaData = new PersistenceMetadataHibernate();
	}

	/** Check that all adder methods in all persistence POJOs are named correctly, e.g. jobFamilies - addJobFamily(), refer to TextUtils.getSingularFirstCharUpperCasePropertyName(). */
	public void testAdderMethods()	{
		String errorMessage = "";
		int errorCount = 0;
		
		for (Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			final int currentErrorCount = errorCount;
			
			for (BeanReflectUtil.BeanProperty property : BeanReflectUtil.properties(clazz))	{
				if (ClassUtils.isCollection(property.propertyClass))	{
					final String collectionType = metaData.getCollectionType(clazz, property.propertyName);
					if (collectionType == null)
						continue;	// TODO: fri_2013-05-13: Map class can not be recognized in PersistenceMetadata currently
					
					final String adderName = ReflectionUtils.getAdderMethodName(property.propertyName);
					try {
						clazz.getMethod(adderName, Class.forName(collectionType));
					}
					catch (NoSuchMethodException e) {
						final String realMethodName;
						if ((realMethodName = hasSimilarMethod(clazz, adderName)) != null)	{
							final String displayCollectionType = collectionType.substring(collectionType.lastIndexOf(".") + 1);
							errorMessage = appendToMessage(errorMessage, realMethodName+"("+displayCollectionType+")");
							errorCount++;
						}
					}
					catch (Exception e)	{
						throw new RuntimeException(e);
					}
				}
			}
			
			if (currentErrorCount != errorCount)	{
				errorMessage = appendToMessage(errorMessage, "found in "+clazz.getSimpleName());
			}
		}
		
		assertTrue("There are "+errorCount+" wrong adder methods: "+errorMessage, errorCount <= 0);
	}

	private String hasSimilarMethod(Class<? extends PersistentObject> clazz, String adderName) {
		// cut off any trailing "y" (family - families)
		if (adderName.endsWith("y"))
			adderName = adderName.substring(0, adderName.length() - 1);
		
		adderName = adderName.substring("add".length());	// cut off leading "add"
		
		for (final Method m : clazz.getMethods())
			if (m.getName().startsWith("add"))
				if (adderName.equals(ReflectionUtils.getAdderMethodName(m.getName())))
					return m.getName();
		
		return null;	// no such method, this can happen and is no error
	}

	/** Check that no table name is longer than 30 characters. */
	public void testTableNames()	{
		String errorMessage = "";
		int errorCount = 0;
		final Map<String,Set<Class<? extends PersistentObject>>> done = new HashMap<String,Set<Class<? extends PersistentObject>>>();
		
		for (Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			final String tableName = metaData.getTableName(clazz);
			if (tableName.length() > MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER)	{
				errorCount++;
				errorMessage = appendToMessage(errorMessage, tableName+" ("+tableName.length()+")");
			}
			
			Set<Class<? extends PersistentObject>> otherClassesOfThatTable = done.get(tableName);
			if (otherClassesOfThatTable == null)
				done.put(tableName, otherClassesOfThatTable = new HashSet<Class<? extends PersistentObject>>());
			
			if (otherClassesOfThatTable.size() > 0 && havingCommonSuperclass(otherClassesOfThatTable, clazz) == false)	{
				errorCount++;
				errorMessage = appendToMessage(errorMessage, tableName+" (name already used: "+otherClassesOfThatTable+")");
			}
			otherClassesOfThatTable.add(clazz);
		}
		
		assertTrue("There are "+errorCount+" database table names either duplicate or longer than "+MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER+" characters: "+errorMessage, errorCount <= 0);
	}

	/** Check that no table property name is longer than 30 characters. */
	public void testAttributeNames()	{
		String errorMessage = "";
		int errorCount = 0;
		final Set<String> done = new HashSet<String>();
		
		for (Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			for (String propertyName : propertyNames(clazz))	{
				try	{
					final String attributeName = metaData.getAttributeName(clazz, propertyName);
					final String fullAttributeName = metaData.getTableName(clazz)+"."+attributeName;
					
					if (attributeName.length() > MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER)	{
						if (done.contains(fullAttributeName) == false)	{
							done.add(fullAttributeName);
							errorCount++;
							errorMessage = appendToMessage(errorMessage, fullAttributeName+" ("+attributeName.length()+")");
						}
					}
				}
				catch (IllegalArgumentException e)	{
					logger.warn(e.getMessage());
				}
			}
		}
		
		assertTrue("There are "+errorCount+" database attribute names longer than "+MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER+" characters: "+errorMessage, errorCount <= 0);
	}

	/** Check that no table name is longer than 30 characters. */
	public void testForeignKeyConstraintNames()	{
		String errorMessage = "";
		int errorCount = 0;
		final Map<String, Set<String>> done = new HashMap<String,Set<String>>();
		
		for (final Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			final String tableName = metaData.getTableName(clazz);
			
			for (final String foreignKeyConstraintName : metaData.getForeignKeyConstraintNames(clazz))	{
				String errorMessageName = tableName+"."+foreignKeyConstraintName;
				
				if (foreignKeyConstraintName.length() > MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER)	{
					errorCount++;
					errorMessage = appendToMessage(errorMessage, errorMessageName+" ("+foreignKeyConstraintName.length()+")");
				}
				
				errorMessageName = clazz.getSimpleName()+"."+foreignKeyConstraintName;
				
				Set<String> tablesOfThatConstraint = done.get(foreignKeyConstraintName);
				if (tablesOfThatConstraint == null)
					done.put(foreignKeyConstraintName, tablesOfThatConstraint = new HashSet<String>());
				
				if (tablesOfThatConstraint.contains(tableName) == false)	{
					if (tablesOfThatConstraint.size() > 0)	{
						errorCount++;
						errorMessage = appendToMessage(errorMessage, errorMessageName+" (name already used: "+tablesOfThatConstraint+")");
					}
					tablesOfThatConstraint.add(tableName);
				}
			}
		}
		
		assertTrue("There are "+errorCount+" foreign key constraint names either duplicate or longer than "+MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER+" characters: "+errorMessage, errorCount <= 0);
	}

	public void testIndexNames()	{
		String errorMessage = "";
		int errorCount = 0;
		final Map<String,Set<String>> done = new HashMap<String,Set<String>>();
		
		for (final Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			for (final String indexName : metaData.getIndexNames(clazz))	{
				final String tableName = metaData.getTableName(clazz);
				String errorMessageName = tableName+"."+indexName;
				
				if (indexName.length() > MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER)	{
					errorCount++;
					errorMessage = appendToMessage(errorMessage, errorMessageName+" ("+indexName.length()+")");
				}
				
				errorMessageName = clazz.getSimpleName()+"."+indexName;
				
				Set<String> tablesOfThatConstraint = done.get(indexName);
				if (tablesOfThatConstraint == null)
					done.put(indexName, tablesOfThatConstraint = new HashSet<String>());
				
				if (tablesOfThatConstraint.contains(tableName) == false)	{
					if (tablesOfThatConstraint.size() > 0)	{
						errorCount++;
						errorMessage = appendToMessage(errorMessage, errorMessageName+" (name already used: "+tablesOfThatConstraint+")");
					}
					tablesOfThatConstraint.add(tableName);
				}
			}
		}
		
		assertTrue("There are "+errorCount+" index names either duplicate or longer than "+MAXIMUM_CHARACTERS_OF_DATABASE_IDENTIFIER+" characters: "+errorMessage, errorCount <= 0);
	}

	/** Check that no table property name is longer than 30 characters. */
	public void testNoIdentifierIsAKeyword()	{
		String errorMessage = "";
		int errorCount = 0;
		
		for (Class<? extends PersistentObject> clazz : metaData.getMappedClasses())	{
			final String tableName = metaData.getTableName(clazz);
			if (isKeyword(tableName))	{
				errorCount++;
				errorMessage = appendToMessage(errorMessage, tableName);
			}
			
			for (String propertyName : propertyNames(clazz))	{
				try	{
					final String attributeName = metaData.getAttributeName(clazz, propertyName);
					
					if (isKeyword(attributeName))	{
						errorCount++;
						errorMessage = appendToMessage(errorMessage, metaData.getTableName(clazz)+"."+attributeName);
					}
				}
				catch (IllegalArgumentException e)	{
					logger.warn(e.getMessage());
				}
			}
			
			for (String foreignKeyConstraintName : metaData.getForeignKeyConstraintNames(clazz))	{
				if (isKeyword(foreignKeyConstraintName))	{
					errorCount++;
					errorMessage = appendToMessage(errorMessage, metaData.getTableName(clazz)+"."+foreignKeyConstraintName);
				}
			}
			
			for (String indexName : metaData.getIndexNames(clazz))	{
				if (isKeyword(indexName))	{
					errorCount++;
					errorMessage = appendToMessage(errorMessage, metaData.getTableName(clazz)+"."+indexName);
				}
			}
		}
		
		assertTrue(""+errorCount+" database identifers are SQL keywords: "+errorMessage, errorCount <= 0);
	}

	private Iterable<String> propertyNames(Class<? extends PersistentObject> clazz) {
		List<BeanReflectUtil.BeanProperty> persistentProperties = PersistenceMetadataUtil.getPersistentProperties(clazz, null, metaData, true);
		List<BeanReflectUtil.BeanProperty> nonCollectionProperties = PersistenceMetadataUtil.getNonCollectionProperties(persistentProperties);
		List<String> propertyNames = new ArrayList<String>(nonCollectionProperties.size());
		for (BeanReflectUtil.BeanProperty beanProperty : nonCollectionProperties)
			propertyNames.add(beanProperty.propertyName);
		return propertyNames;
	}

	private String appendToMessage(String errorMessage, String message)	{
		if (errorMessage.length() > 0)
			errorMessage += ", ";
		return errorMessage + message;
	}

	private boolean isKeyword(String identifier)	{
		return reservedWords.contains(identifier.toUpperCase());
	}
	
//	private boolean isSuperClassOfOneOf(Set<Class<? extends PersistentObject>> otherClassesOfThatTable, Class<? extends PersistentObject> classOfThatTable) {
//		// read super-classes of current given class
//		final List<Class<?>> superClasses = getRelevantSuperclasses(classOfThatTable);
//		
//		// remove super-classes from others
//		for (Class<? extends PersistentObject> otherClassOfThatTable : new HashSet<Class<? extends PersistentObject>>(otherClassesOfThatTable))	// clone the Set because there might be removes from the original one
//			if (superClasses.contains(otherClassOfThatTable))
//				otherClassesOfThatTable.remove(otherClassOfThatTable);	// remove any super-class
//	
//		// check if current given class is a super-class of one of others
//		for (Class<? extends PersistentObject> otherClassOfThatTable : new HashSet<Class<? extends PersistentObject>>(otherClassesOfThatTable))
//			if (getRelevantSuperclasses(otherClassOfThatTable).contains(classOfThatTable))	// is a super-class
//				return true;
//		
//		return false;
//	}
	
	private boolean havingCommonSuperclass(Set<Class<? extends PersistentObject>> otherClassesOfThatTable, Class<? extends PersistentObject> classOfThatTable) {
		final List<Class<?>> superClasses = getRelevantSuperclasses(classOfThatTable);
		superClasses.add(classOfThatTable);
		
		for (Class<? extends PersistentObject> otherClassOfThatTable : otherClassesOfThatTable)	{
			final List<Class<?>> otherSuperClasses = getRelevantSuperclasses(otherClassOfThatTable);
			otherSuperClasses.add(otherClassOfThatTable);
			
			for (Class<?> superClass : superClasses)
				if (otherSuperClasses.contains(superClass))
					return true;
		}
		return false;
	}

	/** @return the list of super-classes, excluding given class itself. */
	private List<Class<?>> getRelevantSuperclasses(Class<? extends PersistentObject> clazz) {
		List<Class<?>> superClasses = new ArrayList<Class<?>>();
		Class<?> superClass = clazz.getSuperclass();
		while (superClass != null && superClass.getSimpleName().equals("PoBase") == false && superClass.getSimpleName().equals("PoHistorization") == false)	{
			// sorry, having no dependency to PoBase and PoHistorization here
			superClasses.add(superClass);
			superClass = superClass.getSuperclass();
		}
		return superClasses;
	}

	
	private static final Set<String> reservedWords = new HashSet<String>();
	
	static	{
		
		// This is a keyword merge from:
		// SQL 92, 99 and 2003, see http://developer.mimer.se/validator/sql-reserved-words.tml
		// Microsoft SQLServer, see http://msdn.microsoft.com/en-us/library/aa238507(v=sql.80).aspx
		// ORACLE, see http://docs.oracle.com/cd/B19306_01/em.102/b40103/app_oracle_reserved_words.htm
		// DB2, see http://publib.boulder.ibm.com/infocenter/dzichelp/v2r2/index.jsp?topic=%2Fcom.ibm.db2z10.doc.sqlref%2Fsrc%2Ftpc%2Fdb2z_reservedwords.htm
		// MySQL, see https://dev.mysql.com/doc/refman/5.5/en/reserved-words.html
		// Postgres, see http://www.postgresql.org/docs/7.3/static/sql-keywords-appendix.html
		
		reservedWords.add("ABSOLUTE");
		reservedWords.add("ACCESS");
		reservedWords.add("ACCESSIBLE");
		reservedWords.add("ACCOUNT");
		reservedWords.add("ACTION");
		reservedWords.add("ACTIVATE");
		reservedWords.add("ADA");
		reservedWords.add("ADD");
		reservedWords.add("ADMIN");
		reservedWords.add("ADVISE");
		reservedWords.add("AFTER");
		reservedWords.add("ALL");
		reservedWords.add("ALLOCATE");
		reservedWords.add("ALLOW");
		reservedWords.add("ALL_ROWS");
		reservedWords.add("ALTER");
		reservedWords.add("ANALYZE");
		reservedWords.add("ANALYSE");
		reservedWords.add("AND");
		reservedWords.add("ANY");
		reservedWords.add("ARCHIVE");
		reservedWords.add("ARCHIVELOG");
		reservedWords.add("ARE");
		reservedWords.add("ARRAY");
		reservedWords.add("AS");
		reservedWords.add("ASC");
		reservedWords.add("ASENSITIVE");
		reservedWords.add("ASSERTION");
		reservedWords.add("ASSOCIATE");
		reservedWords.add("ASUTIME");
		reservedWords.add("ASYMMETRIC");
		reservedWords.add("AT");
		reservedWords.add("ATOMIC");
		reservedWords.add("AUDIT");
		reservedWords.add("AUTHENTICATED");
		reservedWords.add("AUTHORIZATION");
		reservedWords.add("AUTOEXTEND");
		reservedWords.add("AUTOMATIC");
		reservedWords.add("AUX");
		reservedWords.add("AUXILIARY");
		reservedWords.add("AVG");
		reservedWords.add("BACKUP");
		reservedWords.add("BECOME");
		reservedWords.add("BEFORE");
		reservedWords.add("BEGIN");
		reservedWords.add("BETWEEN");
		reservedWords.add("BFILE");
		reservedWords.add("BIGINT");
		reservedWords.add("BINARY");
		reservedWords.add("BIT");
		reservedWords.add("BITMAP");
		reservedWords.add("BIT_LENGTH");
		reservedWords.add("BLOB");
		reservedWords.add("BLOCK");
		reservedWords.add("BODY");
		reservedWords.add("BOOLEAN");
		reservedWords.add("BOTH");
		reservedWords.add("BREADTH");
		reservedWords.add("BUFFERPOOL");
		reservedWords.add("BY");
		reservedWords.add("CACHE");
		reservedWords.add("CACHE_INSTANCES");
		reservedWords.add("CALL");
		reservedWords.add("CALLED");
		reservedWords.add("CANCEL");
		reservedWords.add("CAPTURE");
		reservedWords.add("CASCADE");
		reservedWords.add("CASCADED");
		reservedWords.add("CASE");
		reservedWords.add("CAST");
		reservedWords.add("CATALOG");
		reservedWords.add("CCSID");
		reservedWords.add("CFILE");
		reservedWords.add("CHAINED");
		reservedWords.add("CHANGE");
		reservedWords.add("CHAR");
		reservedWords.add("CHARACTER");
		reservedWords.add("CHARACTER_LENGTH");
		reservedWords.add("CHAR_CS");
		reservedWords.add("CHAR_LENGTH");
		reservedWords.add("CHECK");
		reservedWords.add("CHECKPOINT");
		reservedWords.add("CHOOSE");
		reservedWords.add("CHUNK");
		reservedWords.add("CLEAR");
		reservedWords.add("CLOB");
		reservedWords.add("CLONE");
		reservedWords.add("CLOSE");
		reservedWords.add("CLOSE_CACHED_OPEN_CURSORS");
		reservedWords.add("CLUSTER");
		reservedWords.add("COALESCE");
		reservedWords.add("COLLATE");
		reservedWords.add("COLLATION");
		reservedWords.add("COLLECTION");
		reservedWords.add("COLLID");
		reservedWords.add("COLUMN");
		reservedWords.add("COLUMNS");
		reservedWords.add("COMMENT");
		reservedWords.add("COMMIT");
		reservedWords.add("COMMITTED");
		reservedWords.add("COMPATIBILITY");
		reservedWords.add("COMPILE");
		reservedWords.add("COMPLETE");
		reservedWords.add("COMPOSITE_LIMIT");
		reservedWords.add("COMPRESS");
		reservedWords.add("COMPUTE");
		reservedWords.add("CONCAT");
		reservedWords.add("CONDITION");
		reservedWords.add("CONNECT");
		reservedWords.add("CONNECTION");
		reservedWords.add("CONNECT_TIME");
		reservedWords.add("CONSTRAINT");
		reservedWords.add("CONSTRAINTS");
		reservedWords.add("CONSTRUCTOR");
		reservedWords.add("CONTAINS");
		reservedWords.add("CONTENT");
		reservedWords.add("CONTENTS");
		reservedWords.add("CONTINUE");
		reservedWords.add("CONTROLFILE");
		reservedWords.add("CONVERT");
		reservedWords.add("CORRESPONDING");
		reservedWords.add("COST");
		reservedWords.add("COUNT");
		reservedWords.add("CPU_PER_CALL");
		reservedWords.add("CPU_PER_SESSION");
		reservedWords.add("CREATE");
		reservedWords.add("CROSS");
		reservedWords.add("CUBE");
		reservedWords.add("CURRENT");
		reservedWords.add("CURRENT_DATE");
		reservedWords.add("CURRENT_DEFAULT_TRANSFORM_GROUP");
		reservedWords.add("CURRENT_LC_CTYPE");
		reservedWords.add("CURRENT_PATH");
		reservedWords.add("CURRENT_ROLE");
		reservedWords.add("CURRENT_SCHEMA");
		reservedWords.add("CURRENT_TIME");
		reservedWords.add("CURRENT_TIMESTAMP");
		reservedWords.add("CURRENT_TRANSFORM_GROUP_FOR_TYPE");
		reservedWords.add("CURRENT_USER");
		reservedWords.add("CURREN_USER");
		reservedWords.add("CURSOR");
		reservedWords.add("CYCLE");
		reservedWords.add("DANGLING");
		reservedWords.add("DATA");
		reservedWords.add("DATABASE");
		reservedWords.add("DATABASES");
		reservedWords.add("DATAFILE");
		reservedWords.add("DATAFILES");
		reservedWords.add("DATAOBJNO");
		reservedWords.add("DATE");
		reservedWords.add("DAY");
		reservedWords.add("DAYS");
		reservedWords.add("DAY_HOUR");
		reservedWords.add("DAY_MICROSECOND");
		reservedWords.add("DAY_MINUTE");
		reservedWords.add("DAY_SECOND");
		reservedWords.add("DBA");
		reservedWords.add("DBHIGH");
		reservedWords.add("DBINFO");
		reservedWords.add("DBLOW");
		reservedWords.add("DBMAC");
		reservedWords.add("DEALLOCATE");
		reservedWords.add("DEBUG");
		reservedWords.add("DEC");
		reservedWords.add("DECIMAL");
		reservedWords.add("DECLARE");
		reservedWords.add("DEFAULT");
		reservedWords.add("DEFERRABLE");
		reservedWords.add("DEFERRED");
		reservedWords.add("DEGREE");
		reservedWords.add("DELAYED");
		reservedWords.add("DELETE");
		reservedWords.add("DEPTH");
		reservedWords.add("DEREF");
		reservedWords.add("DESC");
		reservedWords.add("DESCRIBE");
		reservedWords.add("DESCRIPTOR");
		reservedWords.add("DETERMINISTIC");
		reservedWords.add("DIAGNOSTICS");
		reservedWords.add("DIRECTORY");
		reservedWords.add("DISABLE");
		reservedWords.add("DISALLOW");
		reservedWords.add("DISCONNECT");
		reservedWords.add("DISMOUNT");
		reservedWords.add("DISTINCT");
		reservedWords.add("DISTINCTROW");
		reservedWords.add("DISTRIBUTED");
		reservedWords.add("DIV");
		reservedWords.add("DML");
		reservedWords.add("DO");
		reservedWords.add("DOCUMENT");
		reservedWords.add("DOMAIN");
		reservedWords.add("DOUBLE");
		reservedWords.add("DROP");
		reservedWords.add("DSSIZE");
		reservedWords.add("DUAL");
		reservedWords.add("DUMP");
		reservedWords.add("DYNAMIC");
		reservedWords.add("EACH");
		reservedWords.add("EDITPROC");
		reservedWords.add("ELEMENT");
		reservedWords.add("ELSE");
		reservedWords.add("ELSEIF");
		reservedWords.add("ENABLE");
		reservedWords.add("ENCLOSED");
		reservedWords.add("ENCODING");
		reservedWords.add("ENCRYPTION");
		reservedWords.add("END");
		reservedWords.add("END-EXEC");
		reservedWords.add("ENDING");
		reservedWords.add("ENFORCE");
		reservedWords.add("ENTRY");
		reservedWords.add("EQUALS");
		reservedWords.add("ERASE");
		reservedWords.add("ESCAPE");
		reservedWords.add("ESCAPED");
		reservedWords.add("EXCEPT");
		reservedWords.add("EXCEPTION");
		reservedWords.add("EXCEPTIONS");
		reservedWords.add("EXCHANGE");
		reservedWords.add("EXCLUDING");
		reservedWords.add("EXCLUSIVE");
		reservedWords.add("EXEC");
		reservedWords.add("EXECUTE");
		reservedWords.add("EXISTS");
		reservedWords.add("EXIT");
		reservedWords.add("EXPIRE");
		reservedWords.add("EXPLAIN");
		reservedWords.add("EXTENT");
		reservedWords.add("EXTENTS");
		reservedWords.add("EXTERNAL");
		reservedWords.add("EXTERNALLY");
		reservedWords.add("EXTRACT");
		reservedWords.add("FAILED_LOGIN_ATTEMPTS");
		reservedWords.add("FALSE");
		reservedWords.add("FAST");
		reservedWords.add("FENCED");
		reservedWords.add("FETCH");
		reservedWords.add("FIELDPROC");
		reservedWords.add("FILE");
		reservedWords.add("FILTER");
		reservedWords.add("FINAL");
		reservedWords.add("FIRST");
		reservedWords.add("FIRST_ROWS");
		reservedWords.add("FLAGGER");
		reservedWords.add("FLOAT");
		reservedWords.add("FLOAT4");
		reservedWords.add("FLOAT8");
		reservedWords.add("FLOB");
		reservedWords.add("FLUSH");
		reservedWords.add("FOR");
		reservedWords.add("FORCE");
		reservedWords.add("FOREIGN");
		reservedWords.add("FORTRAN");
		reservedWords.add("FOUND");
		reservedWords.add("FREE");
		reservedWords.add("FREELIST");
		reservedWords.add("FREELISTS");
		reservedWords.add("FREEZE");	// postgres
		reservedWords.add("FROM");
		reservedWords.add("FULL");
		reservedWords.add("FULLTEXT");
		reservedWords.add("FUNCTION");
		reservedWords.add("GENERAL");
		reservedWords.add("GENERATED");
		reservedWords.add("GET");
		reservedWords.add("GLOBAL");
		reservedWords.add("GLOBALLY");
		reservedWords.add("GLOBAL_NAME");
		reservedWords.add("GO");
		reservedWords.add("GOTO");
		reservedWords.add("GRANT");
		reservedWords.add("GROUP");
		reservedWords.add("GROUPING");
		reservedWords.add("GROUPS");
		reservedWords.add("HANDLER");
		reservedWords.add("HASH");
		reservedWords.add("HASHKEYS");
		reservedWords.add("HAVING");
		reservedWords.add("HEADER");
		reservedWords.add("HEAP");
		reservedWords.add("HIGH_PRIORITY");
		reservedWords.add("HOLD");
		reservedWords.add("HOST");
		reservedWords.add("HOUR");
		reservedWords.add("HOURS");
		reservedWords.add("HOUR_MICROSECOND");
		reservedWords.add("HOUR_MINUTE");
		reservedWords.add("HOUR_SECOND");
		reservedWords.add("IDENTIFIED");
		reservedWords.add("IDENTITY");
		reservedWords.add("IDGENERATORS");
		reservedWords.add("IDLE_TIME");
		reservedWords.add("IF");
		reservedWords.add("IGNORE");
		reservedWords.add("IGNORE_SERVER_IDS");
		reservedWords.add("ILIKE");	// postgres
		reservedWords.add("IMMEDIATE");
		reservedWords.add("IN");
		reservedWords.add("INCLUDE");
		reservedWords.add("INCLUDING");
		reservedWords.add("INCLUSIVE");
		reservedWords.add("INCREMENT");
		reservedWords.add("INDEX");
		reservedWords.add("INDEXED");
		reservedWords.add("INDEXES");
		reservedWords.add("INDICATOR");
		reservedWords.add("IND_PARTITION");
		reservedWords.add("INFILE");
		reservedWords.add("INHERIT");
		reservedWords.add("INITIAL");
		reservedWords.add("INITIALIZE");
		reservedWords.add("INITIALLY");
		reservedWords.add("INITRANS");
		reservedWords.add("INNER");
		reservedWords.add("INOUT");
		reservedWords.add("INPUT");
		reservedWords.add("INSENSITIVE");
		reservedWords.add("INSERT");
		reservedWords.add("INSTANCE");
		reservedWords.add("INSTANCES");
		reservedWords.add("INSTEAD");
		reservedWords.add("INT");
		reservedWords.add("INT1");
		reservedWords.add("INT2");
		reservedWords.add("INT3");
		reservedWords.add("INT4");
		reservedWords.add("INT8");
		reservedWords.add("INTEGER");
		reservedWords.add("INTERMEDIATE");
		reservedWords.add("INTERSECT");
		reservedWords.add("INTERVAL");
		reservedWords.add("INTO");
		reservedWords.add("IS");
		reservedWords.add("ISNULL");	// postgres
		reservedWords.add("ISOBID");
		reservedWords.add("ISOLATION");
		reservedWords.add("ISOLATION_LEVEL");
		reservedWords.add("ITERATE");
		reservedWords.add("JAR");
		reservedWords.add("JOIN");
		reservedWords.add("KEEP");
		reservedWords.add("KEY");
		reservedWords.add("KEYS");
		reservedWords.add("KILL");
		reservedWords.add("LABEL");
		reservedWords.add("LANGUAGE");
		reservedWords.add("LARGE");
		reservedWords.add("LAST");
		reservedWords.add("LATERAL");
		reservedWords.add("LAYER");
		reservedWords.add("LC_CTYPE");
		reservedWords.add("LEADING");
		reservedWords.add("LEAVE");
		reservedWords.add("LEFT");
		reservedWords.add("LESS");
		reservedWords.add("LEVEL");
		reservedWords.add("LIBRARY");
		reservedWords.add("LIKE");
		reservedWords.add("LIMIT");
		reservedWords.add("LINEAR");
		reservedWords.add("LINES");
		reservedWords.add("LINK");
		reservedWords.add("LIST");
		reservedWords.add("LOAD");
		reservedWords.add("LOB");
		reservedWords.add("LOCAL");
		reservedWords.add("LOCALE");
		reservedWords.add("LOCALTIME");
		reservedWords.add("LOCALTIMESTAMP");
		reservedWords.add("LOCATOR");
		reservedWords.add("LOCATORS");
		reservedWords.add("LOCK");
		reservedWords.add("LOCKED");
		reservedWords.add("LOCKMAX");
		reservedWords.add("LOCKSIZE");
		reservedWords.add("LOG");
		reservedWords.add("LOGFILE");
		reservedWords.add("LOGGING");
		reservedWords.add("LOGICAL_READS_PER_CALL");
		reservedWords.add("LOGICAL_READS_PER_SESSION");
		reservedWords.add("LONG");
		reservedWords.add("LONGBLOB");
		reservedWords.add("LONGTEXT");
		reservedWords.add("LOOP");
		reservedWords.add("LOWER");
		reservedWords.add("LOW_PRIORITY");
		reservedWords.add("MAINTAINED");
		reservedWords.add("MANAGE");
		reservedWords.add("MAP");
		reservedWords.add("MASTER");
		reservedWords.add("MASTER_HEARTBEAT_PERIOD");
		reservedWords.add("MASTER_SSL_VERIFY_SERVER_CERT");
		reservedWords.add("MATCH");
		reservedWords.add("MATERIALIZED");
		reservedWords.add("MAX");
		reservedWords.add("MAXARCHLOGS");
		reservedWords.add("MAXDATAFILES");
		reservedWords.add("MAXEXTENTS");
		reservedWords.add("MAXINSTANCES");
		reservedWords.add("MAXLOGFILES");
		reservedWords.add("MAXLOGHISTORY");
		reservedWords.add("MAXLOGMEMBERS");
		reservedWords.add("MAXSIZE");
		reservedWords.add("MAXTRANS");
		reservedWords.add("MAXVALUE");
		reservedWords.add("MEDIUMBLOB");
		reservedWords.add("MEDIUMINT");
		reservedWords.add("MEDIUMTEXT");
		reservedWords.add("MEMBER");
		reservedWords.add("MERGE");
		reservedWords.add("METHOD");
		reservedWords.add("MICROSECOND");
		reservedWords.add("MICROSECONDS");
		reservedWords.add("MIDDLEINT");
		reservedWords.add("MIN");
		reservedWords.add("MINEXTENTS");
		reservedWords.add("MINIMUM");
		reservedWords.add("MINUS");
		reservedWords.add("MINUTE");
		reservedWords.add("MINUTES");
		reservedWords.add("MINUTE_MICROSECOND");
		reservedWords.add("MINUTE_SECOND");
		reservedWords.add("MINVALUE");
		reservedWords.add("MLSLABEL");
		reservedWords.add("MLS_LABEL_FORMAT");
		reservedWords.add("MOD");
		reservedWords.add("MODE");
		reservedWords.add("MODIFIES");
		reservedWords.add("MODIFY");
		reservedWords.add("MODULE");
		reservedWords.add("MONTH");
		reservedWords.add("MONTHS");
		reservedWords.add("MOUNT");
		reservedWords.add("MOVE");
		reservedWords.add("MTS_DISPATCHERS");
		reservedWords.add("MULTISET");
		reservedWords.add("NAMES");
		reservedWords.add("NATIONAL");
		reservedWords.add("NATURAL");
		reservedWords.add("NCHAR");
		reservedWords.add("NCHAR_CS");
		reservedWords.add("NCLOB");
		reservedWords.add("NEEDED");
		reservedWords.add("NESTED");
		reservedWords.add("NETWORK");
		reservedWords.add("NEW");
		reservedWords.add("NEXT");
		reservedWords.add("NEXTVAL");
		reservedWords.add("NO");
		reservedWords.add("NOARCHIVELOG");
		reservedWords.add("NOAUDIT");
		reservedWords.add("NOCACHE");
		reservedWords.add("NOCOMPRESS");
		reservedWords.add("NOCYCLE");
		reservedWords.add("NOFORCE");
		reservedWords.add("NOLOGGING");
		reservedWords.add("NOMAXVALUE");
		reservedWords.add("NOMINVALUE");
		reservedWords.add("NONE");
		reservedWords.add("NOORDER");
		reservedWords.add("NOOVERRIDE");
		reservedWords.add("NOPARALLEL");
		reservedWords.add("NOREVERSE");
		reservedWords.add("NORMAL");
		reservedWords.add("NOSORT");
		reservedWords.add("NOT");
		reservedWords.add("NOTHING");
		reservedWords.add("NOWAIT");
		reservedWords.add("NO_WRITE_TO_BINLOG");
		reservedWords.add("NULL");
		reservedWords.add("NULLIF");
		reservedWords.add("NULLS");
		reservedWords.add("NUMBER");
		reservedWords.add("NUMERIC");
		reservedWords.add("NUMPARTS");
		reservedWords.add("NVARCHAR2");
		reservedWords.add("OBID");
		reservedWords.add("OBJECT");
		reservedWords.add("OBJNO");
		reservedWords.add("OBJNO_REUSE");
		reservedWords.add("OCTET_LENGTH");
		reservedWords.add("OF");
		reservedWords.add("OFF");
		reservedWords.add("OFFLINE");
		reservedWords.add("OFFSET");	// postgres
		reservedWords.add("OID");
		reservedWords.add("OIDINDEX");
		reservedWords.add("OLD");
		reservedWords.add("ON");
		reservedWords.add("ONLINE");
		reservedWords.add("ONLY");
		reservedWords.add("OPCODE");
		reservedWords.add("OPEN");
		reservedWords.add("OPTIMAL");
		reservedWords.add("OPTIMIZATION");
		reservedWords.add("OPTIMIZE");
		reservedWords.add("OPTIMIZER_GOAL");
		reservedWords.add("OPTION");
		reservedWords.add("OPTIONALLY");
		reservedWords.add("OR");
		reservedWords.add("ORDER");
		reservedWords.add("ORDINALITY");
		reservedWords.add("ORGANIZATION");
		reservedWords.add("OSLABEL");
		reservedWords.add("OUT");
		reservedWords.add("OUTER");
		reservedWords.add("OUTFILE");
		reservedWords.add("OUTPUT");
		reservedWords.add("OVER");
		reservedWords.add("OVERFLOW");
		reservedWords.add("OVERLAPS");
		reservedWords.add("OWN");
		reservedWords.add("PACKAGE");
		reservedWords.add("PAD");
		reservedWords.add("PADDED");
		reservedWords.add("PARALLEL");
		reservedWords.add("PARAMETER");
		reservedWords.add("PART");
		reservedWords.add("PARTIAL");
		reservedWords.add("PARTITION");
		reservedWords.add("PARTITIONED");
		reservedWords.add("PARTITIONING");
		reservedWords.add("PASCAL");
		reservedWords.add("PASSWORD");
		reservedWords.add("PASSWORD_GRACE_TIME");
		reservedWords.add("PASSWORD_LIFE_TIME");
		reservedWords.add("PASSWORD_LOCK_TIME");
		reservedWords.add("PASSWORD_REUSE_MAX");
		reservedWords.add("PASSWORD_REUSE_TIME");
		reservedWords.add("PASSWORD_VERIFY_FUNCTION");
		reservedWords.add("PATH");
		reservedWords.add("PCTFREE");
		reservedWords.add("PCTINCREASE");
		reservedWords.add("PCTTHRESHOLD");
		reservedWords.add("PCTUSED");
		reservedWords.add("PCTVERSION");
		reservedWords.add("PERCENT");
		reservedWords.add("PERIOD");
		reservedWords.add("PERMANENT");
		reservedWords.add("PIECESIZE");
		reservedWords.add("PLAN");
		reservedWords.add("PLACING");	// postgres
		reservedWords.add("PLSQL_DEBUG");
		reservedWords.add("POSITION");
		reservedWords.add("POST_TRANSACTION");
		reservedWords.add("PRECISION");
		reservedWords.add("PREPARE");
		reservedWords.add("PRESERVE");
		reservedWords.add("PREVVAL");
		reservedWords.add("PRIMARY");
		reservedWords.add("PRIOR");
		reservedWords.add("PRIQTY");
		reservedWords.add("PRIVATE");
		reservedWords.add("PRIVATE_SGA");
		reservedWords.add("PRIVILEGE");
		reservedWords.add("PRIVILEGES");
		reservedWords.add("PROCEDURE");
		reservedWords.add("PROFILE");
		reservedWords.add("PROGRAM");
		reservedWords.add("PSID");
		reservedWords.add("PUBLIC");
		reservedWords.add("PURGE");
		reservedWords.add("QUERY");
		reservedWords.add("QUERYNO");
		reservedWords.add("QUEUE");
		reservedWords.add("QUOTA");
		reservedWords.add("RANGE");
		reservedWords.add("RAW");
		reservedWords.add("RBA");
		reservedWords.add("READ");
		reservedWords.add("READS");
		reservedWords.add("READUP");
		reservedWords.add("READ_WRITE");
		reservedWords.add("REAL");
		reservedWords.add("REBUILD");
		reservedWords.add("RECOVER");
		reservedWords.add("RECOVERABLE");
		reservedWords.add("RECOVERY");
		reservedWords.add("RECURSIVE");
		reservedWords.add("REF");
		reservedWords.add("REFERENCES");
		reservedWords.add("REFERENCING");
		reservedWords.add("REFRESH");
		reservedWords.add("REGEXP");
		reservedWords.add("RELATIVE");
		reservedWords.add("RELEASE");
		reservedWords.add("RENAME");
		reservedWords.add("REPEAT");
		reservedWords.add("REPLACE");
		reservedWords.add("REQUIRE");
		reservedWords.add("RESET");
		reservedWords.add("RESETLOGS");
		reservedWords.add("RESIGNAL");
		reservedWords.add("RESIZE");
		reservedWords.add("RESOURCE");
		reservedWords.add("RESTRICT");
		reservedWords.add("RESTRICTED");
		reservedWords.add("RESULT");
		reservedWords.add("RESULT_SET_LOCATOR");
		reservedWords.add("RETURN");
		reservedWords.add("RETURNING");
		reservedWords.add("RETURNS");
		reservedWords.add("REUSE");
		reservedWords.add("REVERSE");
		reservedWords.add("REVOKE");
		reservedWords.add("RIGHT");
		reservedWords.add("RLIKE");
		reservedWords.add("ROLE");
		reservedWords.add("ROLES");
		reservedWords.add("ROLLBACK");
		reservedWords.add("ROLLUP");
		reservedWords.add("ROUND_CEILING");
		reservedWords.add("ROUND_DOWN");
		reservedWords.add("ROUND_FLOOR");
		reservedWords.add("ROUND_HALF_DOWN");
		reservedWords.add("ROUND_HALF_EVEN");
		reservedWords.add("ROUND_HALF_UP");
		reservedWords.add("ROUND_UP");
		reservedWords.add("ROUTINE");
		reservedWords.add("ROW");
		reservedWords.add("ROWID");
		reservedWords.add("ROWNUM");
		reservedWords.add("ROWS");
		reservedWords.add("ROWSET");
		reservedWords.add("RULE");
		reservedWords.add("RUN");
		reservedWords.add("SAMPLE");
		reservedWords.add("SAVEPOINT");
		reservedWords.add("SB4");
		reservedWords.add("SCAN_INSTANCES");
		reservedWords.add("SCHEMA");
		reservedWords.add("SCHEMAS");
		reservedWords.add("SCN");
		reservedWords.add("SCOPE");
		reservedWords.add("SCRATCHPAD");
		reservedWords.add("SCROLL");
		reservedWords.add("SD_ALL");
		reservedWords.add("SD_INHIBIT");
		reservedWords.add("SD_SHOW");
		reservedWords.add("SEARCH");
		reservedWords.add("SECOND");
		reservedWords.add("SECONDS");
		reservedWords.add("SECOND_MICROSECOND");
		reservedWords.add("SECQTY");
		reservedWords.add("SECTION");
		reservedWords.add("SECURITY");
		reservedWords.add("SEGMENT");
		reservedWords.add("SEG_BLOCK");
		reservedWords.add("SEG_FILE");
		reservedWords.add("SELECT");
		reservedWords.add("SENSITIVE");
		reservedWords.add("SEPARATOR");
		reservedWords.add("SEQUENCE");
		reservedWords.add("SERIALIZABLE");
		reservedWords.add("SESSION");
		reservedWords.add("SESSIONS_PER_USER");
		reservedWords.add("SESSION_CACHED_CURSORS");
		reservedWords.add("SESSION_USER");
		reservedWords.add("SET");
		reservedWords.add("SETS");
		reservedWords.add("SHARE");
		reservedWords.add("SHARED");
		reservedWords.add("SHARED_POOL");
		reservedWords.add("SHOW");
		reservedWords.add("SHRINK");
		reservedWords.add("SIGNAL");
		reservedWords.add("SIMILAR");
		reservedWords.add("SIMPLE");
		reservedWords.add("SIZE");
		reservedWords.add("SKIP");
		reservedWords.add("SKIP_UNUSABLE_INDEXES");
		reservedWords.add("SLOW");
		reservedWords.add("SMALLINT");
		reservedWords.add("SNAPSHOT");
		reservedWords.add("SOME");
		reservedWords.add("SORT");
		reservedWords.add("SOURCE");
		reservedWords.add("SPACE");
		reservedWords.add("SPATIAL");
		reservedWords.add("SPECIFIC");
		reservedWords.add("SPECIFICATION");
		reservedWords.add("SPECIFICTYPE");
		reservedWords.add("SPLIT");
		reservedWords.add("SQL");
		reservedWords.add("SQLCA");
		reservedWords.add("SQLCODE");
		reservedWords.add("SQLERROR");
		reservedWords.add("SQLEXCEPTION");
		reservedWords.add("SQLSTATE");
		reservedWords.add("SQLWARNING");
		reservedWords.add("SQL_BIG_RESULT");
		reservedWords.add("SQL_CALC_FOUND_ROWS");
		reservedWords.add("SQL_SMALL_RESULT");
		reservedWords.add("SQL_TRACE");
		reservedWords.add("SSL");
		reservedWords.add("STANDARD");
		reservedWords.add("STANDBY");
		reservedWords.add("START");
		reservedWords.add("STARTING");
		reservedWords.add("STATE");
		reservedWords.add("STATEMENT");
		reservedWords.add("STATEMENT_ID");
		reservedWords.add("STATIC");
		reservedWords.add("STATISTICS");
		reservedWords.add("STAY");
		reservedWords.add("STOGROUP");
		reservedWords.add("STOP");
		reservedWords.add("STORAGE");
		reservedWords.add("STORE");
		reservedWords.add("STORES");
		reservedWords.add("STRAIGHT_JOIN");
		reservedWords.add("STRUCTURE");
		reservedWords.add("STYLE");
		reservedWords.add("SUBMULTISET");
		reservedWords.add("SUBSTRING");
		reservedWords.add("SUCCESSFUL");
		reservedWords.add("SUM");
		reservedWords.add("SUMMARY");
		reservedWords.add("SWITCH");
		reservedWords.add("SYMMETRIC");
		reservedWords.add("SYNONYM");
		reservedWords.add("SYSDATE");
		reservedWords.add("SYSDBA");
		reservedWords.add("SYSFUN");
		reservedWords.add("SYSIBM");
		reservedWords.add("SYSOPER");
		reservedWords.add("SYSPROC");
		reservedWords.add("SYSTEM");
		reservedWords.add("SYSTEM_USER");
		reservedWords.add("SYS_OP_ENFORCE_NOT_NULL$");
		reservedWords.add("SYS_OP_NTCIMG$");
		reservedWords.add("TABLE");
		reservedWords.add("TABLES");
		reservedWords.add("TABLESAMPLE");
		reservedWords.add("TABLESPACE");
		reservedWords.add("TABLESPACE_NO");
		reservedWords.add("TABNO");
		reservedWords.add("TEMPORARY");
		reservedWords.add("TERMINATED");
		reservedWords.add("THAN");
		reservedWords.add("THE");
		reservedWords.add("THEN");
		reservedWords.add("THREAD");
		reservedWords.add("TIME");
		reservedWords.add("TIMESTAMP");
		reservedWords.add("TIMEZONE_HOUR");
		reservedWords.add("TIMEZONE_MINUTE");
		reservedWords.add("TINYBLOB");
		reservedWords.add("TINYINT");
		reservedWords.add("TINYTEXT");
		reservedWords.add("TO");
		reservedWords.add("TOPLEVEL");
		reservedWords.add("TRACE");
		reservedWords.add("TRACING");
		reservedWords.add("TRAILING");
		reservedWords.add("TRANSACTION");
		reservedWords.add("TRANSITIONAL");
		reservedWords.add("TRANSLATE");
		reservedWords.add("TRANSLATION");
		reservedWords.add("TREAT");
		reservedWords.add("TRIGGER");
		reservedWords.add("TRIGGERS");
		reservedWords.add("TRIM");
		reservedWords.add("TRUE");
		reservedWords.add("TRUNCATE");
		reservedWords.add("TX");
		reservedWords.add("TYPE");
		reservedWords.add("UB2");
		reservedWords.add("UBA");
		reservedWords.add("UID");
		reservedWords.add("UNARCHIVED");
		reservedWords.add("UNDER");
		reservedWords.add("UNDO");
		reservedWords.add("UNION");
		reservedWords.add("UNIQUE");
		reservedWords.add("UNKNOWN");
		reservedWords.add("UNLIMITED");
		reservedWords.add("UNLOCK");
		reservedWords.add("UNNEST");
		reservedWords.add("UNRECOVERABLE");
		reservedWords.add("UNSIGNED");
		reservedWords.add("UNTIL");
		reservedWords.add("UNUSABLE");
		reservedWords.add("UNUSED");
		reservedWords.add("UPDATABLE");
		reservedWords.add("UPDATE");
		reservedWords.add("UPPER");
		reservedWords.add("USAGE");
		reservedWords.add("USE");
		reservedWords.add("USER");
		reservedWords.add("USING");
		reservedWords.add("UTC_DATE");
		reservedWords.add("UTC_TIME");
		reservedWords.add("UTC_TIMESTAMP");
		reservedWords.add("VALIDATE");
		reservedWords.add("VALIDATION");
		reservedWords.add("VALIDPROC");
		reservedWords.add("VALUE");
		reservedWords.add("VALUES");
		reservedWords.add("VARBINARY");
		reservedWords.add("VARCHAR");
		reservedWords.add("VARCHAR2");
		reservedWords.add("VARCHARACTER");
		reservedWords.add("VARIABLE");
		reservedWords.add("VARIANT");
		reservedWords.add("VARYING");
		reservedWords.add("VCAT");
		reservedWords.add("VIEW");
		reservedWords.add("VOLATILE");
		reservedWords.add("VOLUMES");
		reservedWords.add("WHEN");
		reservedWords.add("WHENEVER");
		reservedWords.add("WHERE");
		reservedWords.add("WHILE");
		reservedWords.add("WINDOW");
		reservedWords.add("WITH");
		reservedWords.add("WITHIN");
		reservedWords.add("WITHOUT");
		reservedWords.add("WLM");
		reservedWords.add("WORK");
		reservedWords.add("WRITE");
		reservedWords.add("WRITEDOWN");
		reservedWords.add("WRITEUP");
		reservedWords.add("XID");
		reservedWords.add("XMLCAST");
		reservedWords.add("XMLEXISTS");
		reservedWords.add("XMLNAMESPACES");
		reservedWords.add("XOR");
		reservedWords.add("YEAR");
		reservedWords.add("YEARS");
		reservedWords.add("YEAR_MONTH");
		reservedWords.add("ZEROFILL");
		reservedWords.add("ZONE");
		// 834
		
		// how to add new entries: (1) copy&paste text into excel, (2) copy&paste columns to here, (3) apply find/replace with regexp
		// find ^([ \t]*)([A-Z])
		// replace \1reservedWords.add("\2
		// find ([A-Z])$
		// replace \1");
		
		// Source generator, comment this in when having new reserved words.
		/*
		List<String> list = new ArrayList<String>(reservedWords);
		Collections.sort(list);
		int i = 0;
		for (String w : list)	{
			i++;
			System.err.println("\t\treservedWords.add(\""+w+"\");");
		}
		System.err.println("\t\t// "+i);
		*/
	}

}
