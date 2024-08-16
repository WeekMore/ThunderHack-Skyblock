package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.NotificationManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.Notifications;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.MISC);
        randomList.add(1);
        randomList.add(1);
        randomList.add(1);
        randomList.add(-1);
        randomList.add(-1);
        randomList.add(-1);

    }

    private final Setting<DetectMode> detectMode = new Setting<>("DetectMode", DetectMode.DataTracker);
    private final Setting<Boolean> rodSave = new Setting<>("RodSave", true);
    private final Setting<Boolean> changeRod = new Setting<>("ChangeRod", false);
    private final Setting<Boolean> autoSell = new Setting<>("AutoSell", false);
    private final Setting<Integer> maxWaitTime = new Setting<>("MaxWaitTime", 500, 500, 1500);
    private final Setting<Integer> minWaitTime = new Setting<>("minWaitTime", 500, 500, 1500);

    //private final Setting<Boolean> random = new Setting<>("Random", true);
    private final Setting<Boolean> randomMove = new Setting<>("RandomMove", false);
    private final Setting<Boolean> randomFov = new Setting<>("RandomFov", false);


    private boolean flag = false;
    private final Timer timeout = new Timer();
    private final Timer cooldown = new Timer();
    private final List<Integer> randomList = new ArrayList<>();
    private int randomCounter = 0;
    private Timer randomTimer = new Timer();



    private enum DetectMode {
        Sound, DataTracker, Skyblock
    }


    @Override
    public void onEnable() {
        Collections.shuffle(randomList);
        if (fullNullCheck())
            disable("NPE protection");
    }

    @Override
    public void onDisable() {
        flag = false;
        randomCounter = 0;
    }


    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
//        if(e.getPacket() instanceof PlaySoundS2CPacket sound && detectMode.getValue() == DetectMode.Skyblock)
//            Managers.NOTIFICATION.publicity("Soundevent",sound.getSound().value().getId().toString(),5, Notification.Type.INFO);
        if(e.getPacket() instanceof PlaySoundS2CPacket sound && detectMode.getValue() == DetectMode.Skyblock)
            if(sound.getSound().value().getId().toString().toLowerCase().contains("note") && mc.player.fishHook != null){
                catchFish();
                Managers.NOTIFICATION.publicity("Soundevent","Catch",5, Notification.Type.INFO);
            }



        if(e.getPacket() instanceof PlaySoundS2CPacket sound && detectMode.getValue() == DetectMode.Sound)
            if(sound.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) && mc.player.fishHook != null && mc.player.fishHook.squaredDistanceTo(sound.getX(), sound.getY(), sound.getZ()) < 4f)
                catchFish();
    }

    @Override
    public void onUpdate() {
        if (maxWaitTime.getValue() < minWaitTime.getValue()){
            minWaitTime.setValue(maxWaitTime.getValue());
        }


        if (randomMove.getValue() || randomFov.getValue()) {
            if (randomTimer.passedMs(1500)){
                if (randomCounter >= 6){
                    randomCounter = 0;
                    Collections.shuffle(randomList);
                }

                if (randomFov.getValue()){
                    mc.player.setPitch(mc.player.getPitch()+ randomList.get(randomCounter));
                    mc.player.headYaw = mc.player.getYaw() + randomList.get(randomCounter);
                }
                if (randomMove.getValue()){
                    if (randomList.get(randomCounter)> 0){
                        Thread.startVirtualThread(()->{
                            mc.options.leftKey.setPressed(true);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            mc.options.leftKey.setPressed(false);

                        });
                    }else {
                        Thread.startVirtualThread(()->{
                            mc.options.rightKey.setPressed(true);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            mc.options.rightKey.setPressed(false);

                        });
                    }

                    //mc.options.leftKey.setPressed();
                }
                randomCounter++;
                randomTimer.reset();
            }
        }



        if (mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (mc.player.getMainHandStack().getDamage() > 52) {
                if (rodSave.getValue() && !changeRod.getValue()) {
                    disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
                } else if (changeRod.getValue() && getRodSlot() != -1) {
                    sendMessage(isRu() ? "Свапнулся на новую удочку" : "Swapped to a new rod");
                    InventoryUtility.switchTo(getRodSlot());
                    cooldown.reset();
                } else disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
            }
        }

        if(!cooldown.passedMs(1000))
            return;

        if (timeout.passedMs(45000) && mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
            cooldown.reset();
        }

        if (mc.player.fishHook != null && detectMode.getValue() == DetectMode.DataTracker) {
            boolean caughtFish = mc.player.fishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH);
            if (!flag && caughtFish) {
                catchFish();
                flag = true;
            } else if (!caughtFish) flag = false;
        }
    }

    private void catchFish() {
        Managers.ASYNC.run(() -> {

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            if (autoSell.getValue() && timeout.passedMs(1000))
                mc.player.networkHandler.sendChatCommand("sellfish");

            try {
                Thread.sleep((int) MathUtility.random(minWaitTime.getValue(), maxWaitTime.getValue()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
        }, (int) MathUtility.random(99, 200));
    }

    private int getRodSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.player.getInventory().getStack(i);
            if (item.getItem() == Items.FISHING_ROD && item.getDamage() < 52)
                return i;
        }
        return -1;
    }
}
