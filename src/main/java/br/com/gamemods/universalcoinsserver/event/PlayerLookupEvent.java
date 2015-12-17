package br.com.gamemods.universalcoinsserver.event;

import cpw.mods.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerLookupEvent extends Event
{
    public final String searchedName;
    public final ArrayList<String> matchedNames;
    public final HashMap<String, UUID> uuidMap;
    public String exactResultName;
    public UUID exactResultId;

    public PlayerLookupEvent(String searchedName, ArrayList<String> matchedNames, HashMap<String, UUID> uuidMap)
    {
        this.searchedName = searchedName;
        this.matchedNames = matchedNames;
        this.uuidMap = uuidMap;
    }
}
