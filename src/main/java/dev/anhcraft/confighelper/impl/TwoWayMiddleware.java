package dev.anhcraft.confighelper.impl;

import dev.anhcraft.confighelper.ConfigSchema;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface TwoWayMiddleware {
    @Nullable
    Object conf2schema(ConfigSchema.Entry entry, @Nullable Object value);

    @Nullable
    Object schema2conf(ConfigSchema.Entry entry, @Nullable Object value);
}
