package at.workflow.webdesk;

import java.util.Date;

public interface AbstractTimeRange {
    
    String TYPE_TA_BOOKING = "TA_BOOKING";
    String TYPE_TA_CALENDAR = "TA_CALENDAR";
    
    String TYPE_GW = "GW_TIMERANGE";
    String TYPE_PJZ = "PJZ_TIMERANGE";
    
    /**
     * returns Date Object containing Date and Time
     * of the starting point of the timerange
     * 
     * @return
     */
    Date getBeginDateTime();
    
    /**
     * returns date object containing Date and Time
     * of the ending point of the timerange
     * 
     * @return
     */
    Date getEndDateTime();
    
    /**
     * returns a textual representation of the timerange
     * (f.i. subject of appointment)
     * 
     * @return
     */
    String getSubject();
    
    /**
     * returns textual representation of the type of the timerange
     * (f.i. TYPE_TA_BOOKING --> see constants in class at.workflow.webdesk.AbstractTimeRange)
     * 
     * @return
     */
    String getTypeOfSource();
    
    /**
     * returns a long textual representation of the timerange
     * (f.i. body of appointment)
     * 
     * @return
     */
    String getBody();
}
