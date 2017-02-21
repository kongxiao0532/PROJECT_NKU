package tk.sunrisefox.simplehtmlparser;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser {
    final private static Pattern pattern = Pattern.compile("</?\\w+((\\s+[\\w-]+(\\s*=\\s*(?:\".*?\"|'.*?'|[\\^'\">\\s]+))?)+\\s*|\\s*)/?>");
    final private static Pattern tagPattern = Pattern.compile("</?(\\w+)\\s*?");
    final private static Pattern attributePattern = Pattern.compile("([-\\w]+)\\s*=\\s*(?:\"(.*?)\"|'(.*?)'|([^'\">\\s]+))");
    public interface Callback {
        void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet);
        void onText(String text);
        void onTagEnd(HTML.Tag tag);
    }

    public static void parse(String html, Callback callback){
        Queue<Integer> queue = new ArrayDeque<>();
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()){
            queue.offer(matcher.start());
            queue.offer(matcher.end());
        }
        int a, b = 0;
        while (queue.size() != 0){
            a = queue.poll();
            callback.onText(html.substring(b,a).trim());
            b = queue.poll();
            tagAnalyser(html.substring(a,b),callback);
        }
    }

    private static void tagAnalyser(String string, Callback callback){
        Matcher tagMatcher = tagPattern.matcher(string);
        HTML.Tag tag = null;
        if(tagMatcher.find())
            tag = new HTML.Tag(tagMatcher.group(1));
        if(!string.startsWith("</")){
            Matcher attributeMatcher = attributePattern.matcher(string);
            HTML.AttributeSet attributeSet = new HTML.AttributeSet();
            while (attributeMatcher.find()){
                String group = attributeMatcher.group(2);
                if(group == null) group = attributeMatcher.group(3);
                if(group == null) group = attributeMatcher.group(4);
                if(group == null) return;
                attributeSet.add(new HTML.Attribute(attributeMatcher.group(1),group));
            }
            callback.onTagStart(tag,attributeSet);
            if(string.endsWith("/>")){
                callback.onTagEnd(tag);
            }
        } else{
            callback.onTagEnd(tag);
        }
    }
}
