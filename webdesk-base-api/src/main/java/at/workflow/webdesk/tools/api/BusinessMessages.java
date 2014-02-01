package at.workflow.webdesk.tools.api;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Messages the business layer wants to output, which the UI will show.
 * Errors are done via exceptions (BusinessException).
 * 
 * @author fritzberger 16.10.2013
 * @author sdzuban 25.10.2013 changed to I18nMessages
 */
public class BusinessMessages
{
	private Collection<I18nMessage> warnings = new ArrayList<I18nMessage>();
	private Collection<I18nMessage> infos = new ArrayList<I18nMessage>();
	private Collection<Confirm> confirms = new ArrayList<Confirm>();
	

	public void addInfo(String messageI18nKey) {
		infos.add(new I18nMessage(messageI18nKey));
	}

	public void addInfo(I18nMessage i18nMessage) {
		infos.add(i18nMessage);
	}

	public void addWarning(String messageI18nKey) {
		warnings.add(new I18nMessage(messageI18nKey));
	}

	public void addWarning(I18nMessage i18nMessage) {
		warnings.add(i18nMessage);
	}

	public void addAll(BusinessMessages messages) {
		infos.addAll(messages.getInfoMessages());
		warnings.addAll(messages.getWarnings());
	}

	public void addConfirm(Confirm confirm) {
		confirms.add(confirm);
	}

	public Collection<I18nMessage> getInfoMessages() {
		return infos;
	}

	public Collection<I18nMessage> getWarnings() {
		return warnings;
	}

	public Collection<Confirm> getConfirms() {
		return confirms;
	}

	/** @return true then this message container has no messages at all. */
	public boolean isEmpty() {
		return infos.isEmpty() && warnings.isEmpty() && confirms.isEmpty();
	}

	
	
	/**
	 * Confirm message, to be constructed by business layer.
	 * Default possible answers are YES/NO/CANCEL.
	 * After the UI launched the according dialogs, the answer contains the user decision.
	 */
	public static class Confirm extends I18nMessage
	{
		/**
		 * All possible confirm buttons to be pressed by user,
		 * which also are all possible answers.
		 */
		public enum Answer
		{
			YES,
			NO,
			YES4ALL,
			NO4ALL,
			CANCEL,
			OK
		}

		
		/**
		 * Apply this when a confirm depends on the answer of a preceding confirm.
		 * Default required answer is YES.
		 */
		public static class Condition
		{
			public final Confirm confirm;
			public final Answer[] requiredAnswers;

			public Condition(Confirm confirm) {
				this(confirm, new Answer[] { Answer.YES });
			}
			public Condition(Confirm confirm, Answer requiredAnswer) {
				this(confirm, (requiredAnswer != null) ? new Answer[] { requiredAnswer } : null);
			}
			public Condition(Confirm confirm, Answer [] requiredAnswers) {
				assert confirm != null;
				this.confirm = confirm;
				this.requiredAnswers = (requiredAnswers != null) ? requiredAnswers : new Answer [] { Answer.YES };
			}
			
			/** @return true when one of the required answers is equal to the answer of the confirm. */
			boolean matches()	{
				if (confirm.getAnswer() == null)
					throw new IllegalStateException("The confirm "+confirm+" has not been launched, answer is null");
				
				for (Answer requiredAnswer : requiredAnswers)
					if (requiredAnswer == confirm.getAnswer())
						return true;
				
				return false;
			}
		}

		
		
		private final Collection<Condition> dependencies = new ArrayList<Condition>();
		private final Answer [] possibleAnswers;
		
		private Answer answer;

		
		/** Constructor to be called by business layer. Arguments see super-class. */
		public Confirm(String i18nKey, Answer [] possibleAnswers) {
			this(i18nKey, possibleAnswers, (String) null);
		}

		/** Constructor to be called by business layer. Arguments see super-class. */
		public Confirm(String i18nKey, Answer [] possibleAnswers, String ... positionalParameters) {
			this(i18nKey, possibleAnswers, positionalParameters, null);
		}

		/** Constructor to be called by business layer. Arguments see super-class. */
		public Confirm(String i18nKey, Answer [] possibleAnswers, String[] positionalParameters, Boolean[] translationFlags) {
			super(i18nKey, positionalParameters, translationFlags);
			
			this.possibleAnswers = (possibleAnswers != null && possibleAnswers.length > 0)
					? possibleAnswers
					: new Answer [] { Answer.YES, Answer.NO, Answer.CANCEL };
		}

		/**
		 * To be called by presentation layer.
		 * @return the answers the user should be able to give, or null for Yes/No/Cancel.
		 */
		public Answer [] getPossibleAnswers() {
			return possibleAnswers;
		}

		/**
		 * To be called by business layer.
		 * @return the answer given by the user, or null if was not shown to the user for some reason.
		 */
		public Answer getAnswer() {
			return answer;
		}

		/** To be called by presentation layer. */
		public void setAnswer(Answer answer) {
			this.answer = answer;
		}

		/** To be called by business layer. */
		public void addCondition(Condition condition) {
			dependencies.add(condition);
		}

		/** To be called by presentation layer. */
		public Iterable<Condition> getConditions() {
			return dependencies;
		}
		
		/**
		 * To be called by presentation layer.
		 * @return true when this confirm should be displayed, considering its conditions.
		 */
		public boolean shouldDisplay()	{
			for (Condition condition : getConditions())
				if (condition.matches() == false)
					return false;
			return true;
		}
	}

}
