import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.junit.Test;

public class MdTest {

    @Test
    public void testMarkdownParsing() {
        String s = "#header\n\n\nsome text\n\n\n**bold text**";

        Parser parser = Parser.builder()
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();
        Node node = parser.parse(s);
        node.accept(new AbstractVisitor() {
            @Override
            public void visit(HardLineBreak hardLineBreak) {
                super.visit(hardLineBreak);
                System.out.println("hardLineBreak");
            }

            @Override
            public void visit(SoftLineBreak softLineBreak) {
                super.visit(softLineBreak);
                System.out.println("hardLineBreak");
            }
        });
    }
}
