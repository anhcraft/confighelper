package dev.anhcraft.configdoc.internal;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextReplacer {
    public static final Pattern INFO_PLACEHOLDER_PATTERN = Pattern.compile("\\{[a-zA-Z0-9?_. ]+}");
    private UnaryOperator<String> handler;

    public TextReplacer(UnaryOperator<String> handler) {
        this.handler = handler;
    }

    @NotNull
    public String replace(@NotNull String str){
        Matcher m = INFO_PLACEHOLDER_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer(str.length());
        while(m.find()){
            String p = m.group();
            String s = p.substring(1, p.length()-1).trim();
            m.appendReplacement(sb, handler.apply(s));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
