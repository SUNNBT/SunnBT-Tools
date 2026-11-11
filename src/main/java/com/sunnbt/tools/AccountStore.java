package com.sunnbt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * 离线账号列表：.minecraft/config/autorename-accounts.txt，一行一个名字。
 */
public final class AccountStore {

    private static final String FILE_NAME = "config/autorename-accounts.txt";

    private static final List<String> NAMES = new ArrayList<String>();
    private static boolean loaded = false;

    private AccountStore() {
    }

    public static synchronized List<String> getNames() {
        load();
        return NAMES;
    }

    public static synchronized void add(String name) {
        load();
        if (name == null || name.isEmpty()) {
            return;
        }
        for (String existing : NAMES) {
            if (existing.equalsIgnoreCase(name)) {
                return;
            }
        }
        NAMES.add(name);
        save();
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        File file = file();
        if (file == null || !file.isFile()) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !RenameHelper.isValidName(line)) {
                    continue;
                }
                boolean duplicate = false;
                for (String existing : NAMES) {
                    if (existing.equalsIgnoreCase(line)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    NAMES.add(line);
                }
            }
        } catch (Throwable throwable) {
            SunnBTMods.LOGGER.error("[SunnBT] 读取离线账号列表失败", throwable);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void save() {
        File file = file();
        if (file == null) {
            return;
        }
        File dir = file.getParentFile();
        if (dir != null && !dir.isDirectory()) {
            dir.mkdirs();
        }
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            for (String name : NAMES) {
                writer.write(name);
                writer.write("\r\n");
            }
        } catch (Throwable throwable) {
            SunnBTMods.LOGGER.error("[SunnBT] 保存离线账号列表失败", throwable);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static File file() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.mcDataDir == null) {
                return null;
            }
            return new File(mc.mcDataDir, FILE_NAME);
        } catch (Throwable throwable) {
            return null;
        }
    }
}
