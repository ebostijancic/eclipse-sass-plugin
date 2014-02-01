package at.workflow.webdesk.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests and specifies the DbRecord capacities.
 * 
 * @author fritzberger 2012-06-12
 */
public class WTestDbRecord extends TestCase {
	
	public void testEmptyDbRecord() {
		DbRecord record = new DbRecord((Map<String,Object>) null);
		assertEquals(0, record.size());
	}
	
	public void testGetFromDbRecord() {
		DbRecord record = new DbRecord("eins", new Integer(1), "zwei", new Integer(2));
		assertEquals(2, record.size());
		assertEquals(new Integer(1), record.get("eins"));
		
		Map<String,Object> recordContents = new HashMap<String,Object>();
		recordContents.put("eins", "one");
		recordContents.put("zwei", "two");
		recordContents.put("drei", "three");
		DbRecord record2 = new DbRecord(recordContents);
		assertEquals(recordContents.size(), record2.size());
		assertEquals("one", record2.get("eins"));
		assertEquals("two", record2.get("zwei"));
		assertEquals("three", record2.get("drei"));
	}
	
	public void testDbRecordEntries() {
		DbRecord record = new DbRecord("eins", new Integer(1), "zwei", new Integer(2), "drei", new Integer(3));
		assertEquals(3, record.size());
		for (Map.Entry<String,Object> tuple : record.entries())	{
			assertTrue("eins".equals(tuple.getKey()) || "zwei".equals(tuple.getKey()) || "drei".equals(tuple.getKey()));
			assertTrue(new Integer(1).equals(tuple.getValue()) || new Integer(2).equals(tuple.getValue()) || new Integer(3).equals(tuple.getValue()));
		}
	}

	public void testDbRecordKeysAndValues() {
		DbRecord record = new DbRecord("eins", new Integer(1));
		for (String attributeName : record.keys())	{
			assertTrue("eins".equals(attributeName));
		}
		for (Object value : record.values())	{
			assertTrue(new Integer(1).equals(value));
		}
	}

	public void testDbRecordEquality() {
		DbRecord record1 = new DbRecord("eins", new Integer(1), "zwei", new Integer(2));
		DbRecord record2 = new DbRecord("eins", new Integer(1), "zwei", new Integer(2));
		assertEquals(record1, record2);
	}

	public void testDbRecordHashCode() {
		DbRecord record1 = new DbRecord("eins", new Integer(1), "zwei", new Integer(2));
		DbRecord record2 = new DbRecord("eins", new Integer(1), "zwei", new Integer(2));
		DbRecord record3 = new DbRecord("eins", new Integer(1));
		Set<DbRecord> records = new HashSet<DbRecord>();
		records.add(record1);
		records.add(record2);
		records.add(record3);
		assertEquals(2, records.size());
	}

	public void testTuples() {
		DbRecord.Tuple tuple1 = new DbRecord.Tuple("eins", new Integer(1));
		DbRecord.Tuple tuple2 = new DbRecord.Tuple("eins", new Integer(1));
		assertEquals(tuple1, tuple2);
		
		Set<DbRecord.Tuple> records = new HashSet<DbRecord.Tuple>();
		records.add(tuple1);
		records.add(tuple2);
		records.add(new DbRecord.Tuple("zwei", new Integer(1)));
		assertEquals(2, records.size());
	}

}
