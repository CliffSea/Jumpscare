package me.cliff.jumpscare;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundManager {

    public static final Identifier SOUND_SCREAM = Identifier.of(Jumpscare.MOD_ID, "scream");


    public static SoundEvent SOUND_EVENT_SCREAM = SoundEvent.of(SOUND_SCREAM);


    public void registry(){
        Registry.register(Registries.SOUND_EVENT, SOUND_SCREAM, SOUND_EVENT_SCREAM);
    }

}
