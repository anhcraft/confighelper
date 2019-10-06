package dev.anhcraft.confighelper.impl;

import dev.anhcraft.confighelper.ConfigSchema;
import dev.anhcraft.confighelper.annotation.Middleware;
import org.jetbrains.annotations.Nullable;

public interface TwoWayMiddleware {
    @Middleware
    @Nullable
    Object conf2schema(ConfigSchema.Entry entry, @Nullable Object value);

    @Middleware
    @Nullable
    Object schema2conf(ConfigSchema.Entry entry, @Nullable Object value);
}
