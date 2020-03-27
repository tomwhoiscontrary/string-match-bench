package li.earth.urchin.twic;

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PatternBuilder {

    private static class TrieNode {
        final char character;
        final NavigableMap<Character, TrieNode> children = new TreeMap<>();

        TrieNode(char character) {
            this.character = character;
        }

        public void add(String string) {
            add(string, 0);
        }

        public void add(String string, int index) {
            if (string.length() <= index) return;
            TrieNode child = children.computeIfAbsent(string.charAt(index), TrieNode::new);
            child.add(string, index + 1);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            toString(buf);
            return buf.toString();
        }

        private void toString(StringBuilder buf) {
            buf.append(character);
            if (children.size() == 0) {
                // done
            } else if (children.size() == 1) {
                children.firstEntry().getValue().toString(buf);
            } else if (children.size() > 1) {
                buf.append("(?:");
                for (Iterator<TrieNode> childIterator = children.values().iterator(); childIterator.hasNext(); ) {
                    TrieNode child = childIterator.next();
                    child.toString(buf);
                    if (childIterator.hasNext()) buf.append("|");
                }
                buf.append(")");
            }
        }
    }

    /**
     * Makes the minimal pattern which matches exactly the specified set of strings.
     */
    public static String minimalPattern(Collection<String> strings) {
        TrieNode root = new TrieNode('\0');

        strings.forEach(root::add);

        return root.toString().substring(1);
    }

}
