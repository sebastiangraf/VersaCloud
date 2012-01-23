/**
 * 
 */
package org.versacloud;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterBenchClass;
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
    List<HGHandle> nodes = new ArrayList<HGHandle>();

    static Random ran = new Random();

    public static void main(String[] args) {

        // Benchmark
        // final Benchmark bench = new Benchmark(new Config());
        // bench.add(HGDBCreateSample.class);
        //
        // final BenchmarkResult res = bench.run();
        // new TabularSummaryOutput().visitBenchmark(res);

        HGDBCreateSample sample = new HGDBCreateSample();
        sample.beforeClass();
        sample.queryIndexed();
        sample.queryLinks();
        sample.afterClass();
    }

    @BeforeBenchClass
    public void beforeClass() {
        String databaseLocation = "/tmp/bla";
        recursiveDelete(new File(databaseLocation));
        graph = new HyperGraph(databaseLocation);
        fillAndIndex(graph, 100);
        addEdges(graph, 100);
    }

    @AfterBenchClass
    public void afterClass() {
        graph.close();
    }

    @Bench
    public void queryIndexed() {
        nodes.clear();
        nodes = hg
                .findAll(graph, hg.and(hg.type(Node.class), hg.eq("key", 1l)));
        // List nodes = hg.getAll(graph, hg.type(Node.class));
        // for (Object n : nodes) {
        // // System.out.println(n);
        // }
    }

    @Bench(beforeFirstRun = "queryIndexed")
    public void queryLinks() {
        for (int i = 0; i < nodes.size(); i++) {
            List<Object> edges = hg.getAll(graph, hg.incident(nodes.get(i)));
            HGBergeLink link = (HGBergeLink) edges.get(0);
        }

    }

    private static void fillAndIndex(final HyperGraph graph, final int elements) {
        String name = "root";
        byte[] secret = new byte[100];
        for (int i = 0; i < elements; i++) {
            ran.nextBytes(secret);
            // Inserting node
            Node node = new Node(name + i, i, secret);
            graph.add(node);
        }
        HGHandle handle = graph.getTypeSystem().getTypeHandle(Node.class);
        graph.getIndexManager().register(new ByPartIndexer(handle, "key"));
        graph.runMaintenance();
    }

    private static void addEdges(final HyperGraph graph, final int edges) {
        List<HGHandle> nodes = hg.findAll(graph, hg.type(Node.class));
        for (int i = 0; i < edges; i++) {

            final HGHandle[] heads = new HGHandle[ran.nextInt(10)];
            final HGHandle[] tails = new HGHandle[ran.nextInt(10)];
            for (int j = 0; j < heads.length; j++) {
                heads[j] = nodes.get(ran.nextInt(nodes.size()));
            }
            for (int j = 0; j < tails.length; j++) {
                tails[j] = nodes.get(ran.nextInt(nodes.size()));
            }
            graph.add(new HGBergeLink(heads, tails));
        }

        List<HGHandle> node2 = hg.findAll(
                graph,
                hg.or(hg.and(hg.type(Node.class), hg.eq("key", 1l)),
                        hg.and(hg.type(Node.class), hg.eq("key", 2l))));
        graph.add(new HGBergeLink(node2.get(0), node2.get(1)));
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

    final static int RUNS = 1;
    final static AbstractMeter[] METERS = { new TimeMeter(Time.MilliSeconds) };
    final static AbstractOutput[] OUTPUT = {};
    final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
    final static double GCPROB = 1.0d;

    static class Config extends AbstractConfig {

        public Config() {
            super(RUNS, METERS, OUTPUT, ARRAN, GCPROB);
        }

    }
    // private static void handles(final Node node, final HyperGraph graph,
    // final HGHandle handle1, final HGHandle handle2) {
    // Object obj = graph.getHandle(node);
    //
    // // Updating operation
    // node.setKey(54);
    // graph.update(node);
    // // removing of a handle and setting the node to annother handle
    // graph.remove(handle1);
    // graph.replace(handle2, node);
    //
    // // see if reset works
    // obj = graph.getHandle(node);
    //
    // // getting object
    // obj = graph.get(handle2);
    // System.out.println(obj);
    // }
}
