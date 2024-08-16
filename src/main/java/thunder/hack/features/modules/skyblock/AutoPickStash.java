package thunder.hack.features.modules.skyblock;


import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

/**
 * @author yuxiangll
 * @since 2024/8/14 下午9:47
 * IntelliJ IDEA
 */
public class AutoPickStash extends Module {

    public Setting<Float> delay = new Setting<>("delay", 5f, 0f, 30f);

    private final Timer timer = new Timer();

    public AutoPickStash() {
        super("AutoPickStash",Category.SKYBLOCK);
    }

    @Override
    public void onUpdate() {
        if (timer.passedS(delay.getValue())){
            timer.reset();
            mc.player.networkHandler.sendCommand("pickupstash");
            Managers.NOTIFICATION.publicity("AutoPickUpStash","Picked up",2, Notification.Type.INFO);
        }
    }



}
