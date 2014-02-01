IMPORTANT

For documentation see

    http://intranet/daisy/webdesk-tech/10-dsy/8-dsy/2330-dsy/224-dsy.html


The update definition files have to be situated in the 
'v' + version folder (e.g.: v1,v2,v3, ..)

If the purpose is to upgrade from version X to version (X+1),
put a v(X+1) folder in the update directory. In these folder at least 
the upgrade.xml file should exist. If you want to downgrade, also place
a downgrade.xml file there. Then, the versionNumber in the
applicationContext of the module has to be set to (X+1).

The xml files have to have the following syntax:

<(upgrade|downgrade>
    <sql dataSource="db"> .... SQL QUERY ..... </sql>
    .....
    <js>  .... JS File   ..... </js> // Path to js-file 
</(upgrade|downgrade>



If you want to run a sql script, who have to tell the batch update which
db. it should use. 

Possible Values of datasource are:

webdesk
shark


If you want to run a js file during the update, you have to provide 
such a fil inside your version folder (e.g.: v2/run.js)
