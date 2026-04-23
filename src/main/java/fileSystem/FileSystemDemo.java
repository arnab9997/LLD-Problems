package fileSystem;

import fileSystem.filter.FileNameFilter;
import fileSystem.filter.FileSizeFilter;
import fileSystem.filter.NodeFilterChain;
import fileSystem.filter.SearchParams;

import java.util.List;

public class FileSystemDemo {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        // mkdir — create nested directories
        fs.mkdir("/a/b/c");

        // pwd — should print "/"
        System.out.println("pwd: " + fs.pwd());

        // cd — absolute
        fs.cd("/a/b");
        System.out.println("pwd after cd /a/b: " + fs.pwd());

        // touch — create empty files in current directory (/a/b)
        fs.touch("notes.txt");
        fs.touch("readme.md");

        // ls — list current directory
        System.out.println("ls /a/b: " + fs.ls());

        // echo — write content, absolute path
        fs.echo("/a/b/c/file1.txt", "Hello");
        fs.echo("/a/b/c/file2.log", "Data");
        fs.echo("/a/file3.txt", "Some large content to increase file size...");
        fs.echo("/a/file4.txt", "Short");

        // cd — relative
        fs.cd("c");
        System.out.println("pwd after relative cd c: " + fs.pwd());

        // ls — current directory after relative cd
        System.out.println("ls after cd c: " + fs.ls());

        // cat — relative (file in current directory)
        System.out.println("cat file1.txt: " + fs.cat("file1.txt"));

        // cat — absolute path
        System.out.println("cat /a/file3.txt: " + fs.cat("/a/file3.txt"));

        // searchFilesAndDirectories — filter chain + typed params
        System.out.println("\nSearching under /a for .txt files between 5 and 50 bytes:");

        NodeFilterChain filterChain = new NodeFilterChain();
        filterChain.addFilter(new FileNameFilter());
        filterChain.addFilter(new FileSizeFilter());

        SearchParams params = SearchParams.byNameAndSize("file.*\\.txt", 5, 50);
        List<AbstractNode> foundNodes = fs.searchFilesAndDirectories("/a", filterChain, params);

        for (AbstractNode node : foundNodes) {
            if (node instanceof FileNode file) {
                System.out.println("[FILE] " + file.getName() + " (Size: " + file.getSize() + " bytes)");
            } else {
                System.out.println("[DIR]  " + node.getName());
            }
        }
    }
}