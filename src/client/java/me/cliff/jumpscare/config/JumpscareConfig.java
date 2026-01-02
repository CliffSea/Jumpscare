package me.cliff.jumpscare.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "jumpscare")
public class JumpscareConfig implements ConfigData {

    public int probability = 10000;

}


