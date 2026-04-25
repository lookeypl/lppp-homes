package com.lookeypl;

import com.lookeypl.importer.BlossomHomesImporterImpl;
import com.lookeypl.importer.HomeImporterException;
import com.lookeypl.importer.ImporterSource;

import net.minecraft.server.MinecraftServer;


public class HomeImporter {
    public static HomeCollection importHomes(MinecraftServer server, ImporterSource source) throws HomeImporterException {
        switch (source) {
        case BLOSSOMHOMES: return new BlossomHomesImporterImpl().importHomes(server);
        default:
            throw new HomeImporterException("Importer source %s not implemented".formatted(source.name()));
        }
    }
}
