package at.workflow.webdesk.po.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import at.workflow.webdesk.po.PoRuntimeException;

/**
 * Spring bean for configuring password quality.
 * 
 * @author fritzberger 21.10.2010
 */
public class PoPasswordQuality {

	/** This value (-1) says that the password will never expire. This is an alternative to Integer.MAX_VALUE. */
	public static final int PASSWORD_NEVER_EXPIRES = -1;
	
	private static final String EASY_TO_CREATE_SPECIAL_CHARS = "<>,;.:-_#'+*?=()/&%$\"!@";
	private static final String HARD_TO_CREATE_SPECIAL_CHARS = "\\~{}[]´`|³²^°€§";
	
	/** All characters regarded as special password characters. */
	public static final String SPECIAL_CHARS = EASY_TO_CREATE_SPECIAL_CHARS + HARD_TO_CREATE_SPECIAL_CHARS;
	
	private static final int RECOMMENDED_SAFE_LENGTH = 8;
	private static final String ASCII_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DIGIT_CHARS = "0123456789";
	private static final Random random = new Random();
	
	private int validityDays;
	private int minimalLength;
	private boolean requiresUpperAndLowerCharacters;
	private int minimalDigitsCount;
	private int minimalSpecialCharactersCount;
	private int numberOfDifferingLatestPasswords;

	/**
	 * Spring access.
	 * @return non-zero positive integer or PoPasswordService.PASSWORD_NEVER_EXPIRES (-1) for infinity.
	 */
	public int getValidityDays() {
		return validityDays;
	}

	/**
	 * Spring access.
	 * @param validityDays non-zero positive integer or PoPasswordService.PASSWORD_NEVER_EXPIRES (-1) for infinity.
	 */
	public void setValidityDays(int validityDays) {
		this.validityDays = validityDays;
	}

	/** Spring access. */
	public int getMinimalLength() {
		return minimalLength;
	}

	/** Spring access. */
	public void setMinimalLength(int minimalLength) {
		this.minimalLength = minimalLength;
	}

	/** Spring access. */
	public boolean isRequiresUpperAndLowerCharacters() {
		return requiresUpperAndLowerCharacters;
	}

	/** Spring access. */
	public void setRequiresUpperAndLowerCharacters(boolean requiresUpperAndLowerCharacters) {
		this.requiresUpperAndLowerCharacters = requiresUpperAndLowerCharacters;
	}

	/** Spring access. */
	public int getMinimalDigitsCount() {
		return minimalDigitsCount;
	}

	/** Spring access. */
	public void setMinimalDigitsCount(int minimalDigitsCount) {
		this.minimalDigitsCount = minimalDigitsCount;
	}

	/** Spring access. */
	public int getMinimalSpecialCharactersCount() {
		return minimalSpecialCharactersCount;
	}

	/** Spring access. */
	public void setMinimalSpecialCharactersCount(int minimalSpecialCharactersCount) {
		this.minimalSpecialCharactersCount = minimalSpecialCharactersCount;
	}

	/** Spring access. */
	public int getNumberOfDifferingLatestPasswords() {
		return numberOfDifferingLatestPasswords;
	}

	/** Spring access. */
	public void setNumberOfDifferingLatestPasswords(int numberOfDifferingLatestPasswords) {
		this.numberOfDifferingLatestPasswords = numberOfDifferingLatestPasswords;
	}


	/**
	 * This is public only due to the fact that its test case is another package.
	 * @param password the password to check for quality.
	 * @param newestPasswords the latest passwords to check against the created password.
	 * @return null when conformant, else an message describing the error.
	 */
	public String conforms(String password, List<String> newestPasswords) {
		if (password.length() < getMinimalLength())	{
			return MessageFormat.format(PoRuntimeException.ERROR_PASSWORDQUALITY_MIN_LENGTH, getMinimalLength());
		}
		
		if (isRequiresUpperAndLowerCharacters() && hasUpperAndLowerCharacters(password) == false)	{
			return PoRuntimeException.ERROR_PASSWORDQUALITY_UPPER_LOWER_CHARS;
		}
		
		if (getMinimalDigitsCount() > 0)	{
			if (countDigits(password) < getMinimalDigitsCount())
				return MessageFormat.format(PoRuntimeException.ERROR_PASSWORDQUALITY_MIN_DIGITS, getMinimalDigitsCount());
		}
		
		if (getMinimalSpecialCharactersCount() > 0)	{
			if (countSpecialChars(password) < getMinimalSpecialCharactersCount())
				return MessageFormat.format(PoRuntimeException.ERROR_PASSWORDQUALITY_MIN_SPECIALCHARS, getMinimalSpecialCharactersCount());
		}
		
		if (getNumberOfDifferingLatestPasswords() > 0 && newestPasswords.size() > 0)	{
			for (String oldPassword : newestPasswords.subList(0, Math.min(getNumberOfDifferingLatestPasswords(), newestPasswords.size())))	{
				if (oldPassword.equals(password))
					return MessageFormat.format(PoRuntimeException.ERROR_PASSWORDQUALITY_NUMBER_OF_DIFFERING_LATEST, getNumberOfDifferingLatestPasswords());
			}
		}
		
		return null;
	}
	
	/**
	 * This is public only due to the fact that its test case is another package.
	 * @param password the password to check for quality.
	 * @return null when conformant, else an message describing the error.
	 */
	public String createConformantRandomPassword(List<String> newestPasswords) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < getMinimalDigitsCount(); i++)
			sb.append(randomDigit());
		
		if (isRequiresUpperAndLowerCharacters())	{
			sb.append(Character.toLowerCase(randomLetter()));
			sb.append(Character.toUpperCase(randomLetter()));
		}
		
		for (int i = 0; i < getMinimalSpecialCharactersCount(); i++)
			sb.append(randomSpecialChar());
		
		// we must conform to minimal length
		int targetLength = Math.max(getMinimalLength(), sb.length());
		// we should not make a password shorter than recommended
		targetLength = Math.max(RECOMMENDED_SAFE_LENGTH, targetLength);
		
		while (sb.length() < targetLength)
			sb.append(randomDigit());
		
		String password = sb.toString();
		
		int count = 0;
		for (String oldPassword : newestPasswords)	{
			if (count < getNumberOfDifferingLatestPasswords() && oldPassword.equals(password))	{
				return createConformantRandomPassword(newestPasswords);
			}
			count++;
		}
		
		return password;
	}


	private char randomLetter()	{
		return ASCII_LETTERS.charAt(Math.abs(random.nextInt(ASCII_LETTERS.length())));
	}
	
	private char randomDigit()	{
		return DIGIT_CHARS.charAt(Math.abs(random.nextInt(DIGIT_CHARS.length())));
	}
	
	private char randomSpecialChar()	{
		return EASY_TO_CREATE_SPECIAL_CHARS.charAt(Math.abs(random.nextInt(EASY_TO_CREATE_SPECIAL_CHARS.length())));
	}
	
	private int countSpecialChars(String password) {
		int count = 0;
		for (int i = 0; i < password.length(); i++)	{
			char c = password.charAt(i);
			if (Character.isLetterOrDigit(c) == false)
				count++;
		}
		return count;
	}

	private int countDigits(String password) {
		int count = 0;
		for (int i = 0; i < password.length(); i++)	{
			char c = password.charAt(i);
			if (Character.isDigit(c))
				count++;
		}
		return count;
	}

	/** @return true either when no letters, or both counts are greater than zero. */
	private boolean hasUpperAndLowerCharacters(String password) {
		int upperCharsCount = 0;
		int lowerCharsCount = 0;
		boolean hasLetters = false;
		
		for (int i = 0; i < password.length(); i++)	{
			final char c = password.charAt(i);
			
			if (Character.isLetter(c))	{
				hasLetters = true;
				
				if (Character.isUpperCase(c))
					upperCharsCount++;
				else if (Character.isLowerCase(c))
					lowerCharsCount++;
			}
		}
		return hasLetters == false || upperCharsCount > 0 && lowerCharsCount > 0;
	}

}
