package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * List of DbRecords, proving just the methods really needed.
 * This is immutable, accepting data through construction only.
 * 
 * @author fritzberger 12.06.2012
 */
public class DbRecordList implements Iterable<DbRecord> {
	
	private List<DbRecord> records = new ArrayList<DbRecord>();

	/** Arbitrary number of DbRecord arguments. */
	public DbRecordList(DbRecord ... records)	{
		for (DbRecord record : records)
			this.records.add(record);
	}

	/** List of DbRecord to clone. */
	public DbRecordList(List<DbRecord> records)	{
		for (DbRecord record : records)
			this.records.add(record);
	}
	
	
	public DbRecord [] toArray()	{
		return records.toArray(new DbRecord [records.size()]);
	}

	public List<DbRecord> toList()	{
		return new ArrayList<DbRecord>(records);
	}

	/** Implements Iterable by returning an Iterator over a list clone. */
	@Override
	public Iterator<DbRecord> iterator() {
		return toList().iterator();
	}

	/** Overridden to delegate to private records List. */
	@Override
	public int hashCode() {
		return records.hashCode();
	}
	
	/** Overridden to delegate to private records List. */
	@Override
	public boolean equals(Object other) {
		return records.equals(((DbRecordList) other).records);
	}

}
