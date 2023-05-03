package meteordevelopment.meteorclient.systems.modules;

import com.google.common.collect.Ordering;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import meteordevelopment.meteorclient.LemonClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.lemon.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.events.lemon.KeyEvent;
import meteordevelopment.meteorclient.events.lemon.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.events.lemon.MouseButtonEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.borers.*;
import meteordevelopment.meteorclient.systems.modules.chat.*;
import meteordevelopment.meteorclient.systems.modules.combat.*;
import meteordevelopment.meteorclient.systems.modules.hig.*;
import meteordevelopment.meteorclient.systems.modules.misc.*;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.movement.*;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;
import meteordevelopment.meteorclient.systems.modules.player.*;
import meteordevelopment.meteorclient.systems.modules.render.*;
import meteordevelopment.meteorclient.systems.modules.render.marker.Marker;
import meteordevelopment.meteorclient.systems.modules.render.search.Search;
import meteordevelopment.meteorclient.systems.modules.settings.*;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.systems.modules.world.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorIdentifier;
import meteordevelopment.meteorclient.utils.misc.ValueComparableMap;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.LemonClient.mc;

public class Modules extends System<Modules> {
    public static final ModuleRegistry REGISTRY = new ModuleRegistry();

    private static final List<Category> CATEGORIES = new ArrayList<>();

    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> moduleInstances = new HashMap<>();
    private final Map<Category, List<Module>> groups = new HashMap<>();

    private final List<Module> active = new ArrayList<>();
    private Module moduleToBind;

    public Modules() {
        super("modules");
    }

    public static Modules get() {
        return Systems.get(Modules.class);
    }

    @Override
    public void init() {
        initCombat();
        initPlayer();
        initMovement();
        initRender();
        initWorld();
        initHig();
        initBorers();
        initMisc();
        initChat();
        initSettings();
    }

    @Override
    public void load(File folder) {
        for (Module module : modules) {
            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) setting.reset();
            }
        }

        super.load(folder);
    }

    public void sortModules() {
        for (List<Module> modules : groups.values()) {
            modules.sort(Comparator.comparing(o -> o.title));
        }
        modules.sort(Comparator.comparing(o -> o.title));
    }

    public static void registerCategory(Category category) {
        if (!Categories.REGISTERING) throw new RuntimeException("Modules.registerCategory - Cannot register category outside of onRegisterCategories callback.");

        CATEGORIES.add(category);
    }

    public static Iterable<Category> loopCategories() {
        return CATEGORIES;
    }

    public static Category getCategoryByHash(int hash) {
        for (Category category : CATEGORIES) {
            if (category.hashCode() == hash) return category;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> klass) {
        return (T) moduleInstances.get(klass);
    }

    public Module get(String name) {
        for (Module module : moduleInstances.values()) {
            if (module.name.equalsIgnoreCase(name)) return module;
        }

        return null;
    }

    public boolean isActive(Class<? extends Module> klass) {
        Module module = get(klass);
        return module != null && module.isActive();
    }

    public List<Module> getGroup(Category category) {
        return groups.computeIfAbsent(category, category1 -> new ArrayList<>());
    }

    public Collection<Module> getAll() {
        return moduleInstances.values();
    }

    public List<Module> getList() {
        return modules;
    }

    public int getCount() {
        return moduleInstances.values().size();
    }

    public List<Module> getActive() {
        synchronized (active) {
            return active;
        }
    }

    public Set<Module> searchTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural());

        for (Module module : this.moduleInstances.values()) {
            int score = Utils.searchLevenshteinDefault(module.title, text, false);
            modules.put(module, modules.getOrDefault(module, 0) + score);
        }

        return modules.keySet();
    }

    public Set<Module> searchSettingTitles(String text) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Ordering.natural());

        for (Module module : this.moduleInstances.values()) {
            int lowest = Integer.MAX_VALUE;
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int score = Utils.searchLevenshteinDefault(setting.title, text, false);
                    if (score < lowest) lowest = score;
                }
            }
            modules.put(module, modules.getOrDefault(module, 0) + lowest);
        }

        return modules.keySet();
    }

    void addActive(Module module) {
        synchronized (active) {
            if (!active.contains(module)) {
                active.add(module);
                LemonClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    void removeActive(Module module) {
        synchronized (active) {
            if (active.remove(module)) {
                LemonClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    // Binding

    public void setModuleToBind(Module moduleToBind) {
        this.moduleToBind = moduleToBind;
    }

    public boolean isBinding() {
        return moduleToBind != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Press && onBinding(true, event.key)) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && onBinding(false, event.button)) event.cancel();
    }

    private boolean onBinding(boolean isKey, int value) {
        if (!isBinding()) return false;

        if (moduleToBind.keybind.canBindTo(isKey, value)) {
            moduleToBind.keybind.set(isKey, value);
            moduleToBind.info("Bound to (highlight)%s(default).", moduleToBind.keybind);
        }
        else if (value == GLFW.GLFW_KEY_ESCAPE) {
            moduleToBind.keybind.set(Keybind.none());
            moduleToBind.info("Removed bind.");
        }
        else return false;

        LemonClient.EVENT_BUS.post(ModuleBindChangedEvent.get(moduleToBind));
        moduleToBind = null;

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(true, event.key, event.action == KeyAction.Press);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Repeat) return;
        onAction(false, event.button, event.action == KeyAction.Press);
    }

    private void onAction(boolean isKey, int value, boolean isPress) {
        if (mc.currentScreen == null && !Input.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            for (Module module : moduleInstances.values()) {
                if (module.keybind.matches(isKey, value) && (isPress || module.toggleOnBindRelease)) {
                    module.toggle();
                    module.sendToggledMsg();
                }
            }
        }
    }

    // End of binding

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) return;

        for (Module module : moduleInstances.values()) {
            if (module.toggleOnBindRelease && module.isActive()) {
                module.toggle();
                module.sendToggledMsg();
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    LemonClient.EVENT_BUS.subscribe(module);
                    module.onActivate();
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive() && !module.runInMainMenu) {
                    LemonClient.EVENT_BUS.unsubscribe(module);
                    module.onDeactivate();
                }
            }
        }
    }

    public void disableAll() {
        synchronized (active) {
            for (Module module : modules) {
                if (module.isActive()) module.toggle();
            }
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        NbtList modulesTag = new NbtList();
        for (Module module : getAll()) {
            NbtCompound moduleTag = module.toTag();
            if (moduleTag != null) modulesTag.add(moduleTag);
        }
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public Modules fromTag(NbtCompound tag) {
        disableAll();

        NbtList modulesTag = tag.getList("modules", 10);
        for (NbtElement moduleTagI : modulesTag) {
            NbtCompound moduleTag = (NbtCompound) moduleTagI;
            Module module = get(moduleTag.getString("name"));
            if (module != null) module.fromTag(moduleTag);
        }

        return this;
    }

    // INIT MODULES

    public void add(Module module) {
        // Check if the module's category is registered
        if (!CATEGORIES.contains(module.category)) {
            throw new RuntimeException("Modules.addModule - Module's category was not registered.");
        }

        // Remove the previous module with the same name
        AtomicReference<Module> removedModule = new AtomicReference<>();
        if (moduleInstances.values().removeIf(module1 -> {
            if (module1.name.equals(module.name)) {
                removedModule.set(module1);
                module1.settings.unregisterColorSettings();

                return true;
            }

            return false;
        })) {
            getGroup(removedModule.get().category).remove(removedModule.get());
        }

        // Add the module
        moduleInstances.put(module.getClass(), module);
        modules.add(module);
        getGroup(module.category).add(module);

        // Register color settings for the module
        module.settings.registerColorSettings(module);
    }

    private void initCombat() {
        add(new AimAssist());
        add(new AnchorAura());
        add(new AntiBurrow());
        add(new AntiSurroundBlocks());
        add(new ArrowDodge());
        add(new Auto32K());
        add(new AutoAnvil());
        add(new AutoArmor());
        add(new AutoCity());
        add(new AutoEXP());
        add(new AutoHoleFill());
        add(new AutoMinecart());
        add(new AutoTotem());
        add(new AutoTrap());
        add(new AutoTrapPlus());
        add(new AutoWeapon());
        add(new AutoWeb());
        add(new BedBomb());
        add(new BedCrafter());
        add(new BedDisabler());
        add(new BowAimbot());
        add(new BowBomb());
        add(new BowSpam());
        add(new Burrow());
        add(new BurrowPlus());
        add(new CevBreaker());
        add(new HoleMiner());
        add(new CityBreaker());
        add(new CivBreaker());
        add(new Criticals());
        add(new Hitboxes());
        add(new HolePush());
        add(new KillAura());
        add(new LemonAura());
        add(new LemonAuraPlus());
        add(new NewBurrow());
        add(new NewSurround());
        add(new Offhand());
        add(new PistonAura());
        add(new Quiver());
        add(new SelfAnvil());
        add(new SelfProtect());
        add(new SelfTrap());
        add(new SelfWeb());
        add(new Surround());
        add(new TNTAura());
        add(new WebHoleFill());
    }

    private void initPlayer() {
        add(new AntiHunger());
        add(new AntiSpawnpoint());
        add(new AutoClicker());
        add(new AutoCraft());
        add(new AutoEat());
        add(new AutoFish());
        add(new AutoGap());
        add(new AutoMend());
        add(new AutoMine());
        add(new AutoReplenish());
        add(new AutoTool());
        add(new BindClickExtra());
        add(new ChestSwap());
        add(new EXPThrower());
        add(new FakePlayer());
        add(new FastUse());
        add(new GhostHand());
        add(new InstantMine());
        add(new InstantMinePlus());
        add(new LiquidInteract());
        add(new MiddleClickExtra());
        add(new MultiTask());
        add(new NoBreakDelay());
        add(new NoInteract());
        add(new NoMiningTrace());
        add(new NoRotate());
        add(new OffhandCrash());
        add(new OneClickEat());
        add(new PortalGodMode());
        add(new PacketMine());
        add(new Portals());
        add(new PotionSaver());
        add(new PotionSpoof());
        add(new Reach());
        add(new Rotation());
        add(new SpeedMine());
        add(new Suicide());
        add(new Twerk());
        add(new XPThrower());
        add(AutoEatPlus.INSTANCE);
    }

    private void initMovement() {
        add(new AirJump());
        add(new Anchor());
        add(new AntiAFK());
        add(new AntiLevitation());
        add(new AntiPistonPush());
        add(new AntiVoid());
        add(new AutoJump());
        add(new AutoWalk());
        add(new BedrockWalk());
        add(new Blink());
        add(new BoatFly());
        add(new ClickTP());
        add(new EFly());
        add(new ElytraBoost());
        add(new ElytraFly());
        add(new EntityControl());
        add(new EntitySpeed());
        add(new FastClimb());
        add(new Flight());
        add(new GUIMove());
        add(new HighJump());
        add(new HoleSnap());
        add(new Jesus());
        add(new LongJump());
        add(new NoFall());
        add(new NoSlow());
        add(new OldAnvil());
        add(new PacketFly());
        add(new Parkour());
        add(new Phase());
        add(new ReverseStep());
        add(new SafeWalk());
        add(new Scaffold());
        add(new Slippy());
        add(new Sneak());
        add(new Spider());
        add(new Speed());
        add(new SpeedPlus());
        add(new Sprint());
        add(new Step());
        add(new StepPlus());
        add(new Strafe());
        add(new TimerFall());
        add(new TridentBoost());
        add(new Velocity());
        add(new WebNoSlow());
        add(new AutoCenter());
        add(ScaffoldPlus.INSTANCE);
        add(new AutoCenter());
    }

    private void initRender() {
        add(new BetterTooltips());
        add(new BlockSelection());
        add(new BossStack());
        add(new Breadcrumbs());
        add(new BreakIndicators());
        add(new BurrowESP());
        add(new CameraTweaks());
        add(new Chams());
        add(new CityESP());
        add(new Confetti());
        add(new CustomFOV());
        add(new EntityOwner());
        add(new ESP());
        add(new ESP2());
        add(new Freecam());
        add(new FreeLook());
        add(new Fullbright());
        add(new HandView());
        add(new HandTweaks());
        add(new HoleESP());
        add(new ItemPhysics());
        add(new ItemHighlight());
        add(new LightOverlay());
        add(new LogoutSpots());
        add(new Marker());
        add(new Nametags());
        add(new NewChunks());
        add(new NoRender());
        add(new Notifications());
        add(new PopChams());
        add(new RideStats());
        add(new SkinBlinker());
        add(new Search());
        add(new StorageESP());
        add(new TimeAnimator());
        add(new TimeChanger());
        add(new Tracers());
        add(new Trail());
        add(new Trajectories());
        add(new UnfocusedCPU());
        add(new VoidESP());
        add(new WallHack());
        add(new WaypointsModule());
        add(new Xray());
        add(new Zoom());
        add(new Blur());
        add(new TunnelESP());
        add(new NoCaveCulling());
    }

    private void initWorld() {
        add(new AirPlace());
        add(new Ambience());
        add(new AntiCactus());
        add(new AntiRegear());
        add(new AutoBedTrap());
        add(new AutoBreed());
        add(new AutoBrewer());
        add(new AutoBuild());
        add(new AutoExtinguish());
        add(new AutoMount());
        add(new AutoNametag());
        add(new AutoShearer());
        add(new AutoSign());
        add(new AutoSmelter());
        add(new AutoTNT());
        add(new AutoWither());
        add(new BuildHeight());
        add(new EChestFarmer());
        add(new EndermanLook());
        add(new Flamethrower());
        add(new InfinityMiner());
        add(new KillFx());
        add(new LiquidFiller());
        add(new MountBypass());
        add(new NoGhostBlocks());
        add(new Nuker());
        add(new Platform());
        add(new StashFinder());
        add(new SpawnProofer());
        add(new Timer());
        add(new VeinMiner());
    }

    private void initMisc() {
        add(new Swarm());
        add(new AfkLog());
        add(new AntiBot());
        add(new AntiPacketKick());
        add(new AutoClicker());
        add(new AutoLog());
        add(new AutoLogPlus());
        add(new AutoReconnect());
        add(new AutoRespawn());
        add(new BetterBeacons());
        add(new BetterTab());
        add(new BookBot());
        add(new MiddleClickFriend());
        add(new NameProtect());
        add(new Notebot());
        add(new PacketCanceller());
        add(new PauseOnUnloaded());
        add(new PingSpoof());
        add(new SoundBlocker());
        add(new ServerSpoof());
        add(new InventoryTweaks());
        add(new InvManager());
    }

    private void initChat() {
        add(new Announcer());
        add(new AutoLogin());
        add(new BetterChat());
        add(new BurrowNotifier());
        add(new AutoEz());
        add(new GroupChat());
        add(new MessageAura());
        add(new MessageLagger());
        add(new Spammer());
        add(new TotemPopCounter());
        add(new VisualRange());
        add(new Welcomer());
    }

    private void initHig() {
        add(new HandManager());
        add(new HighwayBuilder());
        add(new HighwayBuilderPlus());
        add(new HighwayTools());
        add(new TPSSync());
    }

    private void initBorers() {
        add(new AxisBorer());
        add(new NegNegBorer());
        add(new NegPosBorer());
        add(new PosNegBorer());
        add(new PosPosBorer());
        add(new RingRoadBorer());
    }

    private void initSettings() {
        add(new FacingSettings());
        add(new RangeSettings());
        add(new RaytraceSettings());
        add(new RotationSettings());
        add(new SwingSettings());
    }


    public static class ModuleRegistry extends Registry<Module> {
        public ModuleRegistry() {
            super(RegistryKey.ofRegistry(new MeteorIdentifier("modules")), Lifecycle.stable());
        }

        @Override
        public int size() {
            return Modules.get().getAll().size();
        }

        @Override
        public Identifier getId(Module entry) {
            return null;
        }

        @Override
        public Optional<RegistryKey<Module>> getKey(Module entry) {
            return Optional.empty();
        }

        @Override
        public int getRawId(Module entry) {
            return 0;
        }

        @Override
        public Module get(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public Module get(Identifier id) {
            return null;
        }

        @Override
        public Lifecycle getEntryLifecycle(Module object) {
            return null;
        }

        @Override
        public Lifecycle getLifecycle() {
            return null;
        }

        @Override
        public Set<Identifier> getIds() {
            return null;
        }
        @Override
        public boolean containsId(Identifier id) {
            return false;
        }

        @Nullable
        @Override
        public Module get(int index) {
            return null;
        }

        @Override
        public Iterator<Module> iterator() {
            return new ModuleIterator();
        }

        @Override
        public boolean contains(RegistryKey<Module> key) {
            return false;
        }

        @Override
        public Set<Map.Entry<RegistryKey<Module>, Module>> getEntrySet() {
            return null;
        }

        @Override
        public Set<RegistryKey<Module>> getKeys() {
            return null;
        }

        @Override
        public Optional<RegistryEntry<Module>> getRandom(Random random) {
            return Optional.empty();
        }

        @Override
        public Registry<Module> freeze() {
            return null;
        }

        @Override
        public RegistryEntry<Module> getOrCreateEntry(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public DataResult<RegistryEntry<Module>> getOrCreateEntryDataResult(RegistryKey<Module> key) {
            return null;
        }

        @Override
        public RegistryEntry.Reference<Module> createEntry(Module value) {
            return null;
        }

        @Override
        public Optional<RegistryEntry<Module>> getEntry(int rawId) {
            return Optional.empty();
        }

        @Override
        public Optional<RegistryEntry<Module>> getEntry(RegistryKey<Module> key) {
            return Optional.empty();
        }

        @Override
        public Stream<RegistryEntry.Reference<Module>> streamEntries() {
            return null;
        }

        @Override
        public Optional<RegistryEntryList.Named<Module>> getEntryList(TagKey<Module> tag) {
            return Optional.empty();
        }

        @Override
        public RegistryEntryList.Named<Module> getOrCreateEntryList(TagKey<Module> tag) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<Module>, RegistryEntryList.Named<Module>>> streamTagsAndEntries() {
            return null;
        }

        @Override
        public Stream<TagKey<Module>> streamTags() {
            return null;
        }

        @Override
        public boolean containsTag(TagKey<Module> tag) {
            return false;
        }

        @Override
        public void clearTags() {

        }

        @Override
        public void populateTags(Map<TagKey<Module>, List<RegistryEntry<Module>>> tagEntries) {

        }

        private static class ModuleIterator implements Iterator<Module> {
            private final Iterator<Module> iterator = Modules.get().getAll().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Module next() {
                return iterator.next();
            }
        }
    }
}
