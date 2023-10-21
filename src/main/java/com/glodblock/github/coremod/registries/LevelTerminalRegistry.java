package com.glodblock.github.coremod.registries;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.glodblock.github.api.registries.ILevelTerminalRegistry;
import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.api.registries.ILevelViewableAdapter;
import com.glodblock.github.common.parts.PartFluidLevelEmitter;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.coremod.registries.adapters.PartFluidLevelEmitterAdapter;
import com.glodblock.github.coremod.registries.adapters.PartLevelEmitterAdapter;

import appeng.api.networking.IGridHost;
import appeng.parts.automation.PartLevelEmitter;

public class LevelTerminalRegistry implements ILevelTerminalRegistry {

    private final Set<Class<? extends ILevelViewable>> supportedClasses = new HashSet<>();
    private final Map<Class<? extends ILevelViewable>, Class<? extends IGridHost>> adapterMap = new WeakHashMap<>();
    private final Map<Class<? extends ILevelViewable>, ILevelViewableAdapter> adapterInstanceMap = new WeakHashMap<>();

    private static class Singleton {

        private static final ILevelTerminalRegistry INSTANCE = new LevelTerminalRegistry();
    }

    {
        this.register(TileLevelMaintainer.class);
        this.register(PartFluidLevelEmitter.class, new PartFluidLevelEmitterAdapter());
        this.register(PartLevelEmitter.class, new PartLevelEmitterAdapter());
    }

    public Set<Class<? extends ILevelViewable>> getSupportedClasses() {
        return supportedClasses;
    }

    @Override
    public boolean isAdopted(Class<? extends ILevelViewable> clazz) {
        return adapterMap.containsKey(clazz);
    }

    @Override
    public Class<? extends IGridHost> getAdopted(Class<? extends ILevelViewable> clazz) {
        return adapterMap.getOrDefault(clazz, null);
    }

    @Override
    public ILevelViewableAdapter getAdapter(Class<? extends ILevelViewable> clazz) {
        return adapterInstanceMap.getOrDefault(clazz, null);
    }

    public static ILevelTerminalRegistry instance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void register(Class<? extends ILevelViewable> clazz) {
        supportedClasses.add(clazz);
    }

    @Override
    public void register(Class<? extends IGridHost> aeClass, ILevelViewableAdapter adapter) {
        var aClass = adapter.getClass();
        supportedClasses.add(aClass);
        adapterMap.put(aClass, aeClass);
        adapterInstanceMap.put(aClass, adapter);
    }
}
