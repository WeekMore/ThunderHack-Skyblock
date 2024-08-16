package thunder.hack.features.modules.movement;

import net.minecraft.client.option.KeyBinding;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public class AutoSprint extends Module {

    public static final Setting<Mode> mode = new Setting<>("Mode",Mode.KeyPress);
    public enum Mode{KeyPress, AllDirection}

    public static final Setting<Boolean> sprint = new Setting<>("KeepSprint", true,v -> mode.is(Mode.AllDirection));
    public static final Setting<Float> motion = new Setting("Motion", 1f, 0f, 1f, v -> sprint.getValue() && mode.is(Mode.AllDirection));
    private final Setting<Boolean> stopWhileUsing = new Setting<>("StopWhileUsing", false, v -> mode.is(Mode.AllDirection));
    private final Setting<Boolean> pauseWhileAura = new Setting<>("PauseWhileAura", false, v -> mode.is(Mode.AllDirection));


    public AutoSprint() {
        super("AutoSprint", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.KeyPress) mc.options.sprintKey.setPressed(true);
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case KeyPress -> keyPressSprint();
            case AllDirection -> allDirectionSprint();
        }
    }

    private void allDirectionSprint(){
        mc.player.setSprinting(
                mc.player.getHungerManager().getFoodLevel() > 6
                        && !mc.player.horizontalCollision
                        && mc.player.input.movementForward > 0
                        && (!mc.player.isSneaking() || (ModuleManager.noSlow.isEnabled() && ModuleManager.noSlow.sneak.getValue()))
                        && (!mc.player.isUsingItem() || !stopWhileUsing.getValue())
                        && (!ModuleManager.aura.isEnabled() || Aura.target == null || !pauseWhileAura.getValue())
        );
    }

    private void keyPressSprint(){
        mc.options.sprintKey.setPressed(true);
    }



}
