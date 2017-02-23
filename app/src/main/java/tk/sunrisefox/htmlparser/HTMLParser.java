package tk.sunrisefox.htmlparser;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser {
    final private static Pattern pattern = Pattern.compile("</?\\w+((\\s+[\\w-]+(\\s*=\\s*(?:\".*?\"|'.*?'|[\\^'\">\\s]+))?)+\\s*|\\s*)/?>");
    final private static Pattern tagPattern = Pattern.compile("</?(\\w+)\\s*?");
    final private static Pattern attributePattern = Pattern.compile("([-\\w]+)\\s*=\\s*(?:\"(.*?)\"|'(.*?)'|([^'\">\\s]+))");

    private Stack<HTML.Tag> tagList = new Stack<>();
    private String unsentString;
    private boolean lastTagIsEnd = true;

    public interface Callback {
        void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet);
        void onText(HTML.Tag tag, String text);
        void onTagEnd(HTML.Tag tag);
    }

    public void parse(String html, HTMLParser.Callback callback){
        tagList.push(HTML.Tag.INVALID);
        Queue<Integer> queue = new ArrayDeque<>();
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()){
            queue.offer(matcher.start());
            queue.offer(matcher.end());
        }
        int a, b = 0;
        while (queue.size() != 0){
            a = queue.poll();
            unsentString = html.substring(b,a);
            b = queue.poll();
            tagAnalyser(html.substring(a,b),callback);
        }
    }

    private void tagAnalyser(String string, HTMLParser.Callback callback){
        Matcher tagMatcher = tagPattern.matcher(string);
        HTML.Tag tag = null;
        if(tagMatcher.find())
            tag = new HTML.Tag(tagMatcher.group(1));
        if(!string.startsWith("</")){
            if(!unsentString.trim().isEmpty())
                callback.onText(tagList.peek(), unsentString);
            tagList.push(tag);
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
                tagList.pop();
                callback.onTagEnd(tag);
                lastTagIsEnd = true;
            }else lastTagIsEnd = false;
        } else{
            if(lastTagIsEnd) {
                if (!unsentString.trim().isEmpty())
                    callback.onText(tagList.peek(), unsentString);
            }
            else callback.onText(tagList.peek(), unsentString);
            tagList.pop();
            callback.onTagEnd(tag);
            lastTagIsEnd = true;
        }
    }
}
