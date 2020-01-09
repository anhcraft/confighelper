package dev.anhcraft.confighelper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import dev.anhcraft.confighelper.annotation.Validation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigDoc {
    private final List<ConfigSchema<?>> schemas = new ArrayList<>();
    private final Map<Pattern, String> javaDocs = new HashMap<>();
    private String footer = "<footer>Powered by ConfigDoc/<a href=\"https://github.com/anhcraft/confighelper/\">ConfigHelper</a></footer>";

    public ConfigDoc(){
        addJavadoc("(org.bukkit.*)|(org.spigotmc*)", "https://hub.spigotmc.org/javadocs/spigot/");
        addJavadoc("(com.destroystokyo.paper*)", "https://papermc.io/javadocs/paper/1.14/");
    }

    @Contract("_ -> this")
    public ConfigDoc with(@NotNull ConfigDoc configDoc){
        Preconditions.checkNotNull(configDoc);
        schemas.addAll(configDoc.schemas);
        javaDocs.putAll(configDoc.javaDocs);
        return this;
    }

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
    public ConfigDoc footer(@NotNull String footer){
        Preconditions.checkNotNull(footer);
        this.footer = footer;
        return this;
    }

    @Contract("_, _ -> this")
    public ConfigDoc addJavadoc(@NotNull String classPattern, @NotNull String link){
        Preconditions.checkNotNull(classPattern);
        return addJavadoc(Pattern.compile(classPattern), link);
    }

    @Contract("_, _ -> this")
    public ConfigDoc addJavadoc(@NotNull Pattern classPattern, @NotNull String link){
        Preconditions.checkNotNull(classPattern);
        Preconditions.checkNotNull(footer);
        if(!link.endsWith("/")) link = link + '/';
        javaDocs.put(classPattern, link);
        return this;
    }

    @Contract("_ -> this")
    public ConfigDoc generate(@NotNull File output){
        Preconditions.checkNotNull(output);
        output.mkdirs();

        StringBuilder ovBuilder = new StringBuilder("<!Doctype html><html><head><title>Overview</title><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><meta name=\"robots\" content=\"none\"/><style type=\"text/css\">body{font-family: monospace; font-size: 16px; padding: 25px 50px;margin-bottom: 200px;}a{text-decoration: none; color: #19afdc;}footer{position: fixed; left: 0; bottom: 0; background: #f2d9ff; width: 100%; padding: 5px; text-align: center;font-size: 14px;box-shadow: #676767 1px 1px 10px;}</style></head><body><h1>Overview</h1><br>");

        StringBuilder sideMenuBuilder = new StringBuilder();

        for (ConfigSchema<?> schema : schemas){
            String title = schema.getSchemaClass().getSimpleName();
            sideMenuBuilder.append("<p><a href=\"").append(title).append(".html\">").append(title).append("</a></p>");
        }

        String sideMenuStr = sideMenuBuilder.toString();

        for (ConfigSchema<?> schema : schemas){
            String title = schema.getSchemaClass().getSimpleName();
            Collection<String> keys = schema.listKeys();
            ovBuilder.append("<h3>").append("<a href=\"").append(title).append(".html\">").append(title).append("</a>: ").append(keys.size()).append(" entries </h3>");

            StringBuilder confBuilder = new StringBuilder("<!Doctype html><html><head><title>Config | ").append(title).append("</title><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><meta name=\"robots\" content=\"none\"/><style type=\"text/css\">body{font-family: monospace; font-size: 16px;margin-bottom: 200px;}a{text-decoration: none; color: #19afdc;}table{width: 100%;}table th{font-size: 18px; background-color: #63c37c;}table, table td, table th{border: 1px solid #555; border-collapse: collapse; padding: 7px 12px;}table tr:hover{background: #eee;}header{padding: 15px 50px;}#container{display: grid; grid-template-columns: auto auto auto auto auto auto;grid-gap: 20px;}#right-side{grid-column: 2 / 6;}#left-side{border-right: 1px #e6e6e6 solid;padding: 0 20px;font-size: 16px;}footer{position: fixed; left: 0; bottom: 0; background: #f2d9ff; width: 100%; padding: 5px;text-align: center;font-size: 14px;box-shadow: #676767 1px 1px 10px;}</style></head><body><header><a href=\"index.html\">Overview</a> | <a href=\"javascript:window.history.back()\">Back</a><br/><br/><h1>").append(title).append("</h1>");
            if(schema.getExplanation() != null){
                confBuilder.append("<p>").append(String.join("<br>", schema.getExplanation())).append("</p>");
            }
            confBuilder.append("</header><div id=\"container\"><div id=\"left-side\">").append(sideMenuStr).append("</iframe></div><div id=\"right-side\"><table><tr><th>Key</th><th>Type</th><th>Explanation</th></tr>");

            for (String key : keys){
                ConfigSchema.Entry entry = schema.getEntry(key);
                if(entry == null) continue;
                Field field = entry.getField();
                String fullType = field.getType().getName();
                StringBuilder type = new StringBuilder(field.getType().getSimpleName());
                if(entry.getComponentClass() != null){
                    fullType = entry.getComponentClass().getName();
                    if(!field.getType().isArray()){
                        type.append("&lt;").append(entry.getComponentClass().getSimpleName()).append("&gt;");
                    }
                }
                confBuilder.append("<tr><td>").append(key)
                        .append("</td><td>");
                StringBuilder vb = new StringBuilder(" ");
                if(entry.getValidation() != null){
                    Validation validation = entry.getValidation();
                    if(validation.notNull())
                        vb.append("<b>not-null</b> ");
                    if(validation.notEmptyString() || validation.notEmptyArray() || validation.notEmptyList())
                        vb.append("<b>not-empty</b> ");
                }
                if(entry.getValueSchema() != null && schemas.contains(entry.getValueSchema())){
                    confBuilder.append("<a href=\"").append(entry.getValueSchema().getSchemaClass().getSimpleName()).append(".html\">").append(type).append("</a>");
                } else {
                    boolean found = false;
                    for(Map.Entry<Pattern, String> jd : javaDocs.entrySet()){
                        if(jd.getKey().matcher(fullType).matches()){
                            confBuilder.append("<a href=\"").append(jd.getValue()).append(fullType.replace('.', '/').replace('$', '.')).append(".html\">").append(type).append("</a>");
                            found = true;
                            break;
                        }
                    }
                    if(!found) confBuilder.append(type);
                }
                confBuilder.append(vb).append("</td><td>");
                if(entry.getExplanation() != null){
                    confBuilder.append(Joiner.on("<br>").join(entry.getExplanation()));
                }
                confBuilder.append("</td></tr>");
            }
            confBuilder.append("</table></div></div>").append(footer).append("</body></html>");

            File file = new File(output, title+".html");
            try {
                file.createNewFile();
                Files.write(confBuilder.toString(), file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ovBuilder.append(footer).append("</body></html>");
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
