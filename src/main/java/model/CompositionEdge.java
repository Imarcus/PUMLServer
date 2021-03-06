package model;

/**
 * Represents a composite relationship between two classes.
 */
public class CompositionEdge extends AbstractEdge {

    public CompositionEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public CompositionEdge(){

    }

    @Override
    public Edge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy) {
        return new CompositionEdge(startNodeCopy, endNodeCopy);
    }

    @Override
    public void setTranslateX(double x) {

    }

    @Override
    public void setTranslateY(double y) {

    }

    @Override
    public void setScaleX(double x) {

    }

    @Override
    public void setScaleY(double y) {

    }

    @Override
    public double getTranslateX() {
        return 0;
    }

    @Override
    public double getTranslateY() {
        return 0;
    }

    @Override
    public double getScaleY() {
        return 0;
    }

    @Override
    public double getScaleX() {
        return 0;
    }

    public String getType(){
        return "Composition";
    }
}
