/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.chaoswg;

import net.risingworld.api.Timer;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.objects.Player;

/**
 *
 * @author Schmull
 */
public class ShutdownInfoTimerErrorSprachAPI implements Listener {

    private final ShutdownInfoTimer plugin;
    private final int debug;

    public ShutdownInfoTimerErrorSprachAPI(ShutdownInfoTimer plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebug();
    }
    @EventMethod
    public void onPlayerConnectEvent(PlayerConnectEvent event) {
        if(debug>0){System.out.println("[" + plugin.getDescription("name") + "] "+"Connect ");}
        Player player = event.getPlayer();
        boolean notFound = (plugin.getPluginByName("SprachAPI") == null);
        if (player.isAdmin()){
            if (player.getLanguage().equals("de")){
                player.sendYellMessage("[ShutdownInfoTimer]"+"\nPlugin angehalten"+"\n\n"+"\"SprachAPI\" "+(notFound?"nicht gefunden.":"zu Alte Version. "+"\nBenötiege Version("+plugin.recSprachApiVersion+") "));
            }else{
                player.sendYellMessage("[ShutdownInfoTimer]"+"\nPlugin stopt"+"\n\n"+"\"SprachAPI\" "+(notFound?"not found.":"to Old Version. "+"\nRequires version("+plugin.recSprachApiVersion+") "));
            }
//            Timer closePlugin = new Timer(60f*1.5f, 0f, -1, () -> {  //lambda expression
//                //This will be executed when the timer triggers
//                plugin.onDisable();
//            });
//            closePlugin.start();
        }
    }
    
}
