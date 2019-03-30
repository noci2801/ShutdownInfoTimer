/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.chaoswg;

import de.chaoswg.CRT.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.World;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.general.ShutdownEvent;
import net.risingworld.api.events.general.UpdateEvent;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.objects.Player;

/**
 *
 * @author Schmull
 */
public class ShutdownInfoTimer extends Plugin implements Listener{
    private ShutdownInfoTimer plugin;
    private Server server;
    private World world;
    private CRT crt;
    private int debug;
    public int getDebug(){return debug;}
    public void setDebug(int debug){this.debug=debug;}
    
    private String conf;
    private SprachAPI sprachApiPlugin;
    String recSprachApiVersion;
    private int restartTime;
//    private String[] sShutdownTime;
    private int[][] iShutdownTime;
    private float lastUpdate;
    private float stepp;
    private DateTimeFormatter dtf;
    private Duration elapsed;
    private LocalDateTime[] daylyShutdownDate;
    private String[] intList;
    private ArrayList<Integer> intValu;
    private DateTimeFormatter chatDTF;
    private boolean timeInChat;
//    private String script;
//    private File scriptDir;
//    //private String scriptFile;
//    private int shutdownWait;
//    private String[] aRecSprachApiVersion;
//    public int getDebug(){ return debug; }
    private ShutdownInfoTimerClassText sprachApiDaten;
    private GetConfigDaten sysConfig;
    
//    Date date = new Date();
    
    @Override
    public void onEnable() {
        plugin              = this;
        server              = getServer();
        world               = getWorld();
        crt                 = new CRT();
        conf                = CRT.getSpec()+"config";
//        script              = CRT.getSpec()+"script";
        debug               = 1;
        recSprachApiVersion = "1.1.3";
        String pattern      = "yyyy-MM-dd HH:mm:ss";
        dtf                 = DateTimeFormatter.ofPattern(pattern);
        stepp               = 7.5f;
        
        if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled ");}
        String[] aRecSprachApiVersion = recSprachApiVersion.split("\\.",3);
        if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled "+"reqVer["+recSprachApiVersion+"] "+" verLeng["+aRecSprachApiVersion.length+"] ");}
        if(plugin.getPluginByName("SprachAPI") != null /*&& plugin.getPluginByName("RWGui") != null*/){
            // Lade Plugin
            sprachApiPlugin = (SprachAPI) plugin.getPluginByName("SprachAPI");
            String[] spApiVer = sprachApiPlugin.getDescription("version").split("\\.",3);
            // Prüfe SprachAPI Version
            //if (Integer.valueOf(spApiVer[0])>=Integer.valueOf(aRecSprachApiVersion[0]) && Integer.valueOf(spApiVer[1])>=Integer.valueOf(aRecSprachApiVersion[1]) && (Integer.valueOf(spApiVer[1])==Integer.valueOf(aRecSprachApiVersion[1])&& Integer.valueOf(spApiVer[2])>=Integer.valueOf(aRecSprachApiVersion[2])) ){
            if (crt.isSameVersion(recSprachApiVersion,sprachApiPlugin.getDescription("version"),new CRT.ClassDebug(plugin,debug))){        
                // Lade Eigene Klasse
                sprachApiDaten = new ShutdownInfoTimerClassText();
                sprachApiDaten.setDebug(debug);   //### gebe Debug weiter
                //sprachApiDaten.sDir=conf+CRT.getSpec()+"locale";
                //sprachApiDaten.setDir(conf+CRT.getSpec()+"locale");
                sprachApiDaten.setDir(conf+sprachApiDaten.getDir());
                sprachApiDaten.INI(plugin);

                if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled "+"SprachAPI("+sprachApiPlugin.getDescription("version")+") OK");}
                //#########################
                //### CONFIG für System ###
                //### Definiere Variablen
                String[][] sysConfigArray = {
                    //Name         Wert
                    {"restartTime", "3"},
                    {"intervall", "15 10 5 1"},
                    {"dayly", "1:30"},
                    //{"shutdownWait", "25"},
                    {"timeInChat", "false"},
                    {"timekeeping", String.valueOf(stepp)},
                    {"command", "SIT"},
                    //{"", ""},
                    
                    {"Debug", String.valueOf(debug)}
                };
                //################################
                //### Initialiesiere System Config
                sysConfig = new GetConfigDaten("System", sysConfigArray, this, debug,conf);
                debug = Integer.parseInt(sysConfig.getValue("Debug"));
                restartTime = Integer.parseInt(sysConfig.getValue("restartTime"));
                stepp = Float.parseFloat(sysConfig.getValue("timekeeping").replace(",", "."));
                timeInChat = sysConfig.getValue("timeInChat").toLowerCase().equals("true");
//                shutdownWait = Integer.parseInt(sysConfig.getValue("shutdownWait"));
                
                chatDTF = DateTimeFormatter.ofPattern("HH:mm:ss");
                if (sysConfig.getValue("dayly") != null){
                    String[] sShutdownTimeArray = sysConfig.getValue("dayly").split(" ");
                    int sShutdownTimePartMax=0;
                    for (String sShutdownTimePart : sShutdownTimeArray) {
                        String[] sShutdownTime = sShutdownTimePart.split(":");
                        if (sShutdownTime.length>sShutdownTimePartMax) sShutdownTimePartMax=sShutdownTime.length;
                        if(debug>3){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled "+"dayly "+sShutdownTimePart+" ");}
                    }
                    iShutdownTime     = new int          [sShutdownTimeArray.length][sShutdownTimePartMax];
                    daylyShutdownDate = new LocalDateTime[sShutdownTimeArray.length];
                    for (int n=0;n<iShutdownTime.length;n++){
                        if (sShutdownTimeArray.length>0){
                            String[] sShutdownTime = sShutdownTimeArray[n].split(":");
                            for (int nn=0;nn<sShutdownTime.length;nn++){
                                iShutdownTime[n][nn] = (!sShutdownTime[nn].equals("")?Integer.parseInt(sShutdownTime[nn]):-1);
                            }
//                            LocalDate loacDate = LocalDate.now();
//                            LocalTime localTime  = LocalTime.of(iShutdownTime[n][0],iShutdownTime[n][1]);
                            if (iShutdownTime[n][0]>=0 && iShutdownTime[n][1] >=0) {
                                daylyShutdownDate[n] = LocalDateTime.of(LocalDate.now(), LocalTime.of(iShutdownTime[n][0],iShutdownTime[n][1]));
                                if(debug>3){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled "+"dayly "+sShutdownTimeArray[n]+"|"+iShutdownTime[n][0]+"|"+iShutdownTime[n][1]);}
                                
                            }
                        }
                    }
//                    //###
//                    String[] sShutdownTime = sysConfig.getValue("dayly").split(":");
//                    iShutdownTime = new int[sShutdownTime.length];
//                    if (sShutdownTime.length>0){
//                        for (int n=0;n<sShutdownTime.length;n++){
//                            iShutdownTime[n] = Integer.parseInt(sShutdownTime[n]);
//                        }
//                    }
//                    stepp = 7.5f;
//                    String pattern        = "yyyy-MM-dd HH:mm:ss";
//                    dtf = DateTimeFormatter.ofPattern(pattern);
//                    LocalDate loacDate = LocalDate.now();
//                    LocalTime localTime  = LocalTime.of(iShutdownTime[0],iShutdownTime[1]);
//                    daylyShutdownDate = LocalDateTime.of(loacDate, localTime);
                }else{
                    daylyShutdownDate = null;
                }
                
                if (sysConfig.getValue("intervall") != null){
                    intList = sysConfig.getValue("intervall").split(" ");
                    intValu = new ArrayList();
                    for(int n=0;n<intList.length;n++){
                        intValu.add(Integer.parseInt(intList[n])*60);
                    }
                }
                if(debug>4){System.out.println("[" + plugin.getDescription("name") + "] "+"Enabled "+"currentTimeMillis "+System.currentTimeMillis()+" ");}                
                
//                //### Restart
//                //### Verzeichnis SCRIPT Erstellen, wenn vorhanden Fehler egal
//                scriptDir = new File(plugin.getPath() + conf+script);
//                if (scriptDir.mkdirs()) { if(debug>0){System.out.println("[" + plugin.getDescription("name") + "]"+" Verzeichnis SCRIPT erstellt.");} }
//                if (getOperatingSystem().toLowerCase().contains("win")){
//                    script = "restart.bat";
//                }else{
//                    script = "restart.sh";
//                }
//                scriptFile = scriptDir.getPath()+CRT.getSpec()+script;
//                File f = new File(scriptFile);
//                if(!f.exists() && !f.isDirectory()) { 
//                    FileOutputStream outputFile = null;
//                    try {
//                        InputStream inputFile = this.getClass().getResourceAsStream("/resources/"+script+"");
//                        outputFile = new FileOutputStream(f);
//                        byte by;
//                        while ((by = (byte) inputFile.read()) != -1) {
//                            outputFile.write(by);
//                        }   
//                    } catch (FileNotFoundException e) {
//                        System.err.println("[" + plugin.getDescription("name") + "] "+e.getMessage());
//                    } catch (IOException e) {
//                        System.err.println("[" + plugin.getDescription("name") + "] "+e.getMessage());
//                    } finally {
//                        if (outputFile!=null) try {outputFile.close();} catch (IOException e) {}
//                    }
//                }

//                for (File script:scriptDir.listFiles()){
//                    
//                }
                registerEventListener(this);
                
            }else{
                System.err.println("[" + plugin.getDescription("name") + "-ERR] Die Version von Plugin 'SprachAPI("+sprachApiPlugin.getDescription("version")+")' ist zu klein! Bitte Aktualisieren auf Version("+recSprachApiVersion+")!");
                registerEventListener(new ShutdownInfoTimerErrorSprachAPI(this));
            }
            
        }else{
            if (plugin.getPluginByName("SprachAPI") == null){
                System.err.println("[" + plugin.getDescription("name") + "-ERR] Das Plugin 'SprachAPI' ist nicht installiert! Bitte installieren!");
                registerEventListener(new ShutdownInfoTimerErrorSprachAPI(this));
            }
        }
    }

    @Override
    public void onDisable() {
        System.out.println("[" + plugin.getDescription("name") + "] Disabled");
    }
    
    @EventMethod
    public void onShutdown(ShutdownEvent event) {
        if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Shutdown "+"ausführen nach " + server.getRunningTime()+" ");}
//        server.broadcastYellMessage(""+String.format(sprachApiDaten.getText(player, "shutdownnow"), restartTime) );
        server.getAllPlayers().forEach((Player player) -> {
            if (player!=null){
                String str =String.format(sprachApiDaten.getText(player, "shutdownnow"), restartTime);
                player.sendYellMessage(str.replaceAll("\\[\\#[0-9a-fA-F]{6}\\]", ""));
                player.sendTextMessage((timeInChat?chatDTF.format(LocalTime.now())+" - ":"")+str);
            }
        });
        
//        // To Do### Restart Check
//        if (getOperatingSystem().toLowerCase().contains("win")){
//            try {                
//                // auf dem Server Ausführen
//                if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Shutdown "+"ausführen " + "WINDOWS ");}
//                new ProcessBuilder(("cmd /c start "+scriptFile+" "+shutdownWait).split(" ")).start();
//            } catch (IOException e) {
//                System.err.println("[" + plugin.getDescription("name") + "] "+"Fehler "+e.getMessage());
//            }
//        }else{
//            try {
//                // auf dem Server Ausführen
//                if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Shutdown "+"ausführen " + "LINUX ");}
//                Runtime runtime = Runtime.getRuntime();
//                runtime.exec(""+scriptFile+" "+shutdownWait);
//            } catch (IOException e) {
//                System.err.println("[" + plugin.getDescription("name") + "] "+"Fehler "+e.getMessage());
//            }
//        }
    }
    
    @EventMethod
    public void onUpdate(UpdateEvent event) {
        if (lastUpdate+stepp <= server.getRunningTime()){
            lastUpdate = server.getRunningTime();

            String output = dtf.format( LocalDateTime.now() );
            
            for (int n=0;n<daylyShutdownDate.length;n++){
                if (daylyShutdownDate!=null){
                    if (daylyShutdownDate[n]!=null){
                        elapsed = Duration.between(LocalDateTime.now(), daylyShutdownDate[n]);
                        if (elapsed.getSeconds()<stepp && !elapsed.isNegative()){
                            if(debug>2&&debug!=1){System.out.println("[" + plugin.getDescription("name") + "] "+"Update "+"Ausführen["+n+"] "+chatDTF.format(LocalTime.now()) );}
                            if(debug==1){System.out.println("Erwarteter Neustart["+n+"] - "+"[" + plugin.getDescription("name") + " | "+"Zeitstempel "+" | "+output+"] " );}
                            server.getAllPlayers().forEach((Player player) -> {
                                if (player!=null){
                                    String str =String.format(sprachApiDaten.getText(player, "shutdown"), 0) ;
                                    player.sendYellMessage(str.replaceAll("\\[\\#[0-9a-fA-F]{6}\\]", ""));
                                    player.sendTextMessage((timeInChat?chatDTF.format(LocalTime.now())+" - ":"")+str);
                                }
                            });
                        }
                        CRT.ClassLambadHelper l = new CRT.ClassLambadHelper();
                        l.n = n;
                        intValu.forEach((Integer counter) -> {
                            if (elapsed.getSeconds()<counter+stepp && elapsed.getSeconds()>=counter){
                                if(debug>2){System.out.println("[" + plugin.getDescription("name") + "] "+"Update "+"Ausführen["+l.n+"]["+counter+"] "+chatDTF.format(LocalTime.now()) );}
                                CRT.ClassLambadHelper ll = new CRT.ClassLambadHelper();
                                ll.n = counter/60;
                                server.getAllPlayers().forEach((Player player) -> {
                                    if (player!=null){
                                        String str =String.format(sprachApiDaten.getText(player, "shutdownin"), ll.n);
                                        player.sendYellMessage(str.replaceAll("\\[\\#[0-9a-fA-F]{6}\\]", ""));
                                        player.sendTextMessage((timeInChat?chatDTF.format(LocalTime.now())+" - ":"")+str);
                                    }
                                });
                            }
                        });
                    }
                }
                if(debug>3){System.out.println("[" + plugin.getDescription("name") + "] "+"Update "+"Time["+n+"] " + server.getRunningTime()+" now["+output+"] "+" in["+(elapsed!=null?elapsed.getSeconds():"NULL")+"] " );}
            }
        }
    }

    @EventMethod
    public void onPlayerCommand(PlayerCommandEvent event) throws IOException {
        Player player = event.getPlayer();
        
        String command = event.getCommand();
        String[] cmd = command.split(" ");
        if (cmd[0].toLowerCase().equals("/"+sysConfig.getValue("command")) || cmd[0].toLowerCase().equals("/"+sysConfig.getValue("command").toLowerCase())) {
            if (cmd.length > 1){
                //### Zeigt dem Spieler den Eingegebenen Befehl an
                player.sendTextMessage("");
                player.sendTextMessage(("#> "+cmd[0].substring(1)+" "+"[#00ff00]"+command.substring(1+cmd[0].length())));
            }
            if (cmd.length == 1) {
                player.sendTextMessage((plugin.getDescription("name") + " " + plugin.getDescription("version")));
                player.sendTextMessage((""+sprachApiDaten.getText(player, "command_author") + ": " + plugin.getDescription("author")));
                player.sendTextMessage((""+sprachApiDaten.getText(player, "command_Description") + ": " + plugin.getDescription("description")));
            }
            if (cmd.length > 1) {
                if (cmd[1].toLowerCase().equals("debug")) {
                    if (player.isAdmin()){
                        if(cmd.length > 2){
                            int deb = -1;
                            deb = Integer.parseInt(cmd[2]);
                            if (deb>=0){
                                setDebug(deb);
                                player.sendTextMessage(""+String.format(sprachApiDaten.getText(player, "set_Debug"), deb)  );
                            }
                        }else{
                            player.sendTextMessage(""+sprachApiDaten.getText(player, "less_comannd") );
                        }
                    }else{
                        player.sendTextMessage(""+sprachApiDaten.getText(player, "no_admin") );
                    }
                }
            }
            if (cmd.length > 1) {
                if (cmd[1].toLowerCase().equals("status")) {
                    if (player.isAdmin()){
                        for (int n=0;n<daylyShutdownDate.length;n++){
                            if (daylyShutdownDate!=null){
                                if (daylyShutdownDate[n]!=null){
                                    elapsed = Duration.between(LocalDateTime.now(), daylyShutdownDate[n]);
                                    if(debug>2){System.out.println("[" + plugin.getDescription("name") + "] "+"Status "+"Timer["+n+"] "+chatDTF.format(LocalTime.now())+" "+"Elapsed["+elapsed+"] " );}
                                    if(debug>2){System.out.println("[" + plugin.getDescription("name") + "] "+"Status "+"Timer["+n+"] "+chatDTF.format(LocalTime.now().plus(elapsed))+" " );}
                                    CRT.ClassLambadHelper l = new CRT.ClassLambadHelper();
                                    l.n = n;
                                    l.obj = -1;
                                    l.f = 0;
                                    intValu.forEach((Integer counter) -> {
                                        if (l.f < counter) l.f = counter;
                                        if (elapsed.getSeconds()>counter /*&& elapsed.getSeconds()>=0 /*+stepp && elapsed.getSeconds()>=counter*/){
                                            if(debug>3){System.out.println("[" + plugin.getDescription("name") + "] "+"Status "+"Timer["+l.n+"]["+counter+"] "+elapsed.getSeconds() );}
                                            if ( (int) l.obj < counter){
                                                l.obj = counter;
                                            }
//                                            ClassLambadHelper ll = new CRT.ClassLambadHelper();
//                                            ll.n = counter/60;
//                                            server.getAllPlayers().forEach((Player ePlayer) -> {
//                                                if (ePlayer!=null){
//                                                    String str =String.format(sprachApiDaten.getText(ePlayer, "shutdownin"), ll.n);
//                                                    ePlayer.sendYellMessage(str.replaceAll("\\[\\#[0-9a-fA-F]{6}\\]", ""));
//                                                    ePlayer.sendTextMessage((timeInChat?chatDTF.format(LocalTime.now())+" - ":"")+str);
//                                                }
//                                            });
                                        }else{
                                            if(debug>3){System.out.println("[" + plugin.getDescription("name") + "] "+"Status "+"Timer["+l.n+"]false["+counter+"] "+elapsed.getSeconds() );}
                                        }
                                    });
                                    
                                    if(debug>2){System.out.println("[" + plugin.getDescription("name") + "] "+"Status "+"Timer["+n+"] "+(elapsed.getSeconds()>=0?((int)l.obj>=0 && elapsed.getSeconds()<=(int)l.f?"Aktiv":"Warten"):"Vorbei")+" "/*+((int)l.obj>=0)+" "+((int)l.obj<=(int)l.f)*/ );}
                                    if(debug>2){System.out.println("[" + plugin.getDescription("name") + "] "+"Status " );}                                    
                                    player.sendTextMessage(String.format(sprachApiDaten.getText(player, "status_Timer"), n)+" "+chatDTF.format(LocalTime.now().plus(elapsed))+" "+(elapsed.getSeconds()>=0?((int)l.obj>=0 && elapsed.getSeconds()<=(int)l.f?"[#00c01c]":"[#ffc840]"):"[#808080]")+(elapsed.getSeconds()>=0?((int)l.obj>=0 && elapsed.getSeconds()<=(int)l.f?sprachApiDaten.getText(player, "status_Active"):sprachApiDaten.getText(player, "status_Waiting")):sprachApiDaten.getText(player, "status_Past")) );
                                }
                            }
                        }
                    }else{
                        player.sendTextMessage(""+sprachApiDaten.getText(player, "no_admin") );
                    }
                }
            }
        } else if (cmd[0].toLowerCase().equals("/")) {
            player.sendTextMessage("" + "/"+sysConfig.getValue("command"));
        }
    }
    
}
