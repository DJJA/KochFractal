package calculate;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class KochEdgeCalculator extends Task<List<Edge>> implements Observer{

    private KochFractal koch;
    private EdgeSide side;

    public KochEdgeCalculator(KochFractal koch, EdgeSide side) {
        this.koch = koch;
        this.side = side;
    }

    public EdgeSide getSide() {
        return side;
    }

    private List<Edge> generateEdges(){
        switch (side){
            case LEFT:
                koch.generateLeftEdge();
                return koch.getEdges();
            case RIGHT:
                koch.generateRightEdge();
                return koch.getEdges();
            case BOTTOM:
                koch.generateBottomEdge();
                return koch.getEdges();
            default:
                return new ArrayList<Edge>();
        }
    }

    @Override
    protected List<Edge> call() throws Exception {
        return generateEdges();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(!isCancelled()) {
            updateProgress(koch.getNrEdgesCalculated(), koch.calculateEdgeCountPerEdge());
            updateMessage(koch.getNrEdgesCalculated() + " / " + koch.calculateEdgeCountPerEdge());
        }
    }
}
