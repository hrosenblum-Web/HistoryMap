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
                        Paths.get(GraphNode.PATH, gn.getImage()));
                    img = ",image:\"data:image/jpeg;base64,"
                        + Base64.getEncoder().encodeToString(bytes) + "\"";
                } catch (IOException e) {
                    // image unreadable — omit it
                }
            }
            String external = gn.hasExternalUrl() ? ",external:true" : "";
            nodeJson.add(String.format(
                "{data:{id:\"%s\",label:\"%s\",url:\"%s\"%s%s}}",
                gn.getId(), escapedName, gn.getUrl(), img, external));
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
        out.println("    <style>body{margin:0;}#cy{width:100%;height:100vh;}#cy-tooltip{position:fixed;display:none;background:rgba(0,0,0,0.75);color:#fff;padding:4px 8px;border-radius:4px;font-family:sans-serif;font-size:12px;pointer-events:none;z-index:1000;white-space:nowrap;}</style>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("    <div id=\"cy\"></div>");
        out.println("    <div id=\"controls\" style=\"position:fixed;top:10px;left:10px;z-index:999;background:rgba(255,255,255,0.9);padding:8px 12px;border-radius:6px;box-shadow:0 2px 6px rgba(0,0,0,0.2);font-family:sans-serif;font-size:13px;\">");
        out.println("      <label for=\"filterType\" style=\"display:block;margin-bottom:4px;\">Filter relationships:</label>");
        out.println("      <select id=\"filterType\" multiple size=\"5\" style=\"display:block;\"></select>");
        out.println("      <div style=\"font-size:11px;color:#888;margin-top:4px;\">None selected = show all</div>");
        out.println("    </div>");
        out.println("    <div id=\"cy-tooltip\"></div>");
        out.println("    <script src=\"" + CYTOSCAPE_CDN + "\"></script>");
        out.println("    <script src=\"" + DAGRE_CDN + "\"></script>");
        out.println("    <script src=\"" + CYTOSCAPE_DAGRE_CDN + "\"></script>");
        out.println("    <script>");
        out.println("      cytoscape.use(cytoscapeDagre);");
        out.println("      var dagreLayout = {name:'dagre',rankDir:'TB',nodeSep:80,rankSep:120};");
        out.println("      var cy = cytoscape({");
        out.println("        container: document.getElementById('cy'),");
        out.println("        elements: {");
        out.println("          nodes: [" + String.join(",", nodeJson) + "],");
        out.println("          edges: [" + String.join(",", edgeJson) + "]");
        out.println("        },");
        out.println("        layout: dagreLayout,");
        out.println("        style: [");
        out.println("          {selector:'node',style:{");
        out.println("            label:'data(label)',");
        out.println("            'background-color':'#6FB1FC',");
        out.println("            'text-valign':'bottom',");
        out.println("            'text-margin-y':'4px',");
        out.println("            'font-size':'11px',");
        out.println("            width:70,height:70");
        out.println("          }},");
        out.println("          {selector:'node[external]',style:{");
        out.println("            'background-color':'black'");
        out.println("          }},");
        out.println("          {selector:'node[image][!external]',style:{");
        out.println("            'border-width':3,");
        out.println("            'border-color':'#6FB1FC'");
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
        out.println("      var tip = document.getElementById('cy-tooltip');");
        out.println("      cy.on('mouseover','node',function(e){");
        out.println("        var n = e.target;");
        out.println("        tip.textContent = n.data('label') + ' (' + (n.data('external') ? 'external' : 'internal') + ' link)';");
        out.println("        tip.style.display = 'block';");
        out.println("      });");
        out.println("      cy.on('mousemove','node',function(e){");
        out.println("        tip.style.left = (e.originalEvent.clientX + 14) + 'px';");
        out.println("        tip.style.top  = (e.originalEvent.clientY + 14) + 'px';");
        out.println("      });");
        out.println("      cy.on('mouseout','node',function(){ tip.style.display = 'none'; });");
        out.println("      cy.nodes().filter(n => n.data('image')).forEach(n => {");
        out.println("        n.style({'background-image': n.data('image'),");
        out.println("                 'background-fit': 'cover', 'background-opacity': 0,");
        out.println("                 'background-position-y': '0%', 'shape': 'rectangle'});");
        out.println("      });");
        out.println("      var filterEl = document.getElementById('filterType');");
        out.println("      var edgeTypes = new Set();");
        out.println("      cy.edges().forEach(function(e){ edgeTypes.add(e.data('type')); });");
        out.println("      Array.from(edgeTypes).sort().forEach(function(t){");
        out.println("        var opt = document.createElement('option');");
        out.println("        opt.value = t;");
        out.println("        opt.textContent = t.charAt(0).toUpperCase() + t.slice(1);");
        out.println("        filterEl.appendChild(opt);");
        out.println("      });");
        out.println("      filterEl.addEventListener('change', function(){");
        out.println("        var selected = new Set(Array.from(this.selectedOptions).map(function(o){ return o.value; }));");
        out.println("        cy.elements(':hidden').show();");
        out.println("        if (selected.size > 0) {");
        out.println("          cy.edges().filter(function(e){ return !selected.has(e.data('type')); }).hide();");
        out.println("          cy.nodes().filter(function(n){");
        out.println("            return n.connectedEdges(':visible').length === 0 && n.connectedEdges().length > 0;");
        out.println("          }).hide();");
        out.println("        }");
        out.println("        cy.elements(':visible').layout(dagreLayout).run();");
        out.println("      });");
        out.println("    </script>");
        out.println("  </body>");
        out.println("</html>");
    }
}
