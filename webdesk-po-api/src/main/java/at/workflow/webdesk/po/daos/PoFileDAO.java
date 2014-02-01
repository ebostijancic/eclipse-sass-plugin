package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.Configurable;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoFile;

/**
 * Created on 26.08.2005
 * @author hentner (Harald Entner)
 */
public interface PoFileDAO extends GenericDAO<PoFile> {

	/**
	 * @param fileId
	 * @return a <code>PoFile</code> object with the given <code>fileId</code>
	 * and the highest version.
	 */
	public PoFile getFileWithHighestVersion(String fileId);

	/**
	 * Returns a PoFile object with the given action,action.type and the 
	 * action has to be valid.
	 * 
	 * @param action a <code>PoAction</code> 
	 * @param type a <code>int</code> value that corresponds to the <code>actionType</code>
	 * @param date the date on which the action has to be valid.
	 * 
	 * @return a <code>PoFile</code> object with the given
	 * <code>action</code> (the type of the action must match <code>type</code>). The action has to be valid
	 * at the given <code>date</code>.
	 */
	public  PoFile getFile(PoAction action, int type, Date date);

	/**
	 * @param configurable
	 * @return A <code>PoFile</code> object with the highest <code>versionNumber</code>. 
	 */
	public PoFile getFileOfConfigurable(Configurable configurablee);
	
	/**
	 * 
	 * 
	 * @param action the file must have this <code>action</code> assigned.
	 * @param type the <code>type</code>of the file. (not of the action)
	 * @return a PoFile with the highest versionNumber.
	 */
	public  PoFile getFile(PoAction action, int type);

	/**
	 * @param relPath
	 * @return a fileId if a file with the given <code>relPath</code> was found.
	 * The fileId (even it is the same for all versions) is taken from the highest version.
	 */
	public  String getFileIdPerPath(String relPath);

	/**
	 * This function returns a <code>PoFile</code> object with the 
	 * highest version and the given <code>id</code> (corresponds to the fileId).
	 * 
	 * @param id
	 * @param Version
	 * @return a <code>PoFile</code> object 
	 */
	public  PoFile getFileWithVersionAndFileId(String id,
			int highestVersion);

	/**
	 * @param constraint
	 * @return a List of PoFile objects, which <code>path</code> matches 
	 * the given <code>constraint</code>.
	 */
	public  List<PoFile> findFileWherePathLike(String constraint);

	/**
	 * @param constraint
	 * @return a List of <code>PoFile</code> objects, which <code>path</code> matches 
	 * the given <code>constraint</code>. Additionally the returned files have the highest versions, 
	 * inside their fileId hierarchy.
	 */
	public  List<PoFile> findFileWherePathLikeAndMaxVersion(String constraint);

	/**
	 * @param action
	 * @return
	 */
	public  List<PoFile> findFilesOfActionOrderByVersion(PoAction action);

	/**
	 * @param uid
	 * @return
	 */
	public  List<PoFile> findFilesWithFileId(String uid);

	
	/**
	 * Returns the highest version number of the set of 
	 * <code>PoFile</code> object with the given 
	 * <code>fileId</code>.
	 * @param fileId 
	 */
	public int getHighestVersion(String fileId);


	/**
	 * @return a <code>List</code> of [<code>String</code>] <code>PoFile</code> <code>UID</code>'s.
	 * 
	 */
	public List<String> loadAllFileIds();

}
