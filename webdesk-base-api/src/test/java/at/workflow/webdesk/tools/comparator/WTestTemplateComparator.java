package at.workflow.webdesk.tools.comparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author fritzberger 04.07.2013
 */
public class WTestTemplateComparator extends TestCase
{
	public void testTemplateComparator()	{
		final List<String> templateList = Arrays.asList(new String [] {
				"personJobs",
				"personPositions",
				"personSkills",
				"contactAddresses",
				"contactEmails",
				"contactInstantMessagings",
				"contactPhones",
				"contactSocialNetworks",
				"contactWebs",
				"person.permissions",
				"person.memberOfGroups",
				"person.referencedAsRoleHolder",
				"person.referencedAsCompetenceTarget",
				"person.deputies"
		});
		final List<String> testList = Arrays.asList(new String [] {
				"y",
				"x",
				"personPositions",
				"contactInstantMessagings",
				"contactPhones",
				"personJobs",
				"person.permissions",
				"personSkills",
				"contactAddresses",
				"z",
				"contactEmails",
				"person.deputies"
			});
		
		final Comparator comparator = new TemplateDrivenComparator(templateList);
		Collections.sort(testList, comparator);
		
		final List<String> resultList = Arrays.asList(new String [] {
				"personJobs",
				"personPositions",
				"personSkills",
				"contactAddresses",
				"contactEmails",
				"contactInstantMessagings",
				"contactPhones",
				"person.permissions",
				"person.deputies",
				"x",
				"y",
				"z",
			});
		assertEquals(resultList, testList);
	}
}
