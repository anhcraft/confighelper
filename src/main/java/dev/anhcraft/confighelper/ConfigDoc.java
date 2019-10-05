package dev.anhcraft.confighelper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigDoc {
    private final List<ConfigSchema<?>> schemas = new ArrayList<>();

    @Contract("_ -> this")
    public ConfigDoc withSchema(@NotNull ConfigSchema<?> schema){
        Preconditions.checkNotNull(schema);
        schemas.add(schema);
        return this;
    }

    @Contract("_ -> this")
    public ConfigDoc withSchemaOf(@NotNull Class<?> schemaClass){
        Preconditions.checkNotNull(schemaClass);
        schemas.add(ConfigSchema.of(schemaClass));
        return this;
    }

    @Contract("_ -> this")
    public ConfigDoc generate(@NotNull File output){
        Preconditions.checkNotNull(output);
        output.mkdirs();

        StringBuilder ovBuilder = new StringBuilder("<!Doctype html><html><head><title>Overview</title><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><meta name=\"robots\" content=\"none\"/><style type=\"text/css\">body{font-family: monospace; font-size: 16px; padding: 25px 50px;}a{text-decoration: none; color: #19afdc;}</style></head><body><h1>Overview</h1>");

        for (ConfigSchema<?> schema : schemas){
            String title = schema.getSchemaClass().getSimpleName();
            Collection<String> keys = schema.listKeys();
            ovBuilder.append("<h3>").append("<a href=\"").append(title).append(".html\">").append(title).append("</a>: ").append(keys.size()).append(" entries </h3>");

            StringBuilder confBuilder = new StringBuilder("<!Doctype html><html><head><title>Config | ").append(title).append("</title><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><meta name=\"robots\" content=\"none\"/><style type=\"text/css\">body{font-family: monospace; font-size: 16px; padding: 25px 50px;}a{text-decoration: none; color: #19afdc;}table{width: 100%;}table th{font-size: 18px; background-color: #63c37c;}table, table td, table th{border: 1px solid #555; border-collapse: collapse; padding: 7px 12px;}</style></head><body><a href=\"index.html\">Overview</a> | <a href=\"javascript:window.history.back()\">Back</a><br/><br/><h1>").append(title).append("</h1><table><tr><th>Key</th><th>Type</th><th>Restriction</th><th>Explanation</th></tr>");

            for (String key : keys){
                ConfigSchema.Entry entry = schema.getEntry(key);
                if(entry == null) continue;
                String type = entry.getComponentClass() == null ?
                        entry.getField().getType().getSimpleName() :
                        entry.getComponentClass().getSimpleName();
                confBuilder.append("<tr><td>").append(key);
                confBuilder.append("</td><td>");
                if(entry.getConfigSchema() != null && schemas.contains(entry.getConfigSchema())){
                    confBuilder.append("<a href=\"").append(entry.getConfigSchema().getSchemaClass().getSimpleName())
                            .append(".html\">").append(type).append("</a>");
                } else {
                    confBuilder.append(type);
                }
                confBuilder.append("</td><td>");
                if(entry.getValidation() != null){
                    Validation validation = entry.getValidation();
                    StringBuilder vb = new StringBuilder();
                    if(validation.notNull())
                        vb.append("<b>not-null</b> ");
                    if(validation.notEmptyString() || validation.notEmptyArray() || validation.notEmptyList())
                        vb.append("<b>not-empty</b> ");
                    confBuilder.append(vb);
                }
                confBuilder.append("</td><td>");
                if(entry.getExplanation() != null){
                    confBuilder.append(Joiner.on("<br>").join(entry.getExplanation().value()));
                }
                confBuilder.append("</td></tr>");
            }
            confBuilder.append("</table></body></html>");

            File file = new File(output, title+".html");
            try {
                file.createNewFile();
                Files.write(confBuilder.toString(), file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file = new File(output, "index.html");
        try {
            file.createNewFile();
            Files.write(ovBuilder.toString(), file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
