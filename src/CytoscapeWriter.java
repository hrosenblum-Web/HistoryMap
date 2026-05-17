import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * {@link GraphWriter} implementation that writes a self-contained HTML page
 * containing an interactive Cytoscape.js relationship graph with a dagre
 * top-down hierarchical layout.
 *
 * <p>{@link #writeNames(List)} and {@link #writeRelationships(List)} buffer
 * node and edge JSON respectively. {@link #close()} emits the full HTML document,
 * including the Cytoscape initialization block and click-to-navigate handler.
 *
 * <p>Edge styles by relationship type:
 * <ul>
 *   <li>{@code sensei} — solid arrow</li>
 *   <li>{@code family} — thick red arrow</li>
 *   <li>{@code partner} — dashed bidirectional arrow</li>
 *   <li>anything else — dashed arrow with label</li>
 * </ul>
 *
 * <p>Nodes with a portrait image (detected by {@link GraphNode#hasImage()}) use
 * the image as the node background via Cytoscape's {@code background-image} style.
 */
public class CytoscapeWriter implements GraphWriter {

    private static final String CYTOSCAPE_CDN =
        "https://cdn.jsdelivr.net/npm/cytoscape/dist/cytoscape.min.js";
    private static final String DAGRE_CDN =
        "https://cdn.jsdelivr.net/npm/dagre/dist/dagre.min.js";
    private static final String CYTOSCAPE_DAGRE_CDN =
        "https://cdn.jsdelivr.net/npm/cytoscape-dagre/cytoscape-dagre.js";

    private final PrintStream out;
    private final List<String> nodeJson = new ArrayList<>();
    private final List<String> edgeJson = new ArrayList<>();

    /**
     * Constructs the writer. The HTML document is not opened until {@link #close()}
     * to allow all nodes and edges to be buffered first.
     *
     * @param out stream to write the HTML output to (typically a file stream)
     */
    public CytoscapeWriter(PrintStream out) {
        this.out = out;
    }

    /**
     * Buffers Cytoscape node element JSON for each person.
     * Nodes with a portrait image include an {@code image} data field.
     *
     * @param names list of graph nodes to convert
     */
    @Override
    public void writeNames(List<GraphNode> names) {
        for (GraphNode gn : names) {
            String escapedName = gn.getName()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", " ");
            String img = "";
            if (gn.hasImage()) {
                try {
                    byte[] bytes = Files.readAllBytes(
                        Paths.get(GraphNode.PATH + "Images\\" + gn.getId() + ".jpg"));
                    img = ",image:\"data:image/jpeg;base64,"
                        + Base64.getEncoder().encodeToString(bytes) + "\"";
                } catch (IOException e) {
                    // image unreadable — omit it
                }
            }
            nodeJson.add(String.format(
                "{data:{id:\"%s\",label:\"%s\",url:\"%s\"%s}}",
                gn.getId(), escapedName, gn.getUrl(), img));
        }
    }

    /**
     * Buffers Cytoscape edge element JSON strings produced by {@link CytoscapeReader}.
     *
     * @param relationships list of edge JSON strings,
     *                      e.g. {@code {data:{source:"A",target:"B",type:"sensei"}}}
     */
    @Override
    public void writeRelationships(List<String> relationships) {
        edgeJson.addAll(relationships);
    }

    /**
     * Emits the complete HTML document: head, Cytoscape container div, CDN scripts,
     * and the initialization block containing all buffered nodes and edges.
     */
    @Override
    public void close() {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("  <head>");
        out.println("    <meta charset=\"utf-8\">");
        out.println("    <title>Martial Arts history map</title>");
        out.println("    <style>body{margin:0;}#cy{width:100%;height:100vh;}</style>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("    <div id=\"cy\"></div>");
        out.println("    <script src=\"" + CYTOSCAPE_CDN + "\"></script>");
        out.println("    <script src=\"" + DAGRE_CDN + "\"></script>");
        out.println("    <script src=\"" + CYTOSCAPE_DAGRE_CDN + "\"></script>");
        out.println("    <script>");
        out.println("      cytoscape.use(cytoscapeDagre);");
        out.println("      var cy = cytoscape({");
        out.println("        container: document.getElementById('cy'),");
        out.println("        elements: {");
        out.println("          nodes: [" + String.join(",", nodeJson) + "],");
        out.println("          edges: [" + String.join(",", edgeJson) + "]");
        out.println("        },");
        out.println("        layout: {name:'dagre',rankDir:'TB',nodeSep:80,rankSep:120},");
        out.println("        style: [");
        out.println("          {selector:'node',style:{");
        out.println("            label:'data(label)',");
        out.println("            'background-color':'#6FB1FC',");
        out.println("            'text-valign':'bottom',");
        out.println("            'text-margin-y':'4px',");
        out.println("            'font-size':'11px',");
        out.println("            width:70,height:70");
        out.println("          }},");
        out.println("          {selector:'edge',style:{");
        out.println("            'curve-style':'bezier',");
        out.println("            'target-arrow-shape':'triangle',");
        out.println("            'target-arrow-color':'#555',");
        out.println("            'line-color':'#555'");
        out.println("          }},");
        out.println("          {selector:'edge[type=\"sensei\"]',style:{");
        out.println("            'line-color':'#333',");
        out.println("            'target-arrow-color':'#333'");
        out.println("          }},");
        out.println("          {selector:'edge[type=\"family\"]',style:{");
        out.println("            'line-color':'#c00',");
        out.println("            'target-arrow-color':'#c00',");
        out.println("            width:3");
        out.println("          }},");
        out.println("          {selector:'edge[type=\"partner\"]',style:{");
        out.println("            'line-style':'dashed',");
        out.println("            'source-arrow-shape':'triangle',");
        out.println("            'source-arrow-color':'#555',");
        out.println("            'target-arrow-shape':'triangle'");
        out.println("          }},");
        out.println("          {selector:'edge[type!=\"sensei\"][type!=\"family\"][type!=\"partner\"]',style:{");
        out.println("            'line-style':'dashed',");
        out.println("            label:'data(type)',");
        out.println("            'font-size':'9px'");
        out.println("          }}");
        out.println("        ]");
        out.println("      });");
        out.println("      cy.on('tap','node',e => {");
        out.println("        var url = e.target.data('url');");
        out.println("        if (url.startsWith('http')) window.open(url,'_blank');");
        out.println("        else window.location.href = url;");
        out.println("      });");
        out.println("      cy.nodes().filter(n => n.data('image')).forEach(n => {");
        out.println("        n.style({'background-image': n.data('image'),");
        out.println("                 'background-fit': 'cover', 'background-opacity': 0,");
        out.println("                 'background-position-y': '0%', 'shape': 'rectangle'});");
        out.println("      });");
        out.println("    </script>");
        out.println("  </body>");
        out.println("</html>");
    }
}
