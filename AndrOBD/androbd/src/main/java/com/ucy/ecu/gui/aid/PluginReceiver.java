package com.ucy.ecu.gui.aid;


/**
 * Created by erwin on 26.12.17.
 */

public class PluginReceiver
    extends com.fr3ts0n.androbd.plugin.PluginReceiver
{
    /**
     * Get class of plugin implementation
     *
     * @return Plugin implementation class
     */
    @Override
    public Class getPluginClass()
    {
        return AndroidAutoPlugin.class;
    }
}
