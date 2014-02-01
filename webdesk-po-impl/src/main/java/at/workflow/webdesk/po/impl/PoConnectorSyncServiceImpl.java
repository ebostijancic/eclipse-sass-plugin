package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import at.workflow.webdesk.po.FieldTypeAwareConnector;
import at.workflow.webdesk.po.PoConnectorInterface;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoConnectorSyncService;
import at.workflow.webdesk.po.PoDestinationConnectorInterface;
import at.workflow.webdesk.po.PoPreviewConnector;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.po.PoSourceConnectorInterface;
import at.workflow.webdesk.po.PrimaryKeyAwareConnector;
import at.workflow.webdesk.po.converter.AutomaticConverter;
import at.workflow.webdesk.po.converter.AutomaticConverterFactory;
import at.workflow.webdesk.po.converter.Converter;
import at.workflow.webdesk.po.impl.converter.NullStringConverter;
import at.workflow.webdesk.po.model.PoConnectorLink;
import at.workflow.webdesk.po.model.PoFieldMapping;

/**
 * Implementation of the Connecter link synchronization. Its main task is to sync data between a source and
 * a destination connector. Passed is always a connectorLink which contains the 2 connectors which
 * should be synchronized.
 * Synchronization might add values only on insert and might also delete rows in the destination connector
 * which are not contained in the source connector (if defined).
 * Conversion and scripting can take place for every field of each row to be transfered. It can be configured
 * if scripting will be processed BEFORE or AFTER the conversion takes place.
 * 
 * @author ggruber
 * @author ebostijancic 24.07.2012
 *
 */
public class PoConnectorSyncServiceImpl implements PoConnectorSyncService, ApplicationContextAware {

	private static final String AFTERCONVERSION_POSTFIX = "$converted";
	private static final String AFTERSCRIPT_POSTFIX = "$scripted";
	protected final String logCategory = "webdesk.connectors.po.PoConnectorSyncServiceImpl";
    protected final Logger logger = Logger.getLogger(logCategory);
    private ApplicationContext appCtx;
    private PoConnectorService connectorService;
    private PoScriptingService scriptingService;
    
    @Override
	public void synchronise(PoConnectorLink link, String addSourceConstraint, PoSourceConnectorInterface source, PoDestinationConnectorInterface destination) {
        
        PoSourceConnectorInterface sourceConnector;
        PoDestinationConnectorInterface destinationConnector;
        
    	if (source != null)
    		sourceConnector = source;
    	else
    		sourceConnector = connectorService.getSrcConnectorImpl(link.getSrcConnector());
    	
    	if (destination!=null)
    		destinationConnector = destination;
    	else
    		destinationConnector = connectorService.getDestConnectorImpl(link.getDestConnector());

    	boolean dryRun = false;	// by default synchronise is not in dry run mode.
    	// if the destination is preview connector, do a dry run.
    	if(destinationConnector instanceof PoPreviewConnector)
    		dryRun = true;
        	
        if (isInfoEnabled(dryRun)) {
        	logger.info("Start sync for " + link);
        }
    	
        // assertion
        if (sourceConnector == null || destinationConnector==null) {
            throw new IllegalStateException("Either source or destination connector is null, so we can't synchronise!");
        }
        
        // 	Start Synchronisation
        try {
        	doSynchronise(sourceConnector,destinationConnector,link, addSourceConstraint, dryRun);
        } catch(Exception e) {
        	logger.error("errors occured while syncing... ",e);
        }
        
        if (isInfoEnabled(dryRun))
        	logger.info("ended sync for " + link);
    }

    @Override
	public List<String> getFieldNamesForConnector(String uid) {
		PoConnectorInterface myConn;
		myConn = connectorService.getConnectorImpl(connectorService.getConnector(uid));
		return myConn.getFieldNames();
		
	}
    
    private void assertConnectorFieldMappingPrimaryKey(PoConnectorLink connectorLink) {
    	for (PoFieldMapping mapping : connectorLink.getMappingFields()) {
    		if (mapping.isPrimaryKey()) {
    			return;
    		}
    	}
    	throw new PoRuntimeException("No Primary Key Column defined!");
    }
    
    /**
     * allows to define 'dynamic' constraints holding
     * some scripted parts, which are evaluated with velocity... 
     */
    private String evaluateVelocityConstructsInConstraint(String constraint) {
    	if (constraint == null)
    		return null;
    	
    	Map<String, Object> ctx = new HashMap<String, Object>();
    	return scriptingService.velocitySubstitution(constraint, ctx);
    }
    
    // copy of method in ManageFileTransferHelper...
    // Where to put such a method?
	private String concatContraints(String constr1, String constr2) {
		if (StringUtils.isEmpty( constr1 ) && StringUtils.isEmpty( constr2 ))
				return "";
		
		if (StringUtils.isEmpty( constr1 ) && StringUtils.isEmpty( constr2 ) == false)
			return constr2;
		
		if (StringUtils.isEmpty( constr1 ) == false && StringUtils.isEmpty( constr2 ))
			return constr1;
		
		// TODO
		// depending on the source connector the needed operator for the 'AND' expression
		// could be different. For now it is hardcoded. We might have to ask the connector for
		// concatination of expressions in the future???
		return constr1 + " and " + constr2;
	}

	/**
	 * One source can be connected to more than one destinations 
	 * therefore names of both the source and the destination are combined to mapping key
	 */
	private String getMappingKey(PoFieldMapping fieldMapping) {
		return fieldMapping.getSourceName() + "_" + fieldMapping.getDestinationName();
	}
		
    /**
     * @param sourceConn
     * @param destConn
     * @param link
     * @param addSourceConstraint: additional constraint on the sourceconnector!
     * @return List of Maps containing the processed rows
     * 
     * Synchronises all Mapping Fields (contained in the Mapping Fields of the <code>link</code> variable
     * between the source - and the destination - connector.
     */
    private List<Object> doSynchronise(PoSourceConnectorInterface sourceConn,PoDestinationConnectorInterface destConn, PoConnectorLink link, String addSourceConstraint, boolean dryRun) {
    	
    	Map<String, NullStringConverter> nullStringConverters = getNullStringConverters(sourceConn, destConn, link);
    	Map<String, AutomaticConverter> automaticConverters = getAutomaticConverters(sourceConn, destConn, link);
    	
    	if (destConn instanceof PrimaryKeyAwareConnector)
    		assertConnectorFieldMappingPrimaryKey(link);
    	
        List<String> fieldNames = getFieldNamesFromLink(link,true);
        String constraint = link.getSourceConstraint();
        
        constraint = concatContraints(constraint, addSourceConstraint);
        
        // do velocity substitution
        constraint = evaluateVelocityConstructsInConstraint(constraint);
        
        List<Object> res = new ArrayList<Object>();
        
        // get objects from source connector
        if ( isDebugEnabled(dryRun) ) { 
        	logger.debug("load Objects from Sourceconnector with this constraint: ");
            logger.debug(constraint);
        }
        List<Map<String,Object>> srcResultSet = sourceConn.findAllObjects(fieldNames, constraint);
        
        if ( isInfoEnabled(dryRun) )
        	logger.info("Source Connector returned " + srcResultSet.size() + " entries. Going to investigate them all!");
        
        // contains the Primary Keys 
        List<Object> synchronizedPrimaryKeysOfDestinationMap = new ArrayList<Object>();
        List<Map<String, Object>> srcObjectsToPostProcess = new ArrayList<Map<String, Object>>();
        
        Iterator<Map<String,Object>> srcResultSetIterator = srcResultSet.iterator();
        
        if ( isDebugEnabled(dryRun) )
        	logger.debug("The source result set contains " + srcResultSet.size() + " entries: " + srcResultSet);
        
        long fieldsProcMillis = 0, rowProcMillis = 0;
        long converter1ProcMillis = 0, jsProcMillis = 0, converter2ProcMillis = 0;
        long postProcMillis = 0;
        
        int counter = 0;
        int noOfUpdates = 0;
        int noOfInserts = 0;
        int noOfErrors = 0;
        
        // for all objects the source Connector returned 
        while (srcResultSetIterator.hasNext()) {
        	
        	Map<String,Object> sourceObjMap = srcResultSetIterator.next(); 
        	Map<String, Object> destObjMap = new HashMap<String, Object>();
        	
            try {
            	
            	
                long fieldsMillis = 0;
                
                fieldsMillis = System.currentTimeMillis();
                if( isDebugEnabled(dryRun) )
                	logger.debug("sourcemap=" + sourceObjMap);
                
                long converter1Millis = 0, jsMillis = 0, converter2Millis = 0;
                // The purpose of this code is to fill up the destObjMap
                Iterator<PoFieldMapping> mappingFields = link.getMappingFields().iterator();
                while (mappingFields.hasNext() ) {
                		PoFieldMapping fm = mappingFields.next();
                		// the mapped fieldname
                        String destinationFieldName = fm.getDestinationName();
	                    Object value = sourceObjMap.get(fm.getSourceName());
	                    
	                    NullStringConverter nullStringConverter = nullStringConverters.get(getMappingKey(fm));
	                    if (nullStringConverter != null)
	                    	value = nullStringConverter.convert((String) value);
	                    
	                    converter1Millis = System.currentTimeMillis();
	                    
	                    if (!link.isConverterAfterJavaScript()) {
	                    	// conversion takes place before javascript is executed
		                    value = applyConverter(link, automaticConverters, srcResultSet, sourceObjMap, fm, value);
	                    }
	                    
	                    converter1ProcMillis += System.currentTimeMillis() - converter1Millis;
	                    jsMillis = System.currentTimeMillis();
	                    
	                    if (fm.getJsFunction() != null) {
	                    	value = evaluateJavaScriptWithTransaction(dryRun, sourceObjMap, fm, value);
	                    }
	                    
	                    jsProcMillis += System.currentTimeMillis() - jsMillis;
	                    converter2Millis = System.currentTimeMillis();
	                    
	                    if (link.isConverterAfterJavaScript()) {
		                    value = applyConverter(link, automaticConverters, srcResultSet, sourceObjMap, fm, value);
	                    }
	                    
	                    converter2ProcMillis += System.currentTimeMillis() - converter2Millis;

	                    if (destinationFieldName!=null && !"".equals(destinationFieldName))
	                    	destObjMap.put(destinationFieldName, value);
                }
                
                removePostProcessedConvertedValues(sourceObjMap);
                
                fieldsProcMillis += System.currentTimeMillis() - fieldsMillis;
                if ( isDebugEnabled(dryRun) ) {
                	logger.debug("destinationm	ap=" + destObjMap);
                }
                
                long rowMillis = System.currentTimeMillis();
                
                if (!(destConn instanceof PrimaryKeyAwareConnector)) {
                	
                	if ( isDebugEnabled(dryRun) )
                		logger.debug("Destination not primary key aware -> insert");
                	
                    res.add(destConn.saveObject(destObjMap, null, link.getMappingFieldsAsList(false)));
                    
                    noOfInserts++;
                    
                } else {
	                // the purpose of this code is to save or update the destObjMap, also (if parameterized) adapted 
	                // values of the sourceObj are saved here 
	                final String id = toStringOrNull(destObjMap.get(link.getPrimaryKey(false)));
	                if (!destConn.containsObject( id , link.getPrimaryKey(false))) {
	                    
	                	if ( isDebugEnabled(dryRun) )
	                		logger.debug("Obj is not existent in destination -> insert");
	                	
	                    res.add(destConn.saveObject(destObjMap,link.getPrimaryKey(false), link.getMappingFieldsAsList(false)));
	                    
	                    noOfInserts++;
	                    
	                } else {
	                	
	                	noOfUpdates = performUpdate(destConn, link, res, noOfUpdates, sourceObjMap, destObjMap);
	                }
	                
	                // we keep the information which objects are synchronised. 
	                synchronizedPrimaryKeysOfDestinationMap.add(destObjMap.get(link.getPrimaryKey(false)));
                }
                
                long postMillis = System.currentTimeMillis();
                
                // add process sourceObjMap to result List
                // which was successfully transfered
                // and should be postprocessed
                srcObjectsToPostProcess.add(sourceObjMap);

                postProcMillis += System.currentTimeMillis() - postMillis;
               	rowProcMillis += System.currentTimeMillis() - rowMillis;
                
                if (counter%100==0) {
                	if( isInfoEnabled(dryRun) )
                		logger.info("Actually investigated " + counter + " entries and spent " + fieldsProcMillis/1000 + "sec in fields and " + rowProcMillis/1000 + " sec in row processing");
                	if ( isDebugEnabled(dryRun) ) {
	                	logger.debug("Fields processing: 1st converter " + converter1ProcMillis/1000 + " sec, js " + jsProcMillis/1000 + " sec, converter 2 " + converter2ProcMillis/1000 + " sec");
	                	logger.debug("Postprocessing: " + postProcMillis/1000 + " sec");
                	}
                }
            } catch (Exception e) {
                logger.error("Could not save the " + (counter+1) + ". object (sourceObjectMap=" + sourceObjMap + "), due to: "+ e.getMessage());
                if (logger.isInfoEnabled()) {
                	logger.error(e.getMessage(), e);
                }
                noOfErrors++;
            }
            
            counter ++;
        }
        
        if (isInfoEnabled(dryRun))
        	logger.info("Number of inserts=" + noOfInserts + ", Number of updates=" + noOfUpdates + ", Number of errors=" + noOfErrors);
        
        if (destConn instanceof PrimaryKeyAwareConnector && link.isSyncDeletions() && dryRun == false) {
        	if(isInfoEnabled(dryRun))
        		logger.info("Sync of deletions is enabled");
    		performSynchronizedDeletions(destConn, link, synchronizedPrimaryKeysOfDestinationMap);
        }
        
        // do post processing.
        if (dryRun == false)
        	doPostProcess(srcObjectsToPostProcess, sourceConn, destConn, res);
		
        return res;
    }

	private boolean isInfoEnabled(boolean dryRun) {
		return logger.isInfoEnabled() && dryRun==false;
	}
	
	private boolean isDebugEnabled(boolean dryRun) {
		return logger.isDebugEnabled() && dryRun==false;
	}
    
    /**
     * Evaluates Javascript inside a transaction, to ensure that in dryRun mode no modifications to the database are persisted.
     * In dryrun mode the transaction is rolled back therefore!
     * @return
     */
	private Object evaluateJavaScriptWithTransaction(boolean dryRun, Map<String, Object> sourceObjMap, PoFieldMapping fm, Object value) {
		
		PlatformTransactionManager transactionManger = getTransactionManager();
		
		DefaultTransactionDefinition transDef = new DefaultTransactionDefinition();
		transDef.setName("javascriptFunction");
		transDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			
		TransactionStatus status = transactionManger.getTransaction(transDef);
		
		try {
			value = evaluateJavaScript(sourceObjMap, fm, dryRun);	                    		
		} catch (Exception e) {
			// do nothing here -> we ignore it!
			logger.warn("Problems evaluating a javascript", e);
			logger.warn("This is the problematic javascript:\n" + fm.getJsFunction());
			logger.warn("using this context: " + sourceObjMap);
		}
		
		if(dryRun)
			transactionManger.rollback(status);
		else 
			transactionManger.commit(status);
		return value;
	}
	
	private PlatformTransactionManager getTransactionManager() {
		return (PlatformTransactionManager) appCtx.getBean("TransactionManager");
	}
    
    private void doPostProcess(List<Map<String, Object>> srcObjectsToPostProcess, final PoSourceConnectorInterface sourceConn, final PoDestinationConnectorInterface destConn, List<Object> res) {
        // do postprocessing of source objects
        // (changing their state, etc.)
        
		TransactionTemplate transactionTemplate = new TransactionTemplate( getTransactionManager() );

		// Run postprocess of Source & destination connector
		// in Transaction 
		final PoSourceConnectorInterface argSrcConn = sourceConn;
		final PoDestinationConnectorInterface argDestConn = destConn; 
		
		// hentner TODO: bad code, postProcess is one time called with a list of Objects(id's as String) , and one time 
		// with a List of Maps 
		// check if this code (with the final variables in a service), can cause a memory leak
		final List<Map<String,Object>> argSrcObjectsToPostProcess = srcObjectsToPostProcess;
		final List<Object> argRes = res;
		
		// transaction may last max. 1 hour!
		transactionTemplate.setTimeout( 60 * 60 );
		transactionTemplate.execute(new TransactionCallback() {
			
	        @Override
			public Object doInTransaction(TransactionStatus status ) {
	        	
	        	// don't change anything on source side in dry run mode.
        		argSrcConn.postProcessImportedRecords(argSrcObjectsToPostProcess);	        		
	        	argDestConn.postProcessImportedObjects(argRes);
	        	return status;
	        }
	    });
    }

	private Object evaluateJavaScript(Map<String, Object> sourceObjMap, PoFieldMapping fm, boolean dryRun) {
		Object value;		
		if (isDebugEnabled(dryRun)) 
			logger.debug("execute js command: " + fm.getJsFunction() + " of field " + fm.getSourceName());
		value = scriptingService.executeJS(fm.getJsFunction(), sourceObjMap);
		
		if (value instanceof org.mozilla.javascript.Undefined)
			logger.warn("result of js command of field " +  fm.getSourceName() + " ["+ fm.getJsFunction()+"] = " + value);
		if (value instanceof org.mozilla.javascript.NativeJavaObject)
			value = ((org.mozilla.javascript.NativeJavaObject)value).unwrap();
		
		addScriptedValueAsEntry(sourceObjMap, fm.getSourceName(), value);
		
		if (isDebugEnabled(dryRun)) 
			logger.debug("result of js command of field " + fm.getSourceName() + " = " + value);
		return value;
	}

	private int performUpdate(PoDestinationConnectorInterface destConn, PoConnectorLink link, List<Object> res, int noOfUpdates,
			Map<String, Object> sourceObjMap, Map<String, Object> destObjMap) {
		List<String> destMappingFieldList = link.getMappingFieldsAsList(false);
		if (logger.isDebugEnabled())
			logger.debug("update given Object!");
		
		// remove fields from destination object map which
		// should only be written on insert (=new entries)
		Iterator<String> srcFieldsIterator = sourceObjMap.keySet().iterator();
		while (srcFieldsIterator.hasNext() ) {
		    String sourceFieldName = srcFieldsIterator.next(); // the source field name
		    
		    if (link.getMappingFieldsAsMap(true).containsKey(sourceFieldName)) {
		    	// get mapping
		        PoFieldMapping myMapping = link.getMappingFieldsAsMap(true).get(sourceFieldName);
		        if (myMapping.isWriteOnInsertOnly()) {
		        	// remove field from destination object map with given name
		        	destObjMap.remove(myMapping.getDestinationName());
		        	destMappingFieldList.remove(myMapping.getDestinationName());
		        }
		    }
		}
		
		res.add(destConn.saveObject(destObjMap,link.getPrimaryKey(false), destMappingFieldList));
		
		noOfUpdates++;
		return noOfUpdates;
	}

	private Object applyConverter(PoConnectorLink link, Map<String, AutomaticConverter> automaticConverters, 
			List<Map<String, Object>> srcResultSet, Map<String, Object> sourceObjMap, PoFieldMapping fm, Object value) {

        if (fm.getConverter() != null ) {

			if (AUTOMATIC.equals(fm.getConverter())) {
				
				AutomaticConverter automaticConverter = automaticConverters.get(getMappingKey(fm));
				
				if (automaticConverter != null) {
					try {
						value = automaticConverter.convert(value);
						addConvertedValueAsEntry(sourceObjMap, fm.getSourceName(), value);
					} catch (Exception e) {
						logger.warn("Could not convert '" + value + "' using " + AUTOMATIC + " " + automaticConverter.toString() + " pos: " + srcResultSet.indexOf(sourceObjMap) + " sourceFieldName " + fm.getSourceName()) ;
					}
	            }
			} else {
				Converter converter = (Converter) appCtx.getBean(fm.getConverter());
				
				// locale determination
				String localeString = getConverterLocale(link, fm);
				// do not set any default to locale - empty locale is for backward compatibility
				try {
					value = converter.convert(value, fm.getConverterPattern(), localeString);
					addConvertedValueAsEntry(sourceObjMap, fm.getSourceName(), value);
				} catch (Exception e) {
					logger.warn("Could not convert '" + value + "' using converter " + converter.getName() + ", pattern " + fm.getConverterPattern() + " and locale " + localeString + " pos: " + srcResultSet.indexOf(sourceObjMap) + " sourceFieldName " + fm.getSourceName()) ;
				}
			}
        }
		return value;
	}

	private void performSynchronizedDeletions(PoDestinationConnectorInterface destConn, PoConnectorLink link,
			List<Object> synchronizedPrimaryKeysOfDestinationMap) {
		
		Iterator<Map<String, Object>> srcResultSetIterator;
		try {
			List<String> fieldList = new ArrayList<String>();
			fieldList.add(link.getPrimaryKey(false));
			// get all Objects with the constraint of the destination connector
			String destConstraint = evaluateVelocityConstructsInConstraint(link.getDestinationConstraint());
			List<Map<String, Object>> destObjects = destConn.findAllObjects(fieldList, destConstraint);
			
			// extract the primary keys (as defined in the destination connector)
			// into a list
			List<Object> destListOfPKs = new ArrayList<Object>();
			srcResultSetIterator = destObjects.iterator();
			while (srcResultSetIterator.hasNext()) {
				Object pk = (srcResultSetIterator.next()).get(link.getPrimaryKey(false));
				if (pk!=null)
					destListOfPKs.add(pk);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("sourceListOfFKs=" + synchronizedPrimaryKeysOfDestinationMap);
				logger.debug("destListOfPKs=" + destListOfPKs);
			}
			
			// FIXME: WHY sort the lists // additionally this causes problems as a list of Objets cannot be sorted, when type is not known
			// Collections.sort(synchronizedPrimaryKeysOfDestinationMap);
			// Collections.sort(destListOfPKs);
			
			// remove all entries, where a corresponding entry in the
			// source connector exists (assumes that the sourceListOfFKs is of the same
			// logical field as the primarykey in the destination connector)
			if (destListOfPKs.removeAll(synchronizedPrimaryKeysOfDestinationMap)) {
				// there are records in the destination connector
				// which are not contained in the source connector!
				if (destListOfPKs.size()>0) {
		    		logger.info("Records to delete are found in the destination connector: " + destListOfPKs.size() + " items.");
		    		
		    		Iterator<Object> destResultSetIterator = destListOfPKs.iterator();
		    		while (destResultSetIterator.hasNext()) {
		    			Object key =  destResultSetIterator.next();
		    			if (logger.isDebugEnabled())
		    				logger.debug("delete record from destination connector with id=" + key);
		    			
		    			try {
		    				destConn.deleteObject((String)key, link.getPrimaryKey(false));
		    			} catch (Exception deleteException) {
		    				logger.info("Deletion of object with key " + key + " did not work, due to " + 
		    						deleteException.getMessage() + ". Enable debug to see the stack trace");
		    				if (logger.isDebugEnabled())
		    					logger.debug(deleteException, deleteException);
		    			}
		    		}
		    		logger.info("finished deleting " + destListOfPKs.size() + " records in the destination connector!");
				} else
					logger.info("no records to delete found in destination connector!");
			}
		} catch (Exception e) {
			logger.warn("Exception happend while trying to sync deletions " + e.getMessage());
			if (logger.isInfoEnabled())
					logger.error(e);
		}
	}

	private Map<String, AutomaticConverter> getAutomaticConverters(PoSourceConnectorInterface sourceConn, PoDestinationConnectorInterface destConn,
			PoConnectorLink link) {
		
		Map<String, AutomaticConverter> automaticConverters = new HashMap<String, AutomaticConverter>();
    	// are assigned only when types of both source and destination can be determined
    	if (sourceConn instanceof FieldTypeAwareConnector && destConn instanceof FieldTypeAwareConnector) {
    		
    		FieldTypeAwareConnector source = (FieldTypeAwareConnector) sourceConn;
    		FieldTypeAwareConnector dest = (FieldTypeAwareConnector) destConn;
    		
    		for (PoFieldMapping mapping : link.getMappingFields()) {
    			String fieldMapping = "For mapping " + mapping.getSourceName() + " to " + mapping.getDestinationName();
    			if (AUTOMATIC.equals(mapping.getConverter())) {
					Class<?> sourceClass = source.getTypeOfField(mapping.getSourceName());
					Class<?> destClass = dest.getTypeOfField(mapping.getDestinationName());
					
					AutomaticConverter converter = AutomaticConverterFactory.getConverter(sourceClass, destClass, getConverterLocale(link, mapping));
					if (converter != null) { // automatic converter exists
						String mappingKey = getMappingKey(mapping);
						automaticConverters.put(mappingKey, converter);
						logger.info(fieldMapping + " instantiating converter " + converter.toString());
					}
    			} else if (StringUtils.isNotBlank(mapping.getConverter())) {
    				logger.info(fieldMapping + " retrieving converter " + mapping.getConverter());
    			}
    		}
    	}
		return automaticConverters;
	}
	
	private String getConverterLocale(PoConnectorLink link, PoFieldMapping mapping) {
		if (StringUtils.isEmpty( mapping.getConverterLocale() ) == false) 
			return mapping.getConverterLocale();
		
		if ( StringUtils.isEmpty(link.getLocale())==false ) {
			return link.getLocale();
		}
		
		return Locale.getDefault().toString();
	}

	private Map<String, NullStringConverter> getNullStringConverters(PoSourceConnectorInterface sourceConn, PoDestinationConnectorInterface destConn,
			PoConnectorLink link) {
		
		Map<String, NullStringConverter> nullStringConverters = new HashMap<String, NullStringConverter>();
    	// assigned only to String source mappings
    	if (link.isConvertNullToEmptyString() && sourceConn instanceof FieldTypeAwareConnector) {
    		FieldTypeAwareConnector source = (FieldTypeAwareConnector) sourceConn;
    		FieldTypeAwareConnector dest = null;
    		if (destConn instanceof FieldTypeAwareConnector)
    			dest = (FieldTypeAwareConnector) destConn;
    		for (PoFieldMapping mapping : link.getMappingFields()) {
				Class<?> sourceClass = source.getTypeOfField(mapping.getSourceName());
				Class<?> destClass = null;
				if (dest != null)
					destClass = dest.getTypeOfField(mapping.getDestinationName());
    			if (String.class == sourceClass) {
    				NullStringConverter nullConverter = new NullStringConverter(destClass);
    				// one source can be connected to more than one destinations
    				nullStringConverters.put(getMappingKey(mapping), nullConverter);
					logger.info("For mapping " + mapping.getSourceName() + " to " + mapping.getDestinationName() + 
							" instantiating " + nullConverter.toString());
    			}
    		}
    	}
		return nullStringConverters;
	}
    
    private String toStringOrNull(Object obj) {
    	if (obj == null)
    		return null;
    	
    	return obj.toString();
    }

	private void addConvertedValueAsEntry(Map<String, Object> sourceObjMap, String originalSourceName, Object value) {
		if (originalSourceName!=null)
			sourceObjMap.put(originalSourceName + AFTERCONVERSION_POSTFIX, value);
	}
	
	private void addScriptedValueAsEntry(Map<String, Object> sourceObjMap, String originalSourceName, Object value) {
		if (originalSourceName!=null)
			sourceObjMap.put(originalSourceName + AFTERSCRIPT_POSTFIX, value);
	}
    
    private void removePostProcessedConvertedValues(Map<String, Object> sourceObjMap) {
    	List<String> keys2Remove = new ArrayList<String>();
    	for (String key : sourceObjMap.keySet()) {
    		if (key.endsWith(AFTERCONVERSION_POSTFIX) || key.endsWith(AFTERSCRIPT_POSTFIX)) {
    			keys2Remove.add(key);
    		}
    	}
    	for (String key : keys2Remove)
    		sourceObjMap.remove(key);
	}

	/**
     * @param link a PoConnectorLink object
     * @return a <code>List</code> of fieldnames.
     */
    protected List<String> getFieldNamesFromLink(PoConnectorLink link, boolean isSource) {
        List<String> fieldNames = new ArrayList<String>();
        Iterator<PoFieldMapping> i=link.getMappingFields().iterator();
        while (i.hasNext()) {
            PoFieldMapping fm= i.next();
            if (!isSource) { 
                fieldNames.add(fm.getDestinationName());
            } else {
                fieldNames.add(fm.getSourceName());
            }
        }
        return fieldNames;
    }
    
	@Override
	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		this.appCtx = appCtx;
	}

	public void setConnectorService(PoConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	/** delegates to synchronise(link, null). */
	@Override
	public void synchronise(PoConnectorLink link) {
		synchronise(link, null);
	}

	@Override
	public void synchronise(PoConnectorLink link, String addSourceConstraint) {
		synchronise(link, addSourceConstraint,null,null);
	}

	public void setScriptingService(PoScriptingService scriptingService) {
		this.scriptingService = scriptingService;
	}
}
