package fileSystem.filter;

import fileSystem.AbstractNode;
import fileSystem.FileNode;

public class FileSizeFilter implements NodeFilter {

    @Override
    public boolean apply(AbstractNode node, SearchParams params) {
        // Size constraints only apply to files, not directories
        if (!(node instanceof FileNode file)) {
            return true;
        }

        if (params.getMinSize() == null && params.getMaxSize() == null) {
            return true;    // No size constraint - pass through
        }

        int fileSize = file.getSize();
        boolean aboveMin = params.getMinSize() == null || fileSize >= params.getMinSize();
        boolean belowMax = params.getMaxSize() == null || fileSize <= params.getMaxSize();

        return aboveMin && belowMax;
    }
}
