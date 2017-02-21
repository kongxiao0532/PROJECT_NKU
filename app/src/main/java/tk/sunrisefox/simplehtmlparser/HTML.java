package tk.sunrisefox.simplehtmlparser;

import android.util.Pair;

import java.util.ArrayList;

public class HTML{
    public static class Tag{
        private final String tag;
        Tag(String tag){
            this.tag = tag.toLowerCase();
        }

        public boolean equals(Tag tag) {
            return this.tag.equals(tag.tag.toLowerCase());
        }

        public boolean equals(String tag) {
            return this.tag.equals(tag.toLowerCase());
        }

        public String tag() { return tag; }
    }

    public static class Attribute extends Pair<String, String>{
        Attribute(String key, String value){
            super(key.toLowerCase(),value);
        }
        public String key() { return super.first; }
        public String value() { return super.second; }
    }

    public static class AttributeSet extends ArrayList<Attribute> {
        public boolean hasAttribute(String key){
            key = key.toLowerCase();
            for (Attribute attribute: this){
                if(attribute.key().equals(key)) return true;
            }
            return false;
        }

        public String getAttribute(String key){
            key = key.toLowerCase();
            for (Attribute attribute: this){
                if(attribute.key().equals(key)) return attribute.value();
            }
            return null;
        }
    }
}
