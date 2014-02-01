package at.workflow.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author DI Harald Entner <br>
 *         logged in as: hentner<br>
 * <br>
 * 
 *         Project: webdesk3.1<br>
 *         created at: 07.08.2007<br>
 *         package: at.workflow.webdesk.tools<br>
 *         compilation unit: FileTools.java<br>
 * <br>
 * 
 *         <p>
 *         This class contains various functions which should help to work with
 *         java.io.File objects.
 * 
 * 
 * 
 *         </p>
 * 
 * 
 */
public class FileTools {

	/**
	 * @param absoluteFilePath
	 * @return true if an empty file with the given
	 *         <code>absoluteFilePath</code> was created, false otherwise.
	 */
	public static boolean createFile(String absoluteFilePath) {
		boolean res = true;
		File f = new File(absoluteFilePath);
		if (absoluteFilePath.indexOf(".") != -1) {
			// path is a file not a directory
			absoluteFilePath = absoluteFilePath.replaceAll("\\\\", "/");
			if (absoluteFilePath.indexOf("/") != -1) {
				String directory = absoluteFilePath.substring(0,
						absoluteFilePath.lastIndexOf("/"));
				File dir = new File(directory);
				if (!dir.exists())
					res = dir.mkdirs();
			}
			if (res)
				if (!f.exists())
					try {
						res = f.createNewFile();
					} catch (Exception e) {
						res = false;
					}
		} else {
			if (!f.exists())
				return f.mkdirs();
		}
		return res;
	}

	/**
	 * Replaces placeholders in the given String. The following placeholders are
	 * considered so far:
	 * 
	 * <ul>
	 * <li>$y</li> Year
	 * <li>$M</li> Month (0=jan!)
	 * <li>$d</li> Day
	 * <li>$h</li> Hour
	 * <li>$m</li> Minute
	 * <li>$s</li> second
	 * 
	 * <li>$yy</li> Year (2 digits)
	 * <li>$yyyy</li> Year (4 digits)
	 * <li>$MM</li> Month (with 1=Jan)
	 * <li>$dd</li> Day (in pattern 00)
	 * <li>$hh</li> hour (in pattern 00)
	 * <li>$mm</li> Minute (in pattern 00)
	 * <li>$ss</li> second (in pattern 00)
	 * 
	 * </ul>
	 * 
	 * 
	 * @param s
	 *            the <code>String</code> which contains placeholders.
	 * @return a <code>String</code>
	 */
	public static String replacePlaceHolders(String s) {

		Date d = new Date();

		s = s.replaceAll("\\$yyyy", dateToFormat(d, "yyyy"));
		s = s.replaceAll("\\$yy", dateToFormat(d, "yy"));
		s = s.replaceAll("\\$MM", dateToFormat(d, "MM"));
		s = s.replaceAll("\\$dd", dateToFormat(d, "dd"));
		s = s.replaceAll("\\$hh", dateToFormat(d, "hh"));
		s = s.replaceAll("\\$HH", dateToFormat(d, "HH"));
		s = s.replaceAll("\\$mm", dateToFormat(d, "mm"));
		s = s.replaceAll("\\$ss", dateToFormat(d, "ss"));

		s = s.replaceAll("\\$y", new Integer(getDateField(d, Calendar.YEAR))
				.toString());
		s = s.replaceAll("\\$M", new Integer(getDateField(d, Calendar.MONTH))
				.toString());
		s = s.replaceAll("\\$d", new Integer(getDateField(d,
				Calendar.DAY_OF_MONTH)).toString());
		s = s.replaceAll("\\$h", new Integer(getDateField(d,
				Calendar.HOUR_OF_DAY)).toString());
		s = s.replaceAll("\\$m", new Integer(getDateField(d, Calendar.MINUTE))
				.toString());
		s = s.replaceAll("\\$s", new Integer(getDateField(d, Calendar.SECOND))
				.toString());
		return s;
	}

	private static String dateToFormat(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	private static int getDateField(Date date, int field) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(field);
	}

	public static byte[] readFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			System.err.println("File is too large : " + file.getPath());
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
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
