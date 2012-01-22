/**
 * 
 */
package org.versacloud;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.versacloud.model.Node;

public class HGDBCreateSample {

    HyperGraph graph;

    @BeforeBenchClass
    public void beforeClass() {
        String databaseLocation = "/tmp/bla";
        recursiveDelete(new File(databaseLocation));
        graph = new HyperGraph(databaseLocation);
        fill(graph, 10000);
    }

    public static void main(String[] args) {

        final Benchmark bench = new Benchmark(new Config());
        bench.add(HGDBCreateSample.class);

        final BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);

        // handles(childNode, graph, handle1, handle2);

        // HGBergeLink link = new HGBergeLink(handle1, handle2);
        // graph.add(link);

    }

    @Bench
    public void query() {
        List nodes = hg.getAll(graph,
                hg.and(hg.type(Node.class), hg.eq("key", 1l)));
        // List nodes = hg.getAll(graph, hg.type(Node.class));
        for (Object n : nodes) {
            // System.out.println(n);
        }
    }

    @Bench(beforeFirstRun = "index")
    public void queryIndexed() {
        List nodes = hg.getAll(graph,
                hg.and(hg.type(Node.class), hg.eq("key", 1l)));
        // List nodes = hg.getAll(graph, hg.type(Node.class));
        for (Object n : nodes) {
            // System.out.println(n);
        }
    }

    public void index() {
        HGHandle handle = graph.getTypeSystem().getTypeHandle(Node.class);
        graph.getIndexManager().register(new ByPartIndexer(handle, "key"));
    }

    private static void handles(final Node node, final HyperGraph graph,
            final HGHandle handle1, final HGHandle handle2) {
        Object obj = graph.getHandle(node);

        // Updating operation
        node.setKey(54);
        graph.update(node);
        // removing of a handle and setting the node to annother handle
        graph.remove(handle1);
        graph.replace(handle2, node);

        // see if reset works
        obj = graph.getHandle(node);

        // Working with type handles -> Not working atm
        // Object clazz1 = graph.getTypeSystem().getType(handle1);
        // Object clazz2 = graph.getTypeSystem().getType(handle2);

        // getting object
        obj = graph.get(handle2);
        System.out.println(obj);
    }

    private static void fill(final HyperGraph graph, final int elements) {
        String name = "root";
        Random ran = new Random(elements);
        byte[] secret = new byte[100];

        for (int i = 0; i < elements; i++) {
            ran.nextBytes(secret);
            // Inserting node
            Node node = new Node(name + i, i, secret);
            graph.add(node);
        }
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    private static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    final static int RUNS = 10;
    final static AbstractMeter[] METERS = { new TimeMeter(Time.MilliSeconds) };
    final static AbstractOutput[] OUTPUT = {/*
                                             * new TabularSummaryOutput()
                                             */};
    final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
    final static double GCPROB = 1.0d;

    static class Config extends AbstractConfig {

        /**
         * @param paramRuns
         * @param paramMeters
         * @param paramOutput
         * @param paramArr
         * @param paramGC
         */
        public Config() {
            super(RUNS, METERS, OUTPUT, ARRAN, GCPROB);
        }

    }

}
