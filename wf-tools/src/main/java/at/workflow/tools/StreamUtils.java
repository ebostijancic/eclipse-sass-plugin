package at.workflow.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class StreamUtils {

	public static final String UTF_8 = "utf-8";
	public static final Charset UTF_8_CHARSET = Charset.forName(UTF_8);

	/** @return ByteArrayInputStream of string */
	public static InputStream getInputStream(String string) {
		return new ByteArrayInputStream(string.getBytes());
	}
	
	/** @return ByteArrayInputStream to the utf8 encoded string */
	public static InputStream getInputStreamFromUtf8String(String utf8EnodedString) {
		return new ByteArrayInputStream(utf8EnodedString.getBytes(UTF_8_CHARSET));
	}
	
	/** @return String representation of the input stream content */
	public static String getStreamAsString(InputStream inputStream) {
		
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(inputStream, writer, null); // null is important as encoding
		} catch (IOException e) {
			final String msg = "Exception while reading input stream " + e;
			throw new RuntimeException(msg, e);
		}
		return writer.toString();
	}
	
	/** @return byte array representation of the input stream content */
	public static byte[] getStreamAsByteArray(InputStream inputStream) {
		return getStreamAsString(inputStream).getBytes();
	}
	
	/** @return utf-8 encoded byte array representation of the input stream content */
	public static byte[] getStreamAsUtf8ByteArray(InputStream inputStream) {
		return getStreamAsString(inputStream).getBytes(UTF_8_CHARSET);
	}
}
