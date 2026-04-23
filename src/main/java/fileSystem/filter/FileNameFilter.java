package fileSystem.filter;

import fileSystem.AbstractNode;

import java.util.regex.Pattern;

public class FileNameFilter implements NodeFilter {

    @Override
    public boolean apply(AbstractNode node, SearchParams params) {
        if (params.getFileNameRegex() == null) {
            return true;    // No name constraint - pass through
        }

        return Pattern.matches(params.getFileNameRegex(), node.getName());
    }
}
