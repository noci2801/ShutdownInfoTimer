/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.chaoswg;

import java.util.ArrayList;

/**
 *
 * @author Schmull
 */
public class ShutdownInfoTimerClassText  extends SprachAPI {
    @Override
    protected void setDatenFunktion(){
        Sprache = new ArrayList();
        Sprache.add("en");
        Sprache.add("de");
        setSprache(Sprache);
        
        Daten = new String[][] { 
            //Name                  en                                                              de
            {"shutdownnow"          ,"Server restarts!\n\nExpected in %d min. accessible again.",   "Server startet neu!\n\nVorraussichtlich in %d min. wieder erreichbar."},
            {"shutdownin"           ,"Server restart, in %d min.",                                  "Server neustart, in %d min."},
            {"shutdown"             ,"The server is restarted every moment.",                       "Der Server wird jeden Moment neu gestartet."},
            {"less_comannd"         ,"Too few parameters.",                                         "Zu wenig Parameter."},
            {"set_Debug"            ,"Debug changed to %d.",                                        "Debug geändert zu %d."},
            {"no_admin"             ,"You are not an admin! Run Denied.",                           "Du bist kein Admin! Ausführen Verweigert."},
//                                                player.sendTextMessage(""+sprachApiDaten.getText(player, "")+(elapsed.getSeconds()>=0?((int)l.obj>=0 && elapsed.getSeconds()<=(int)l.f?"Aktiv":"Warten"):"Vorbei")+(elapsed.getSeconds()>=0?((int)l.obj>=0 && elapsed.getSeconds()<=(int)l.f?sprachApiDaten.getText(player, ""):sprachApiDaten.getText(player, "")):sprachApiDaten.getText(player, "")) );
            {"status_Timer"         ,"timer[%d]",                                                   "Timer[%d]"},
            {"status_Active"        ,"active",                                                      "Aktiv"},
            {"status_Waiting"       ,"Waiting",                                                     "Warten"},
            {"status_Past"          ,"Past",                                                        "Vorbei"},
            {"command_author"       ,"author",                                                      "Autor"},
            {"command_Description"  ,"Description",                                                 "Beschreibung"}
            
        };
        setDaten(Daten);        
        
    }

}
