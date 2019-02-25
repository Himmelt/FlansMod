package co.uk.flansmods.common.guns;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.world.World;

import java.util.*;

public class RunningBulletManager implements ITickHandler {

    private static Map<World, RunningBulletManager> managers = new HashMap<>();
    public World worldObj;
    public List<RunningBullet> bullets = new ArrayList<>();
    public int tick = 0;
    boolean remove = false;
    public Timer t;

    public void register(RunningBullet bullet) {
        this.bullets.add(bullet);
    }

    public void spawn(RunningBullet bullet) {
    }

    public void remove() {
        this.remove = true;
    }

    public static RunningBulletManager getManager(World worldObj) {
        RunningBulletManager manager = managers.get(worldObj);
        if (manager == null) {
            manager = new RunningBulletManager();
            managers.put(worldObj, manager);
        }

        return manager;
    }

    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        ++this.tick;
        //if (this.tick % 20 == 0) {
        //    System.out.println(this.bullets);
        //}

        Iterator it = this.bullets.iterator();

        while (it.hasNext()) {
            this.remove = false;
            ((RunningBullet) it.next()).onUpdate();
            if (this.remove) {
                it.remove();
            }
        }
    }

    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
    }

    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.SERVER);
    }

    public String getLabel() {
        return "FlansMod RunningBulletManager";
    }
}
