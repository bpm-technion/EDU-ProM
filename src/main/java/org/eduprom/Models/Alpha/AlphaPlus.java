package org.eduprom.Models.Alpha;

import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaVersion;

/**
 * Created by ydahari on 4/12/2017.
 */
public class AlphaPlus extends Alpha {

    public AlphaPlus(String filename) throws Exception {
        super(filename);
    }
    public AlphaPlus(XLog log, String filename) throws Exception {
        super(log,filename);
    }
    public AlphaVersion GetVersion(){
        return AlphaVersion.PLUS;
    }
}
