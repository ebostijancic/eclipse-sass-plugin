package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.PoModule;

/**
 * <p>
 * This service is responsible to handle the version of <code>PoModule</code> objects.
 * Each module of the <code>Webdesk ©</code> application has its own version number.
 * If the persistent versionNumber and the number in the module's xx.applicationContext.xml differs,
 * an upgrade performed automatically.
 * </p>
 * Upgrade commands are stored in XML format. SQL, JavaScript and Java (fully qualified class-name) can be executed.
 * 
 * @author DI Harald Entner (hentner), webdesk 3.1, created at 02.07.2007
 */
public interface PoModuleUpdateService {

	/**
     * Upgrades the given module to the given version (<code>toVersion</code>).
     * Does nothing when target version is zero, or smaller than or equal to the module version.
     * The update folders are organized like the following:
     * <pre>
     *      v1/[prepare.xml]<br>
     *      v1/[upgrade.xml]<br>
     *      v2/[prepare.xml]<br>
     *      v2/[upgrade.xml]<br>
     *      ....
     * </pre> 
     * @param module the PoModule to upgrade.
     * @param toVersion the version that should be achieved.
     */
    public void upgrade(PoModule module, int toVersion, String implFolder) throws UpgradeFailedException;

    /**
     * @return a <code>List</code> of <code>String</code> objects. These <code>String</code>'s
     * are the name's of the <code>PoModule</code>'s.
     */
    public List<String> getModules();
    
    /**
     * Installs the licenced webdesk modules as PoModule objects inside the database 
     * and detaches non licensed ones.
     * Upgrades modules when necessary.
     */
    public void installModules() throws UpgradeFailedException;

}
