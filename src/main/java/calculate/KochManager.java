/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import fun3kochfractalfx.FUN3KochFractalFX;
import javafx.application.Platform;
import javafx.concurrent.Task;
import timeutil.TimeStamp;

/**
 *
 * @author Nico Kuijpers
 * Modified for FUN3 by Gertjan Schouten
 */
public class KochManager implements Observer, Runnable{
    
//    private KochFractal koch;
    private ArrayList<Edge> edges;
    private FUN3KochFractalFX application;
    private TimeStamp tsCalc;
    private TimeStamp tsDraw;
    private boolean cancelled;
    private boolean running;

    KochFractal kochLeft = new KochFractal();
    KochFractal kochRight = new KochFractal();
    KochFractal kochBottom = new KochFractal();

    private ExecutorService pool = Executors.newFixedThreadPool(3);
//    private ExecutorService pool = Executors.newCachedThreadPool();
//    private ExecutorService pool = Executors.newSingleThreadExecutor();
//    private ExecutorService pool = new ThreadPoolExecutor()

    KochEdgeCalculator kochCalculatorLeft;
    KochEdgeCalculator kochCalculatorRight;
    KochEdgeCalculator kochCalculatorBottom;

    public KochManager(FUN3KochFractalFX application) {
        this.edges = new ArrayList<Edge>();
        //this.koch = new KochFractal(this);
        this.application = application;
        this.tsCalc = new TimeStamp();
        this.tsDraw = new TimeStamp();
        running = false;
        cancelled = false;
    }

    public void changeLevel(int nxt) {
        if(running){
            cancel();
            while(running){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cancelled = false;
        }

        running = true;

        application.clearKochPanel();

        edges.clear();

        kochLeft = new KochFractal();
        kochRight = new KochFractal();
        kochBottom = new KochFractal();

        kochLeft.setLevel(nxt);
        kochRight.setLevel(nxt);
        kochBottom.setLevel(nxt);

        kochLeft.addObserver(this);
        kochRight.addObserver(this);
        kochBottom.addObserver(this);

        tsCalc.init();
        tsCalc.setBegin("Begin calculating");

        tsDraw.init();
        tsDraw.setBegin("Begin drawing");

        kochCalculatorLeft = new KochEdgeCalculator(kochLeft, EdgeSide.LEFT);
        kochCalculatorRight = new KochEdgeCalculator(kochRight, EdgeSide.RIGHT);
        kochCalculatorBottom = new KochEdgeCalculator(kochBottom, EdgeSide.BOTTOM);

        kochLeft.addObserver(kochCalculatorLeft);
        kochRight.addObserver(kochCalculatorRight);
        kochBottom.addObserver(kochCalculatorBottom);

        application.progressLeft.progressProperty().bind(kochCalculatorLeft.progressProperty());
        application.progressRight.progressProperty().bind(kochCalculatorRight.progressProperty());
        application.progressBottom.progressProperty().bind(kochCalculatorBottom.progressProperty());

        application.progressLeftEdgesNr.textProperty().bind(kochCalculatorLeft.messageProperty());
        application.progressRightEdgesNr.textProperty().bind(kochCalculatorRight.messageProperty());
        application.progressBottomEdgesNr.textProperty().bind(kochCalculatorBottom.messageProperty());

        pool.submit(kochCalculatorLeft);
        pool.submit(kochCalculatorRight);
        pool.submit(kochCalculatorBottom);


        new Thread(this).start();
    }

    public void cancel(){
        this.cancelled = true;
        kochLeft.cancel();
        kochRight.cancel();
        kochBottom.cancel();
//        kochCalculatorLeft.cancel();
//        kochCalculatorRight.cancel();
//        kochCalculatorBottom.cancel();
    }

    public void exit(){
        cancel();
        pool.shutdown();
    }

//    public void exit(){
//        if(running){
//            cancel();
//            while(running){
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            cancelled = false;
//        }
//    }

    public void drawEdges() {
        tsDraw.init();
        tsDraw.setBegin("Begin drawing");
        application.clearKochPanel();
        for (Edge e : edges) {
            application.drawEdge(e);
        }
        tsDraw.setEnd("End drawing");
        application.setTextDraw(tsDraw.toString());
    }

    public void drawEdges(List<Edge> edges) {
        application.clearKochPanel();
        for (Edge e : edges) {
            application.drawEdge(e);
        }
        application.setTextDraw(tsDraw.toString());
    }
    
    public synchronized void addEdge(Edge e) {
        edges.add(e);
    }

    public synchronized void addEdges(List<Edge> edges){
        if(!cancelled) {
            this.edges.addAll(edges);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(cancelled)
            return;

       Edge e = (Edge)arg;

        try {
            Thread.sleep(0, 1);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

       Platform.runLater(new Runnable() {
           @Override
           public void run() {
//                    application.progressLeft.setProgress(kochLeft.calculateProgress());
//                    application.progressRight.setProgress(kochRight.calculateProgress());
//                    application.progressBottom.setProgress(kochBottom.calculateProgress());
//
//                    application.progressLeftEdgesNr.setText(kochLeft.getNrEdgesCalculated() + " / " + kochLeft.calculateEdgeCountPerEdge());
//                    application.progressRightEdgesNr.setText(kochRight.getNrEdgesCalculated() + " / " + kochRight.calculateEdgeCountPerEdge());
//                    application.progressBottomEdgesNr.setText(kochBottom.getNrEdgesCalculated() + " / " + kochBottom.calculateEdgeCountPerEdge());

                    application.drawEdge(e);
                }
       });
    }

    @Override
    public void run() {
        //                while(!cancelled) {
//                    if (kochCalculatorLeft.isDone() && kochCalculatorRight.isDone() && kochCalculatorBottom.isDone()) {
        List<Edge> edges = null;
        try {
            edges = kochCalculatorLeft.get();
            this.edges.addAll(edges);
            edges = kochCalculatorRight.get();
            this.edges.addAll(edges);
            edges = kochCalculatorBottom.get();
            this.edges.addAll(edges);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(!cancelled) {
            tsCalc.setEnd("End calculating");
            tsDraw.setEnd("End drawing");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    application.setTextNrEdges("" + (kochLeft.getNrOfEdges() + kochRight.getNrOfEdges() + kochBottom.getNrOfEdges()));
                    application.setTextCalc(tsCalc.toString());
                    application.setTextDrawLater(tsDraw.toString());
                }
            });
        }
//                        break;  // Break out of the while loop, so we don't keep updating the GUI while it has already finished
//                    }
//                }
        running = false;
    }
}
