package com.glodblock.github.api.registries;

import java.util.Set;

import appeng.api.features.ILevelViewable;
import appeng.api.networking.IGridHost;

public interface ILevelTerminalRegistry {

    void register(Class<? extends ILevelViewable> clazz);

    void register(Class<? extends IGridHost> aeClass, ILevelViewableAdapter adapter);

    Set<Class<? extends ILevelViewable>> getSupportedClasses();

    boolean isAdopted(Class<? extends ILevelViewable> clazz);

    Class<? extends IGridHost> getAdopted(Class<? extends ILevelViewable> clazz);

    ILevelViewableAdapter getAdapter(Class<? extends ILevelViewable> clazz);
}
