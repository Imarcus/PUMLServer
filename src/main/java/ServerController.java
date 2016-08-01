import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.scene.paint.Color;
import model.*;
import util.Constants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Forward changes in the graph to all clients.
 * Used by MainController for turning the diagram in to a host for clients to connect to.
 */
public class ServerController {

    public static void main(String[] args) {
        ServerController serverController = new ServerController(54555);
    }

    private Graph graph;
    private Server server;
    private int port;
    private int nrClients = 0;

    public ServerController(int pPort) {
        port = pPort;

        server = new Server();
        server.start();
        try {
            server.bind(port,port);
        } catch (IOException e){
            e.printStackTrace();
        }

        graph = new Graph();
        initKryo(server.getKryo());


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {

                }
                else if (object instanceof AbstractNode) {
                    graph.addNode((AbstractNode) object, true);
                    server.sendToAllExceptTCP(connection.getID(), object);
                }
                else if (object instanceof AbstractEdge) {
                    graph.addEdge((AbstractEdge) object, true);
                    server.sendToAllExceptTCP(connection.getID(), object);
                }
                else if (object instanceof String[]){
                    remoteCommand((String[]) object);
                    server.sendToAllExceptTCP(connection.getID(), object);
                } else if (object instanceof Graph){
                    graph = (Graph) object;

                }
            }

            public void connected(Connection c){
                nrClients = server.getConnections().length;
                System.out.println("Client connected, total: " + nrClients);
                server.sendToTCP(c.getID(), graph);
            }

            public void disconnected(Connection c){
            }
        });
        System.out.println("Server running");

    }

    private void initKryo(Kryo kryo){
        kryo.register(ClassNode.class);
        kryo.register(AbstractNode.class);
        kryo.register(PackageNode.class);
        kryo.register(AbstractEdge.class);
        kryo.register(InheritanceEdge.class);
        kryo.register(CompositionEdge.class);
        kryo.register(AssociationEdge.class);
        kryo.register(AggregationEdge.class);
        kryo.register(Graph.class);
        kryo.register(ArrayList.class);
        kryo.register(model.AbstractEdge.Direction.class);
        kryo.register(String[].class);
    }

    public void closeServer(){
        server.close();
    }

    /**
     * Called when the model has been modified remotely.
     * @param dataArray
     * [0] = Type of change
     * [1] = id of node
     * [2+] = Optional new values
     */
    public void remoteCommand(String[] dataArray){
        if(dataArray[0].equals(Constants.changeSketchPoint)){
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.addPointRemote(Double.parseDouble(dataArray[2]), Double.parseDouble(dataArray[3]));
                }
            }
        }
        else if (dataArray[0].equals(Constants.changeSketchStart)){
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.setStartRemote(Double.parseDouble(dataArray[2]), Double.parseDouble(dataArray[3]));
                    sketch.setColor(Color.web(dataArray[4]));
                }
            }
        }
        else if (dataArray[0].equals(Constants.sketchAdd)){
            graph.addSketch(new Sketch(), true);
        }
        else if (dataArray[0].equals(Constants.sketchRemove)){
            Sketch sketchToBeDeleted = null;
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketchToBeDeleted = sketch; //ConcurrentModificationException fix
                    break;
                }
            }
            graph.removeSketch(sketchToBeDeleted, true);
        }
        else if(dataArray[0].equals(Constants.changeNodeTranslateY) || dataArray[0].equals(Constants.changeNodeTranslateX)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetTranslateX(Double.parseDouble(dataArray[2]));
                    node.remoteSetTranslateY(Double.parseDouble(dataArray[3]));
                    node.remoteSetX(Double.parseDouble(dataArray[2]));
                    node.remoteSetY(Double.parseDouble(dataArray[3]));
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeNodeWidth) || dataArray[0].equals(Constants.changeNodeHeight)) {
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetWidth(Double.parseDouble(dataArray[2]));
                    node.remoteSetHeight(Double.parseDouble(dataArray[3]));
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeNodeTitle)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetTitle(dataArray[2]);
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.NodeRemove)) {
            AbstractNode nodeToBeDeleted = null;
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    nodeToBeDeleted = node; //ConcurrentModificationException fix
                    break;
                }
            }
            graph.removeNode(nodeToBeDeleted, true);
        } else if (dataArray[0].equals(Constants.EdgeRemove)) {
            Edge edgeToBeDeleted = null;
            for(Edge edge : graph.getAllEdges()){
                if(dataArray[1].equals(edge.getId())){
                    edgeToBeDeleted = edge;
                    break;
                }
            }
            graph.removeEdge(edgeToBeDeleted, true);
        } else if (dataArray[0].equals(Constants.changeClassNodeAttributes) ||dataArray[0].equals(Constants.changeClassNodeOperations)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    ((ClassNode)node).remoteSetAttributes(dataArray[2]);
                    ((ClassNode)node).remoteSetOperations(dataArray[3]);
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeEdgeStartMultiplicity) || dataArray[0].equals(Constants.changeEdgeEndMultiplicity)){
            for(Edge edge : graph.getAllEdges()){
                if(dataArray[1].equals(edge.getId())){
                    ((AbstractEdge) edge).remoteSetStartMultiplicity(dataArray[2]);
                    ((AbstractEdge) edge).remoteSetEndMultiplicity(dataArray[3]);
                }
            }
        } else if (dataArray[0].equals(Constants.changeSketchTranslateX)) {
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.remoteSetTranslateX(Double.parseDouble(dataArray[2]));
                }
            }
        } else if (dataArray[0].equals(Constants.changeSketchTranslateY)) {
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.remoteSetTranslateY(Double.parseDouble(dataArray[2]));
                }
            }
        }
    }
}
