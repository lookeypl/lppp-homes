package com.lookeypl.importer;

import com.lookeypl.HomeCollection;
import net.minecraft.server.MinecraftServer;

public interface ImporterImpl {
    public HomeCollection importHomes(MinecraftServer server) throws HomeImporterException;
    public ImporterSource getSourceType();
}
