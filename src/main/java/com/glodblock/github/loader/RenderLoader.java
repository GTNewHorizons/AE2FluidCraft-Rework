package com.glodblock.github.loader;

import com.glodblock.github.client.render.ItemCertusQuartzTankRender;
import com.glodblock.github.client.render.ItemDropRender;
import com.glodblock.github.client.render.ItemPacketRender;
import com.glodblock.github.client.render.ItemWalrusRender;
import com.glodblock.github.client.render.WalrusRenderer;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        WalrusRenderer.load();
        new ItemDropRender();
        new ItemPacketRender();
        new ItemWalrusRender();
        new ItemCertusQuartzTankRender();
    }
}
