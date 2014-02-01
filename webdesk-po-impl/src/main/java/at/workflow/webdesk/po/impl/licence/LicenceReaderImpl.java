package at.workflow.webdesk.po.impl.licence;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import at.workflow.webdesk.po.licence.Licence;
import at.workflow.webdesk.po.licence.LicenceReader;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.po.model.LicenceDefinition.LicenceCheckType;
import at.workflow.webdesk.tools.licencing.util.ByteArray;
import at.workflow.webdesk.tools.security.WebdeskKeystore;

/**
 * Is able to read Licence Definitions and Licences.
 * Both are hold in a bytearray as private variables.
 * 
 * @author hentner, ggruber
 */
@SuppressWarnings("deprecation")
public class LicenceReaderImpl implements LicenceReader {

	private static final Logger logger = Logger.getLogger(LicenceReaderImpl.class);
	
	private static final String RUNNING_MODE = "org.apache.cocoon.mode";	// fri_2013-10-30: this name has been duplicated 7 times in Webdesk 

	private ByteArray licenceDef = new ByteArray();
	private boolean wellformed = false;
	private ByteArray licence = new ByteArray();
	private Properties webdeskProps = new Properties();

	private String company;
	private String companyDesc;

	private int counter = 0;

	private ArrayList<LicenceDefinition> licenceDefinitions;

	private HashMap<String, Licence> licenceMap;
	

	/** Reads the encrypted Files and stores the result in ByteArrays. */
	public LicenceReaderImpl() {
		readWebdeskProps();
		load();
		readLicence();
		readLicenceDefinitions();
	}

	@Override
	public String getCompany() {
		return company;
	}

	@Override
	public String getCompanyDesc() {
		return companyDesc;
	}

	/**
	 * @see at.workflow.webdesk.po.impl.licence.LicenceReader#getLicenceDefinitions()
	 */
	@Override
	public ArrayList<LicenceDefinition> getLicenceDefinitions() {
		return licenceDefinitions;
	}

	/**
	 * @see at.workflow.webdesk.po.impl.licence.LicenceReader#getLicenceMap()
	 */
	@Override
	public HashMap<String, Licence> getLicenceMap() {
		return licenceMap;
	}


	private void readWebdeskProps() {
		Resource r = new ClassPathResource("webdesk.properties");
		if (r.exists()) {
			try {
				this.webdeskProps.load(r.getURL().openStream());
			} catch (Exception e) {
				logger.warn("problems loading webdesk.properties from classpath...");
			}
		} else {
			logger.warn("no webdesk.properties found in classpath...");
		}
	}

	/**
	 * This function extracts a block of bytes inside the bytearray
	 * <code>source</code>. The resulting byte arry has a size of
	 * <code>end - begin</code>.
	 * 
	 * @param begin
	 * @param end
	 * @param source
	 * @return
	 */
	private byte[] extract(int begin, int end, byte[] source) {
		byte[] res = new byte[end - begin];
		int counter = 0;
		while (begin < end && begin < source.length) {
			res[counter] = source[begin];
			counter++;
			begin++;
		}
		return res;
	}

	/**
	 * 
	 * This function returns the bytes of an inputstream
	 * 
	 * @param is
	 *            InputStream
	 * @return a byte array containing the bytes of the inputstream
	 * 
	 *         throws IoException, PoRuntimeException
	 */
	private byte[] getBytesFromIS(InputStream is) {
		try {
			long length = is.available();

			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				throw new RuntimeException("File is too large.");
			}
			// Create the byte array to hold the data
			byte[] bytes = new byte[(int) length];
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file.");
			}
			// Close the input stream and return bytes
			is.close();
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads the encrypted files from the filesystem and stores the result in
	 * various byte-arrays (private var's of this class)
	 */
	private void load() {
		try {
			//Security.addProvider(new BouncyCastleProvider());
			//final Cipher decrypting = Cipher.getInstance("RSA", "BC");
			final Cipher decrypting = new WebdeskKeystore().newCipherInstance(Cipher.DECRYPT_MODE);

			// load LicenceDefinitions
			Set<Resource> uniqueSet = getUniqueResources("classpath*:/at/workflow/**/*.ldef");
			counter += uniqueSet.size();
			writePlainTextToByteArray(uniqueSet, decrypting, licenceDef);
			licenceDef = makeWellFormedXML();

			try { // search for licences, have to be in root path and have to
					// have form of licence*.lic
				uniqueSet = getUniqueResources("classpath*:/licence*.lic");
				writePlainTextToByteArray(uniqueSet, decrypting, licence);

				logger.debug("licenceDef: " + new String(licenceDef.toByteArray()));
				logger.debug("licence: " + new String(licence.toByteArray()));
			}
			catch (FileNotFoundException fne) {
				logger.warn("Could not find licence file 'licence.lic'. No Modules will be loaded.");
			}
		} catch (Exception e) {
			logger.error(e, e);
			throw new RuntimeException(e);
		}
	}

	private void writePlainTextToByteArray(Set<Resource> uniqueSet, Cipher decrypting, ByteArray licenceDef) throws IOException, IllegalBlockSizeException, BadPaddingException {
		for (final Resource r : uniqueSet) {
			final byte[] encr = this.getBytesFromIS(r.getInputStream());

			for (int i = 0; i < encr.length; i += 128) {
				byte[] ciphertext = decrypting.doFinal(extract(i, i + 128, encr));
				for (int k = 0; k < ciphertext.length; k++) {
					if (ciphertext[k] == 0)
						ciphertext[k] = ' ';
				}
				licenceDef.write(ciphertext);
			}
		}
		
		// now we have the def in plain text xml-format stored in the licenceDef array
	}

	private Set<Resource> getUniqueResources(String springPattern) throws IOException {
		PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourceResolver.getResources(springPattern);
		return new HashSet<Resource>(Arrays.asList(resources));
	}

	/**
	 * @return a list of LicenceDefinition (already filled with the amount
	 *         [can be found in the licence file])
	 * 
	 *         Uses the previously loaded byte-arrays (see load()), parses the
	 *         xml-structure and properly fills the LicenceDefinition objects.
	 * 
	 *         The licence file itself is used to determine wether a definition
	 *         is added
	 *         or not.
	 */
	void readLicenceDefinitions() {

		logger.info("Read Licence " + licenceDef.size());

		this.licenceDefinitions = new ArrayList<LicenceDefinition>();
		
		if (licenceDef.size() > 0) {
			
			
			// and parse those to LicenceDefinition objects
			try {
				parseLicenceDefXMLFile(new ByteArrayInputStream(this.licenceDef.toByteArray()));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if ((isRunningModeDev(webdeskProps) || hasNoRunningMode(webdeskProps)) && hasLicenceXMLSources()) {
			
			// in this case we are loading the unencrypted licence
			// definitions
			// directly (which are *NOT* in the production build!!!)
			// This will only work if called from eclipse!
			readLicenceDefinitionsFromSource();
			
		} 
		
		fillLicenceDefinitionsWithActualAmounts();
	}

	private boolean hasNoRunningMode(Properties webdeskProps) {
		return System.getProperty(RUNNING_MODE)==null && webdeskProps.getProperty(RUNNING_MODE)==null;
	}
	
	private boolean hasLicenceXMLSources() {
		boolean hasLicenceXMLSources = false;
		PathMatchingResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
		try {
			hasLicenceXMLSources = ( rpr.getResources("classpath*:/at/workflow/**/*.ldef.xml").length > 0 );
		} catch (Exception e) {
		}
		return hasLicenceXMLSources;
	}

	void readLicenceDefinitionsFromSource() { 
	
		if (licenceDefinitions==null)
			licenceDefinitions = new ArrayList<LicenceDefinition>();

		PathMatchingResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
		try {
			Resource[] ress = rpr.getResources("classpath*:/at/workflow/**/*.ldef.xml");

			for (Resource res : ress) {
				try {
					parseLicenceDefXMLFile(res.getInputStream());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void parseLicenceDefXMLFile(InputStream stream) {
		SAXBuilder myBuilder = new SAXBuilder();
		try {
			Document doc = myBuilder.build(stream);
			@SuppressWarnings("unchecked")
			List<Element> elements = XPath.selectNodes(doc, "//licence-defs/licence");
			Iterator<Element> myItr = elements.iterator();
			while (myItr.hasNext()) {
				Element element = myItr.next();
				LicenceDefinition licDef = generateLicenceDefinition(element);
				addLicenceDefinition(licDef);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// reset licence definitions
			this.licenceDefinitions.clear();
		}

	}
	
	private void addLicenceDefinition(LicenceDefinition licDef) {
		if (licenceDefinitions!=null && licenceDefinitions.contains(licDef)==false)
			licenceDefinitions.add(licDef);
	}

	@SuppressWarnings("unchecked")
	private LicenceDefinition generateLicenceDefinition(Element element) {
		LicenceDefinition licDef = new LicenceDefinition();
		licDef.setName(element.getChildText("name"));

		licDef.setModules(new ArrayList<String>());
		if (element.getChild("modules") != null) {
			String[] splitModules = element.getChildText("modules").split(",");
			for (int i = 0; i < splitModules.length; i++) {
				licDef.getModules().add(splitModules[i].trim());
			}
		} else if (element.getChild("module") != null) {
			licDef.getModules().add(element.getChildText("module"));
		}

		Element contexts = element.getChild("contexts");
		// extract path information from modules
		if (contexts != null) {
			Iterator<Element> contextI = contexts.getChildren("context").iterator();
			while (contextI.hasNext()) {
				Element context = contextI.next();
				licDef.getContexts().add(context.getTextTrim());
			}
		}
		Element actions = element.getChild("actions");
		if (actions != null)
			if (actions.getChildren("action") != null) {
				Iterator<Element> actionsI = actions.getChildren("action").iterator();
				while (actionsI.hasNext()) {
					Element action = actionsI.next();
					licDef.getActions().add(action.getText());
				}
			}
		if (element.getChildText("isNegativeList") != null)
			licDef.setNegativeList(new Boolean(element
					.getChildText("isNegativeList")).booleanValue());
		licDef.setSql(element.getChildText("sql"));
		
		String sqlIsInvolved = element.getChildText("sqlIsInvolved");
		licDef.setTriggerMethodsForVolumeLicencing(StringUtils.commaDelimitedListToSet(sqlIsInvolved));
		
		if (element.getChildText("checkType") != null) {
			licDef.setCheckType(LicenceCheckType.byCode(element.getChildText("checkType")));
		}

		licDef.setWhichDb(element.getChildText("whichDb"));
		
		if (element.getChildText("extends") != null)
			licDef.setExtendedLicence(element.getChildText("extends"));

		if (element.getChildText("dialect") != null)
			licDef.setDialect(element.getChildText("dialect"));
		else
			licDef.setDialect("sql");

		return licDef;
	}
	
	private void fillLicenceDefinitionWithActualAmount(LicenceDefinition licDef) {
		// if a licence is given, set the allowed amount
		// this depends on the licences!!!!
		if (licenceMap.containsKey(licDef.getName())) {
			// the licence was stated directly
			Licence licence = licenceMap.get(licDef.getName());
			if (licence.getAmount()>0)
				licDef.setCurrentlyLicencedAmount(licence.getAmount());
		} else {
			// here we are setting the amount from an
			// licencedefinition where we are inheriting from...
			// so the licence was *NOT* stated inside the licence.lic
			for (LicenceDefinition licDefToInheritFrom : findLicenceDefinitionExtensions(licDef)) {
				if (licenceMap.containsKey(licDefToInheritFrom.getName())) {
					Licence licence = licenceMap.get(licDefToInheritFrom.getName());
					if (licence.getAmount()>0 && licDef.getCurrentlyLicencedAmount() < licence.getAmount())
						licDef.setCurrentlyLicencedAmount(licence.getAmount());
				}
			}
		}
	}
	
	private List<LicenceDefinition> findLicenceDefinitionExtensions(LicenceDefinition licDef) {
		List<LicenceDefinition> ret = new ArrayList<LicenceDefinition>();
		if (licenceDefinitions!=null) {
			for (LicenceDefinition otherLicDef : licenceDefinitions) {
				if (!otherLicDef.equals(licDef) && otherLicDef.getExtendedLicence()!=null && otherLicDef.getExtendedLicence().equals(licDef.getName())) {
					ret.add(otherLicDef);
				}
			}
		}
		return ret;
	}
	
	void fillLicenceDefinitionsWithActualAmounts() {
		if (licenceDefinitions!=null)
			for (LicenceDefinition licDef : licenceDefinitions) {
				fillLicenceDefinitionWithActualAmount(licDef);
			}
	}

	private ByteArray makeWellFormedXML() {
		
		if (wellformed)
			return licenceDef;
		
		byte[] ba;
		StringBuffer sb = new StringBuffer();
		ByteArray byteArray = null;
		try {
			ba = licenceDef.toByteArray();
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ba)));
			String str = "";
			int actCount = 0;
			// delete root node if necessary (the actual byte array may consist
			// of severeal licence definition files.
			while ((str = br.readLine()) != null) {
				if (str.indexOf("<licence-defs>") != -1) {
					if (actCount == 0)
						sb.append(str + "\n");
					actCount++;
				} else if (str.indexOf("</licence-defs>") != -1) {
					if (actCount == counter)
						sb.append(str + "\n");
				} else
					sb.append(str + "\n");
			}

			byteArray = new ByteArray();
			byteArray.write(sb.toString().getBytes());
		}
		catch (IOException e) {
			throw new RuntimeException("Can't read licence definitions correctly.");
		}
		wellformed = true;
		return byteArray;
	}

	/**
	 * read licence from bytearray to licenceMap<String, Licence>
	 */
	private void readLicence() {

		licenceMap = new HashMap<String, Licence>();
		String line = "";

		if (this.licence.size() > 0) {
			// read licences from licence.lic!
			try {
				BufferedReader is = new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(this.licence.toByteArray())));
				while ((line = is.readLine()) != null) {
					createLicence(line);
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
		} else {
			// simulation of licences in DEV mode only
			Resource r = new ClassPathResource("webdesk.properties");
			if (r.exists()) {
				Properties props = new Properties();
				try {
					props.load(r.getInputStream());
					String licenceString = props.getProperty("webdesk.licenceCheckSimulation");
					if (licenceString != null && !"".equals(licenceString) && isRunningModeDev(props)) {
						createLicencesFromString(licenceString);
					}
				} catch (IOException e) {
					logger.warn("could not load webdesk.properties", e);
				}
			}

		}
	}
	
	void createLicencesFromString(String licenceString) {
		
		licenceMap.clear();
		
		String[] lines = licenceString.split(",");
		for (int i = 0; i < lines.length; i++) {
			createLicence(lines[i]);
		}
	}

	private boolean isRunningModeDev(Properties props) {
		return ("dev".equals(props.getProperty(RUNNING_MODE)) || "dev".equals(System.getProperty(RUNNING_MODE)));

	}

	private void createLicence(String line) {
		if (line.indexOf("=") != -1 && !line.startsWith("#")) {
			Licence licence = new Licence();
			String name = line.substring(0, line.indexOf("="));
			licence.setName(name);

			if (line.indexOf(";") != -1) {
				String amount = line.substring(line.indexOf("=") + 1, line.indexOf(";"));
				licence.setAmount(new Integer(amount.trim()).intValue());
				// expires
				String expires = "";
				try {
					expires = line.substring(line.lastIndexOf("=") + 1, line.length());
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date expireD = df.parse(expires);
					licence.setExpires(expireD);
				} catch (ParseException nfe) {
					logger.warn("Could not read date:" + expires);
				}
				licenceMap.put(name, licence);
			} else {
				// it could be company or companyDesc
				if (!name.equals("company") && !name.equals("companyDesc")) {
					String amount = line.substring(line.indexOf("=") + 1, line.length());
					try {
						licence.setAmount(new Integer(amount.trim()).intValue());
					} catch (Exception e) {
						logger.warn("could not parse number of licences for licence=" + name, e);
						licence.setAmount(0);
					}
					licenceMap.put(name, licence);
				} else {
					if (name.equals("company"))
						this.company = line.substring(line.indexOf("=") + 1, line.length());
					else
						this.companyDesc = line.substring(line.indexOf("=") + 1, line.length());
				}
			}
		}
	}


}
